package com.rendertweaks.mixin.lionfish;

import com.rendertweaks.lionfish.LionfishBoxCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.github.L_Ender.lionfishapi.client.model.tools.LionfishModelRenderUtils$ModelBox", remap = false)
public abstract class ModelBoxMixin implements LionfishBoxCache {

    @Unique
    private float[] renderTweaks$data;

    @Override
    public float[] renderTweaks$data() {
        return renderTweaks$data;
    }

    @Override
    public void renderTweaks$setData(float[] data) {
        renderTweaks$data = data;
    }
}
