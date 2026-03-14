package com.originspellschools;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(OriginSpellSchoolsMod.MOD_ID)
public class OriginSpellSchoolsMod {

    public static final String MOD_ID = "originspellschools";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static SchoolRestrictionConfig config;
    private static SpellCastListener listener;

    public OriginSpellSchoolsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        config = SchoolRestrictionConfig.load();
        listener = new SpellCastListener(config);
        MinecraftForge.EVENT_BUS.register(listener);
        LOGGER.info("[OriginSpellSchools] Loaded with {} origin mappings.", config.getMappingCount());
    }

    /** Called by /originschools reload */
    public static void onConfigReload(SchoolRestrictionConfig newConfig) {
        if (listener != null) {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        config = newConfig;
        listener = new SpellCastListener(config);
        MinecraftForge.EVENT_BUS.register(listener);
        LOGGER.info("[OriginSpellSchools] Config reloaded: {} mappings.", config.getMappingCount());
    }

    /** Returns a human-readable summary of a player's origin and their allowed schools. */
    public static String getOriginInfo(ServerPlayer player) {
        try {
            OriginComponent component = ModComponents.ORIGIN.get(player);
            OriginLayer layer = OriginLayers.getLayer(new ResourceLocation("origins", "origin"));
            if (component == null || layer == null) return "Could not read origin data.";

            Origin origin = component.getOrigin(layer);
            if (origin == null || origin.isSpecial()) return "No origin selected.";

            ResourceLocation originId = origin.getIdentifier();
            String[] allSchools = {"fire","ice","lightning","holy","ender","blood","evocation","nature","eldritch"};

            StringBuilder sb = new StringBuilder();
            sb.append("Origin: ").append(originId).append("\nAllowed schools: ");
            boolean first = true;
            for (String school : allSchools) {
                if (config.isSchoolAllowed(originId, school)) {
                    if (!first) sb.append(", ");
                    sb.append(school);
                    first = false;
                }
            }
            if (first) sb.append("(none)");
            return sb.toString();
        } catch (Exception e) {
            return "Error reading origin: " + e.getMessage();
        }
    }

    public static SchoolRestrictionConfig getConfig() {
        return config;
    }
}
