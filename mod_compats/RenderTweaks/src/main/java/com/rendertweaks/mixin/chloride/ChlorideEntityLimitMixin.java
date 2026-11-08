package com.rendertweaks.mixin.chloride;

import com.bawnorton.mixinsquared.TargetHandler;
import com.rendertweaks.Tweak;
import com.rendertweaks.chloride.NearestEntities;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runs in front of Chloride's {@code redirect$entitiesForRendering}, its redirect of
 * {@code entitiesForRendering()} in {@code LevelRenderer.renderLevel}. Reached through MixinSquared,
 * so the priority is above Chloride's default 1000.
 */
@Mixin(value = LevelRenderer.class, priority = 1500, remap = false)
public abstract class ChlorideEntityLimitMixin {

    @TargetHandler(
            mixin = "me.srrapero720.chloride.mixins.impl.EntityDistanceCullingMixin$LevelRendererEntityMixin",
            name = "redirect$entitiesForRendering")
    // require = 0: if a Chloride update renames the handler, skip this tweak instead of failing startup.
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderTweaks$nearestEntities(ClientLevel level, CallbackInfoReturnable<Iterable<Entity>> cir) {
        if (!Tweak.CHLORIDE.isEnabled()) {
            return;
        }
        Iterable<Entity> nearest = NearestEntities.select(level);
        if (nearest != null) {
            cir.setReturnValue(nearest);
        }
    }
}
