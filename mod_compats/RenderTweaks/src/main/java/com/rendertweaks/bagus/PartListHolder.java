package com.rendertweaks.bagus;

import net.minecraft.client.model.geom.ModelPart;

/** Added to {@code ModelPart}: every part in its tree, flattened once, and the reload it is from. */
public interface PartListHolder {
    ModelPart[] renderTweaks$parts();

    int renderTweaks$partsGeneration();

    void renderTweaks$setParts(int generation, ModelPart[] parts);
}
