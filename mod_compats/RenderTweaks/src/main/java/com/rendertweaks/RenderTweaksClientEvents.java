package com.rendertweaks;

import com.rendertweaks.bagus.PoseReset;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mod-bus client events. */
@Mod.EventBusSubscriber(modid = RenderTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RenderTweaksClientEvents {

    private RenderTweaksClientEvents() {}

    /** Models are rebuilt on every resource reload, so cached part lists from before are stale. */
    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> PoseReset.invalidate());
    }
}
