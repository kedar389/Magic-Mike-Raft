package com.rendertweaks.mixin.subtle;

import com.bawnorton.mixinsquared.TargetHandler;
import com.rendertweaks.rain.RainTint;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Inside Subtle Effects' {@code LevelRendererMixin.renderSnowAndRain} handler (its wrap of the rain
 * vertex colour), {@code m_108811_} is {@code BiomeColors#getAverageWaterColor}. Reached through
 * MixinSquared, so the priority is above Subtle Effects' default 1000.
 */
@Mixin(value = LevelRenderer.class, priority = 1500, remap = false)
public abstract class SubtleRainTintMixin {

    @TargetHandler(mixin = "einstein.subtle_effects.mixin.client.LevelRendererMixin", name = "renderSnowAndRain")
    // require = 0: if a Subtle Effects update changes the handler, skip this tweak instead of failing startup.
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/BiomeColors;m_108811_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"),
            require = 0)
    private int renderTweaks$cachedRainTint(BlockAndTintGetter level, BlockPos pos) {
        return RainTint.waterColor(level, pos);
    }
}
