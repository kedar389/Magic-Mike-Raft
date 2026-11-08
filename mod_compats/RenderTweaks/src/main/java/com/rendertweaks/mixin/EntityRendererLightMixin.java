package com.rendertweaks.mixin;

import com.rendertweaks.light.EntityLight;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code m_114508_} is {@code EntityRenderer#getSkyLightLevel}, {@code m_6086_} is
 * {@code getBlockLightLevel}; {@code m_45517_} is {@code getBrightness(LightLayer, BlockPos)}. Only
 * the world lookups inside them are redirected, so the on-fire check in getBlockLightLevel and any
 * renderer override (blazes, glowing mobs) keep working as before.
 */
@Mixin(value = EntityRenderer.class, remap = false)
public abstract class EntityRendererLightMixin {

    private static final String GET_BRIGHTNESS =
            "Lnet/minecraft/world/level/Level;m_45517_(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I";

    @Redirect(method = "m_114508_", at = @At(value = "INVOKE", target = GET_BRIGHTNESS))
    private int renderTweaks$cachedSkyLight(Level level, LightLayer layer, BlockPos pos, Entity entity, BlockPos entityPos) {
        return EntityLight.brightness(level, layer, pos, entity);
    }

    @Redirect(method = "m_6086_", at = @At(value = "INVOKE", target = GET_BRIGHTNESS))
    private int renderTweaks$cachedBlockLight(Level level, LightLayer layer, BlockPos pos, Entity entity, BlockPos entityPos) {
        return EntityLight.brightness(level, layer, pos, entity);
    }
}
