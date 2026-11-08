package com.rendertweaks.mixin.apoli;

import com.bawnorton.mixinsquared.TargetHandler;
import com.rendertweaks.Tweak;
import com.rendertweaks.apoli.ApoliPowers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runs in front of Apoli's {@code EntityRenderDispatcherMixin.preventRenderingEntities}, its inject
 * into {@code EntityRenderDispatcher.shouldRender}. That handler only ever hides an entity when the
 * local player has a Prevent Entity Render power, so with none this frame it is skipped. Skipping
 * it leaves {@code shouldRender} to vanilla, exactly as Apoli does when nothing matches.
 */
@Mixin(value = EntityRenderDispatcher.class, priority = 1500, remap = false)
public abstract class ApoliEntityRenderCheckMixin {

    @TargetHandler(mixin = "io.github.apace100.apoli.mixin.EntityRenderDispatcherMixin", name = "preventRenderingEntities")
    // require = 0: if an Apoli update renames the handler, skip this tweak instead of failing startup.
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderTweaks$skipWithoutPower(
            Entity entity,
            Frustum frustum,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfoReturnable<Boolean> apoliCir,
            CallbackInfo ci) {
        if (Tweak.APOLI.isEnabled() && !ApoliPowers.playerMayHideEntities()) {
            ci.cancel();
        }
    }
}
