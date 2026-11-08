package com.rendertweaks.mixin.bagus;

import bagu_chan.bagus_lib.client.event.BagusModelEvent;
import com.rendertweaks.Tweak;
import com.rendertweaks.bagus.PoseReset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bagus Lib 5.6.1. The original listener: if the event's model is supported, reset every part
 * under its root via {@code getAllParts().forEach(ModelPart::resetPose)}. Same work here, from a
 * cached flat array.
 */
@Mixin(targets = "bagu_chan.bagus_lib.client.ClientEventHandler", remap = false)
public abstract class BagusPoseResetMixin {

    // require = 0: if a Bagus Lib update changes the listener, skip this tweak instead of failing startup.
    @Inject(
            method = "animationEvent(Lbagu_chan/bagus_lib/client/event/BagusModelEvent$Init;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private static void renderTweaks$cachedPoseReset(BagusModelEvent.Init event, CallbackInfo ci) {
        if (!Tweak.BAGUS.isEnabled()) {
            return;
        }
        if (!event.isSupportedAnimateModel() || PoseReset.resetAll(event.getRootModel().getBagusRoot())) {
            ci.cancel();
        }
    }
}
