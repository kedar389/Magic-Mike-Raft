package com.rendertweaks.apoli;

/** Added to {@code Entity}: this frame's answers about its Apoli powers. */
public interface ApoliCacheHolder {
    int renderTweaks$apoliFrame();

    /** No Invisibility or Prevent Feature Render power. */
    boolean renderTweaks$apoliPlain();

    /** Has a Self Glow power. */
    boolean renderTweaks$selfGlow();

    void renderTweaks$setApoli(int frame, boolean plain, boolean selfGlow);
}
