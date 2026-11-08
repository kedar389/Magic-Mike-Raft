package com.rendertweaks.apoli;

/** Added to {@code Entity}: per-frame answers about its Apoli powers. */
public interface ApoliCacheHolder {
    int renderTweaks$apoliFrame();

    boolean renderTweaks$apoliPlain();

    void renderTweaks$setApoli(int frame, boolean plain);

    int renderTweaks$glowFrame();

    boolean renderTweaks$selfGlow();

    void renderTweaks$setGlow(int frame, boolean selfGlow);
}
