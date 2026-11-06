package com.rendertweaks.lionfish;

/** Added to Lionfish's {@code ModelBox}: its quads flattened by {@link LionfishBoxData}. */
public interface LionfishBoxCache {
    float[] renderTweaks$data();

    void renderTweaks$setData(float[] data);
}
