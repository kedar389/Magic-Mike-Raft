package com.rendertweaks.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rendertweaks.FrameCounter;
import com.rendertweaks.Tweak;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Vanilla {@code EntityRenderer} reads sky light and block light from the world for every entity it
 * draws, every frame, and again for the shadow pass: about 1.2% of the frame with 300 entities.
 * Those values only change when the entity moves into another block or a light update reaches it.
 *
 * <p>This keeps the last value per entity and layer while the entity stays in the same block, and
 * re-reads at least every {@link #REFRESH_FRAMES} frames for light updates. Only the world lookup is
 * cached: on-fire glow and renderers that override the light level are untouched.
 */
public final class EntityLight {

    private static final int REFRESH_FRAMES = 8;

    private EntityLight() {}

    public static int brightness(Level level, LightLayer layer, BlockPos pos, Entity entity) {
        if (!Tweak.ENTITY_LIGHT.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return level.getBrightness(layer, pos);
        }
        boolean sky = layer == LightLayer.SKY;
        EntityLightHolder holder = (EntityLightHolder) entity;
        int frame = FrameCounter.current();
        int stamp = holder.renderTweaks$lightFrame(sky);
        long key = pos.asLong();
        if (stamp != 0 && holder.renderTweaks$lightPos(sky) == key && frame >= stamp && frame - stamp < REFRESH_FRAMES) {
            return holder.renderTweaks$lightValue(sky);
        }
        int value = level.getBrightness(layer, pos);
        holder.renderTweaks$setLight(sky, frame, key, value);
        return value;
    }
}
