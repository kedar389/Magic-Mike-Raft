package com.rendertweaks;

/**
 * Counts world renders, so per-entity values can be computed once per frame and reused. Starts at
 * 1 so a fresh entity's stored 0 never matches. Render thread only.
 */
public final class FrameCounter {

    private static int frame = 1;

    private FrameCounter() {}

    public static int current() {
        return frame;
    }

    public static void next() {
        frame++;
    }
}
