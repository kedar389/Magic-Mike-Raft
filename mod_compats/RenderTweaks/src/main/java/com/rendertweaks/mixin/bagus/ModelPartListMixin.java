package com.rendertweaks.mixin.bagus;

import com.rendertweaks.bagus.PartListHolder;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ModelPart.class, remap = false)
public abstract class ModelPartListMixin implements PartListHolder {

    @Unique
    private ModelPart[] renderTweaks$parts;

    @Unique
    private int renderTweaks$partsGeneration;

    @Override
    public ModelPart[] renderTweaks$parts() {
        return renderTweaks$parts;
    }

    @Override
    public int renderTweaks$partsGeneration() {
        return renderTweaks$partsGeneration;
    }

    @Override
    public void renderTweaks$setParts(int generation, ModelPart[] parts) {
        renderTweaks$partsGeneration = generation;
        renderTweaks$parts = parts;
    }
}
