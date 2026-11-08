package com.rendertweaks.light;

import com.rendertweaks.FrameCounter;
import com.rendertweaks.Tweak;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Vanilla asks {@code LevelRenderer.getLightColor} for every block entity it draws, every frame:
 * the block at that position plus its block light and sky light, read from chunk storage. In a
 * busy base that was 4.2% of the frame, for values that only change when a light update reaches
 * that block.
 *
 * <p>This keeps each block entity's packed light and re-reads it once every {@link #REFRESH_FRAMES}
 * frames. Refreshes are spread over the frames by position, so they never all land in one frame.
 * A torch placed next to a chest lights it up at most 8 frames later, about 40 ms at 200 FPS.
 * Day and night are unaffected: the packed value holds light levels, the time-of-day brightness is
 * applied later by the lightmap.
 */
public final class BlockEntityLight {

    /** Power of two, so the phase is a mask. */
    private static final int REFRESH_FRAMES = 8;

    private BlockEntityLight() {}

    public static int lightColor(BlockAndTintGetter level, BlockPos pos, BlockEntity blockEntity) {
        if (!Tweak.LIGHT.isEnabled()) {
            return LevelRenderer.getLightColor(level, pos);
        }
        LightCacheHolder holder = (LightCacheHolder) blockEntity;
        int frame = FrameCounter.current();
        int stamp = holder.renderTweaks$lightFrame();
        boolean onPhase = ((frame + pos.hashCode()) & (REFRESH_FRAMES - 1)) == 0;
        // stamp 0: never read. The age check covers block entities that were off screen on their
        // phase frame, and the frame counter itself wrapping.
        if (stamp == 0 || onPhase || frame - stamp >= REFRESH_FRAMES || frame < stamp) {
            int light = LevelRenderer.getLightColor(level, pos);
            holder.renderTweaks$setLight(frame, light);
            return light;
        }
        return holder.renderTweaks$light();
    }
}
