package com.rendertweaks.mixin;

import com.rendertweaks.FrameCounter;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** {@code m_109599_} is {@code LevelRenderer#renderLevel}: one call per frame. */
@Mixin(value = LevelRenderer.class, remap = false)
public abstract class LevelRendererFrameMixin {

    @Inject(method = "m_109599_", at = @At("HEAD"))
    private void renderTweaks$nextFrame(CallbackInfo ci) {
        FrameCounter.next();
    }
}
