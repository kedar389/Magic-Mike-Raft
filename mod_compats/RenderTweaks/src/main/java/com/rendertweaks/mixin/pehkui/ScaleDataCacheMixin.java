package com.rendertweaks.mixin.pehkui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rendertweaks.Tweak;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pehkui 3.8.2 {@code ScaleData.getScale(float)} recomputes on every call on the client: the base
 * scale lerp, then every modifier in a sorted set, each of which may look up another scale the same
 * way. Pehkui caches the result itself, but only on the server and only for a delta of 1. Mob
 * rendering, entity-search bounding boxes and the shadow pass ask for the same scales many times a
 * frame: about 4.6% of the render thread with 300 entities, almost all of it returning 1.0.
 *
 * <p>This remembers the last result per scale and delta, on the render thread only, and forgets it
 * wherever Pehkui forgets its own cache ({@code invalidateCachedScales}: every tick, every update).
 * Within a frame the partial tick is constant, so render calls hit; tick-time calls (delta 1) hit
 * until the next tick. A scale that depends on another (width on base) can be one frame behind
 * when only the base changes between ticks, the same window Pehkui's server cache accepts.
 */
@Mixin(targets = "virtuoel.pehkui.api.ScaleData", remap = false)
public abstract class ScaleDataCacheMixin {

    @Shadow
    public abstract Entity getEntity();

    @Unique
    private boolean renderTweaks$valid;

    @Unique
    private float renderTweaks$delta;

    @Unique
    private float renderTweaks$scale;

    @Inject(method = "getScale(F)F", at = @At("HEAD"), cancellable = true)
    private void renderTweaks$cachedScale(float delta, CallbackInfoReturnable<Float> cir) {
        if (renderTweaks$valid
                && renderTweaks$delta == delta
                && Tweak.PEHKUI.isEnabled()
                && RenderSystem.isOnRenderThread()) {
            cir.setReturnValue(renderTweaks$scale);
        }
    }

    @Inject(method = "getScale(F)F", at = @At("RETURN"))
    private void renderTweaks$storeScale(float delta, CallbackInfoReturnable<Float> cir) {
        if (!Tweak.PEHKUI.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return;
        }
        Entity entity = getEntity();
        if (entity == null || entity.level() == null || !entity.level().isClientSide) {
            return;
        }
        renderTweaks$delta = delta;
        renderTweaks$scale = cir.getReturnValueF();
        renderTweaks$valid = true;
    }

    @Inject(method = "invalidateCachedScales()V", at = @At("TAIL"))
    private void renderTweaks$forgetScale(CallbackInfo ci) {
        renderTweaks$valid = false;
    }
}
