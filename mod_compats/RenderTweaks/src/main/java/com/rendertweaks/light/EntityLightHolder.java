package com.rendertweaks.light;

/** Added to {@code Entity}: last sky and block light read for it, the block, and the frame. */
public interface EntityLightHolder {
    int renderTweaks$lightFrame(boolean sky);

    long renderTweaks$lightPos(boolean sky);

    int renderTweaks$lightValue(boolean sky);

    void renderTweaks$setLight(boolean sky, int frame, long pos, int value);
}
