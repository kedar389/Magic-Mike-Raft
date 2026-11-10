package com.rendertweaks.lionfish;

/**
 * Added to Lionfish's {@code AdvancedModelBox}: its Accelerated Rendering renderer, if one was made.
 * Typed as Object so the mixin never names an Accelerated Rendering class.
 */
public interface AcceleratedBoxHolder {
    Object renderTweaks$acceleratedRenderer();

    void renderTweaks$setAcceleratedRenderer(Object renderer);
}
