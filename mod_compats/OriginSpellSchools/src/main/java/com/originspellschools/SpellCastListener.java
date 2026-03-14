package com.originspellschools;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;
import java.util.Optional;

/**
 * Listens for Iron's Spellbooks SpellPreCastEvent and cancels it if the
 * player's current origin does not allow that spell's school of magic.
 */
public class SpellCastListener {

    /**
     * Layer that Origins uses for the primary origin choice.
     * Cached lazily since OriginLayers is populated after mods load.
     */
    private static final ResourceLocation ORIGIN_LAYER_ID = new ResourceLocation("origins", "origin");

    private final SchoolRestrictionConfig config;

    public SpellCastListener(SchoolRestrictionConfig config) {
        this.config = config;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();

        // Only intercept on the server side
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Get the spell's school
        SchoolType school = event.getSchoolType();
        if (school == null) return;

        // Resolve the school's registry name (e.g. "irons_spellbooks:fire" → we want "fire")
        ResourceLocation schoolRegistryId = school.getId();
        if (schoolRegistryId == null) return;
        String schoolName = schoolRegistryId.getPath().toLowerCase(Locale.ROOT);

        // Get the player's current origin
        Optional<ResourceLocation> originIdOpt = getPlayerOriginId(serverPlayer);
        if (originIdOpt.isEmpty()) {
            // Player hasn't chosen an origin yet — let them cast freely
            return;
        }
        ResourceLocation originId = originIdOpt.get();

        // Check config
        if (!config.isSchoolAllowed(originId, schoolName)) {
            event.setCanceled(true);

            if (config.shouldNotifyPlayer()) {
                sendBlockMessage(serverPlayer, school, originId);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Origin lookup
    // -----------------------------------------------------------------------

    private static Optional<ResourceLocation> getPlayerOriginId(ServerPlayer player) {
        try {
            // Origins (Forge) stores origin data via Cardinal Components.
            // ModComponents.ORIGIN is the ComponentKey<OriginComponent>.
            OriginComponent component = ModComponents.ORIGIN.get(player);
            if (component == null) return Optional.empty();

            OriginLayer layer = OriginLayers.getLayer(ORIGIN_LAYER_ID);
            if (layer == null) return Optional.empty();

            Origin origin = component.getOrigin(layer);
            if (origin == null || origin.isSpecial()) return Optional.empty();

            ResourceLocation id = origin.getIdentifier();
            return Optional.ofNullable(id);
        } catch (Exception e) {
            OriginSpellSchoolsMod.LOGGER.debug("[OriginSpellSchools] Could not retrieve origin for {}: {}", player.getName().getString(), e.getMessage());
            return Optional.empty();
        }
    }

    // -----------------------------------------------------------------------
    // Player notification
    // -----------------------------------------------------------------------

    private static void sendBlockMessage(ServerPlayer player, SchoolType school, ResourceLocation originId) {
        // Build a colored message: "Your origin (blazeborn) cannot wield Fire magic."
        String originName = formatOriginName(originId);
        MutableComponent schoolName = school.getDisplayName().copy().withStyle(getSchoolColor(school));

        MutableComponent msg = Component.literal("✗ ")
            .withStyle(ChatFormatting.RED)
            .append(Component.literal("Your origin ")
                .withStyle(ChatFormatting.GRAY))
            .append(Component.literal("(" + originName + ")")
                .withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" cannot wield ")
                .withStyle(ChatFormatting.GRAY))
            .append(schoolName)
            .append(Component.literal(" magic.")
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(msg);
    }

    private static String formatOriginName(ResourceLocation id) {
        // "medievalorigins:high_elf" → "High Elf"
        String[] parts = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1));
        }
        return sb.toString();
    }

    private static ChatFormatting getSchoolColor(SchoolType school) {
        if (school.getId() == null) return ChatFormatting.WHITE;
        return switch (school.getId().getPath()) {
            case "fire"       -> ChatFormatting.RED;
            case "ice"        -> ChatFormatting.AQUA;
            case "lightning"  -> ChatFormatting.YELLOW;
            case "holy"       -> ChatFormatting.WHITE;
            case "ender"      -> ChatFormatting.DARK_PURPLE;
            case "blood"      -> ChatFormatting.DARK_RED;
            case "evocation"  -> ChatFormatting.GREEN;
            case "nature"     -> ChatFormatting.DARK_GREEN;
            case "eldritch"   -> ChatFormatting.DARK_GRAY;
            default           -> ChatFormatting.LIGHT_PURPLE;
        };
    }
}
