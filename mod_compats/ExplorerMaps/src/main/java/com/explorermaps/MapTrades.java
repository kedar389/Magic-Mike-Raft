package com.explorermaps;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Resolves the config against the running server and builds the map offers.
 *
 * <p>The structure search runs synchronously at offer creation, like vanilla's explorer maps. Its
 * radius is counted in placement cells ({@code spacing} chunks each), so it is derived per target
 * from the live spacing to cap the map distance at {@code max_distance_blocks}.
 */
public final class MapTrades {

    private static final int MAX_RADIUS_CELLS = 32;
    /** Concentric-ring placements have no spacing; vanilla uses this for strongholds. */
    private static final int FALLBACK_SPACING = 32;

    private record Target(String name, HolderSet<Structure> structures, int radius) {
    }

    private record Tier(MapTradeConfig.Tier config, MapDecoration.Type decoration, List<Target> targets) {
    }

    private static volatile Map<Integer, Tier> tiersByLevel = Map.of();
    private static volatile int attempts = 1;

    private MapTrades() {
    }

    static void load(MinecraftServer server) {
        MapTradeConfig config;
        try {
            config = MapTradeConfig.read();
        } catch (Exception e) {
            ExplorerMaps.LOGGER.error("Cannot read explorer_maps.json, no map trades added", e);
            return;
        }
        ServerLevel overworld = server.overworld();
        Registry<Structure> registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ChunkGeneratorStructureState state = overworld.getChunkSource().getGeneratorState();
        int maxDistanceChunks = Math.max(1, config.maxDistanceBlocks() / 16);

        Map<Integer, Tier> resolved = new HashMap<>();
        int targetCount = 0;
        for (MapTradeConfig.Tier tier : config.tiers()) {
            MapDecoration.Type decoration = MapDecoration.Type.valueOf(tier.decoration().toUpperCase(Locale.ROOT));
            List<Target> targets = new ArrayList<>();
            for (MapTradeConfig.Target target : tier.targets()) {
                Target t = resolve(target, registry, state, maxDistanceChunks);
                if (t != null) {
                    targets.add(t);
                }
            }
            if (!targets.isEmpty()) {
                resolved.put(tier.level(), new Tier(tier, decoration, List.copyOf(targets)));
                targetCount += targets.size();
            }
        }
        tiersByLevel = Map.copyOf(resolved);
        attempts = Math.max(1, config.attempts());
        ExplorerMaps.LOGGER.info("Loaded {} explorer map targets across {} cartographer levels", targetCount, resolved.size());
    }

    static void clear() {
        tiersByLevel = Map.of();
    }

    private static Target resolve(MapTradeConfig.Target target, Registry<Structure> registry,
                                  ChunkGeneratorStructureState state, int maxDistanceChunks) {
        List<Holder<Structure>> holders = new ArrayList<>();
        int minSpacing = Integer.MAX_VALUE;
        for (String id : target.structures()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            var holder = location == null ? null
                    : registry.getHolder(ResourceKey.create(Registries.STRUCTURE, location)).orElse(null);
            if (holder == null) {
                ExplorerMaps.LOGGER.warn("Explorer map '{}': unknown structure {}", target.name(), id);
                continue;
            }
            List<StructurePlacement> placements = state.getPlacementsForStructure(holder);
            if (placements.isEmpty()) {
                ExplorerMaps.LOGGER.warn("Explorer map '{}': {} does not generate in the overworld", target.name(), id);
                continue;
            }
            holders.add(holder);
            for (StructurePlacement placement : placements) {
                int spacing = placement instanceof RandomSpreadStructurePlacement spread ? spread.spacing() : FALLBACK_SPACING;
                minSpacing = Math.min(minSpacing, spacing);
            }
        }
        if (holders.isEmpty()) {
            ExplorerMaps.LOGGER.warn("Explorer map '{}' dropped, no structure resolved", target.name());
            return null;
        }
        int radius = Mth.clamp(Mth.positiveCeilDiv(maxDistanceChunks, minSpacing), 1, MAX_RADIUS_CELLS);
        return new Target(target.name(), HolderSet.direct(holders), radius);
    }

    /** Called at the end of {@code Villager#updateTrades}, once per level reached. */
    public static void addTo(Villager villager) {
        if (!(villager.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (villager.getVillagerData().getProfession() != VillagerProfession.CARTOGRAPHER) {
            return;
        }
        Tier tier = tiersByLevel.get(villager.getVillagerData().getLevel());
        if (tier == null || !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
            return;
        }
        MerchantOffer offer = createOffer(villager, level, tier);
        if (offer != null) {
            villager.getOffers().add(offer);
        }
    }

    private static MerchantOffer createOffer(Villager villager, ServerLevel level, Tier tier) {
        RandomSource random = villager.getRandom();
        List<Target> pool = new ArrayList<>(tier.targets());
        for (int i = 0; i < attempts && !pool.isEmpty(); i++) {
            Target target = pool.remove(random.nextInt(pool.size()));
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, target.structures(), villager.blockPosition(), target.radius(), false);
            if (found == null) {
                ExplorerMaps.LOGGER.debug("No '{}' within {} cells of {}", target.name(), target.radius(), villager.blockPosition());
                continue;
            }
            return new MerchantOffer(
                    new ItemStack(Items.EMERALD, tier.config().emeralds()),
                    new ItemStack(Items.COMPASS),
                    createMap(level, found.getFirst(), target.name(), tier.decoration()),
                    tier.config().maxUses(),
                    tier.config().xp(),
                    tier.config().priceMultiplier());
        }
        return null;
    }

    /** Same steps as vanilla's {@code TreasureMapForEmeralds}. */
    private static ItemStack createMap(ServerLevel level, BlockPos pos, String name, MapDecoration.Type decoration) {
        ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, pos, "+", decoration);
        map.setHoverName(Component.literal(name + " Map").withStyle(style -> style.withItalic(false)));
        return map;
    }
}
