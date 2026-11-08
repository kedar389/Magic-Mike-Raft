package com.rendertweaks;

import com.rendertweaks.gtbc.TickEntityQueries;
import com.rendertweaks.rain.RainTint;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge-bus client events. */
@Mod.EventBusSubscriber(modid = RenderTweaks.MOD_ID, value = Dist.CLIENT)
public final class RenderTweaksForgeEvents {

    private RenderTweaksForgeEvents() {}

    /** Caches holding a level or its entities let go of it, so a left world can be collected. */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            RainTint.clear();
            TickEntityQueries.clear();
        }
    }
}
