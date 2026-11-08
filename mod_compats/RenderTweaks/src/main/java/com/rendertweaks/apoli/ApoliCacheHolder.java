package com.rendertweaks.apoli;

/** Added to {@code Entity}: whether it had no layer-affecting Apoli powers, and in which frame. */
public interface ApoliCacheHolder {
    int renderTweaks$apoliFrame();

    boolean renderTweaks$apoliPlain();

    void renderTweaks$setApoli(int frame, boolean plain);
}
