package com.rendertweaks.mixin.chloride;

import com.rendertweaks.chloride.EntitySortKeys;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** {@code m_109599_} is {@code LevelRenderer#renderLevel}, where Chloride's sort runs. */
@Mixin(value = LevelRenderer.class, remap = false)
public abstract class LevelRendererFrameMixin {

    @Inject(method = "m_109599_", at = @At("HEAD"))
    private void renderTweaks$nextFrame(CallbackInfo ci) {
        EntitySortKeys.nextFrame();
    }
}
