package com.rendertweaks.chloride;

/** Added to {@code Entity}: the distance key from the last frame it was computed in. */
public interface SortKeyHolder {
    int renderTweaks$sortFrame();

    double renderTweaks$sortKey();

    void renderTweaks$setSortKey(int frame, double key);
}
