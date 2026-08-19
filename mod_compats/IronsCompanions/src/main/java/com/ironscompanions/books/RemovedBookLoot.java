package com.ironscompanions.books;

import dev.xylonity.companions.registry.CompanionsBlocks;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Drops the two books Companions injects into chest loot, keeping its coins.
 *
 * <p>{@code CompanionsLootTables} appends one pool named {@code coin_pool} to every
 * {@code minecraft:chests/*} table, holding a dimension-appropriate coin plus {@code book_black_hole}
 * and {@code book_magic_ray} at 4.5% each. A pool's entries are a final package-private array, so the
 * books cannot be picked out of it; instead the whole pool is removed and replaced with a coin-only
 * copy carrying the same chances and counts.
 *
 * <p>Runs at {@link EventPriority#LOWEST}, so Companions has already added its pool.
 */
public final class RemovedBookLoot {

    private static final String COMPANIONS_COIN_POOL = "coin_pool";
    private static final String REPLACEMENT_COIN_POOL = "irons_companions_coin_pool";

    private static final String VANILLA = "minecraft";
    private static final String CHEST_TABLES = "chests/";

    private RemovedBookLoot() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation table = event.getName();
        if (!VANILLA.equals(table.getNamespace()) || !table.getPath().startsWith(CHEST_TABLES)) {
            return;
        }
        if (event.getTable().removePool(COMPANIONS_COIN_POOL) == null) {
            return; // Companions did not contribute to this table
        }
        event.getTable().addPool(coinOnlyPool(table.getPath()));
    }

    /** Mirrors Companions' own coin selection: which coin, how many, and how often. */
    private static LootPool coinOnlyPool(String tablePath) {
        Supplier<Block> coin;
        float chance;
        NumberProvider count;
        if (tablePath.contains("nether")) {
            coin = CompanionsBlocks.NETHER_COIN;
            chance = 0.45f;
            count = UniformGenerator.between(1f, 3f);
        } else if (tablePath.contains("end")) {
            coin = CompanionsBlocks.END_COIN;
            chance = 0.8f;
            count = ConstantValue.exactly(1f);
        } else {
            coin = CompanionsBlocks.COPPER_COIN;
            chance = 0.075f;
            count = UniformGenerator.between(1f, 5f);
        }
        return LootPool.lootPool()
                .name(REPLACEMENT_COIN_POOL)
                .setRolls(ConstantValue.exactly(1f))
                .add(LootItem.lootTableItem(coin.get().asItem())
                        .apply(SetItemCountFunction.setCount(count))
                        .when(LootItemRandomChanceCondition.randomChance(chance)))
                .build();
    }
}
