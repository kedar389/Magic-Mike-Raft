package com.rendertweaks.bagus;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Bagus Lib 5.6.1 resets the pose of every part of every supported mob model, every frame
 * ({@code ClientEventHandler.animationEvent(BagusModelEvent.Init)}), by building a Java stream over
 * the whole part tree with {@code ModelPart.getAllParts()}. About 1.2% of the frame. The tree of a
 * baked model does not change, so this flattens it once and loops over the array.
 *
 * <p>Two exceptions keep Bagus Lib's own code: models from Entity Model Features (Fresh Animations
 * and similar packs), whose parts can change at runtime, and any model seen before the last
 * resource reload, which rebuilds models.
 */
public final class PoseReset {

    private static final String EMF_PACKAGE = "traben.entity_model_features.";
    private static final ModelPart[] UNCACHEABLE = new ModelPart[0];

    /** Bumped on every resource reload. Starts at 1 so a fresh part's 0 never matches. */
    private static int generation = 1;

    private PoseReset() {}

    public static void invalidate() {
        generation++;
    }

    /** Resets every part under root. False if root must go through Bagus Lib's own code. */
    public static boolean resetAll(ModelPart root) {
        if (root == null) {
            return false;
        }
        // ModelPart is final, so javac needs the cast through Object; the mixin adds the interface.
        PartListHolder holder = (PartListHolder) (Object) root;
        ModelPart[] parts = holder.renderTweaks$parts();
        if (parts == null || holder.renderTweaks$partsGeneration() != generation) {
            parts = flatten(root);
            holder.renderTweaks$setParts(generation, parts);
        }
        if (parts == UNCACHEABLE) {
            return false;
        }
        for (ModelPart part : parts) {
            part.resetPose();
        }
        return true;
    }

    private static ModelPart[] flatten(ModelPart root) {
        ModelPart[] parts = root.getAllParts().toArray(ModelPart[]::new);
        for (ModelPart part : parts) {
            if (part.getClass().getName().startsWith(EMF_PACKAGE)) {
                return UNCACHEABLE;
            }
        }
        return parts;
    }
}
