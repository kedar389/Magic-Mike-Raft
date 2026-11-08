package com.rendertweaks.rain;

import com.rendertweaks.FrameCounter;
import com.rendertweaks.Tweak;
import java.util.Arrays;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Subtle Effects 1.15.1 tints rain by biome ({@code biomeColorRain}) by wrapping every vertex colour
 * call in vanilla's rain loop: 4 calls per rain column, up to 441 columns, each averaging the biome
 * water colour through vanilla's tint cache and its read lock. About 0.8% of the frame while it
 * rains, for a colour that only changes when the column changes biome.
 *
 * <p>This keeps each column's colour for {@link #REFRESH_FRAMES} frames in a 64 x 64 grid keyed by
 * position. The rain radius is at most 10, so columns of one frame never share a slot.
 */
public final class RainTint {

    private static final int REFRESH_FRAMES = 20;
    private static final int SIZE = 64;
    private static final int MASK = SIZE - 1;

    private static final long[] positions = new long[SIZE * SIZE];
    private static final int[] colors = new int[SIZE * SIZE];
    private static final int[] frames = new int[SIZE * SIZE];
    private static BlockAndTintGetter lastLevel;

    private RainTint() {}

    public static int waterColor(BlockAndTintGetter level, BlockPos pos) {
        if (!Tweak.RAIN.isEnabled()) {
            return BiomeColors.getAverageWaterColor(level, pos);
        }
        if (level != lastLevel) {
            clear();
            lastLevel = level;
        }
        int slot = ((pos.getX() & MASK) << 6) | (pos.getZ() & MASK);
        long key = pos.asLong();
        int frame = FrameCounter.current();
        int stamp = frames[slot];
        if (stamp != 0 && positions[slot] == key && frame >= stamp && frame - stamp < REFRESH_FRAMES) {
            return colors[slot];
        }
        int color = BiomeColors.getAverageWaterColor(level, pos);
        positions[slot] = key;
        colors[slot] = color;
        frames[slot] = frame;
        return color;
    }

    /** Drops every cached colour and the level reference, so an unloaded level is not kept. */
    public static void clear() {
        Arrays.fill(frames, 0);
        lastLevel = null;
    }
}
