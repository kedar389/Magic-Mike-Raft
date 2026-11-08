package com.rendertweaks.light;

/** Added to {@code BlockEntity}: its last packed light value and the frame it was read in. */
public interface LightCacheHolder {
    int renderTweaks$lightFrame();

    int renderTweaks$light();

    void renderTweaks$setLight(int frame, int light);
}
