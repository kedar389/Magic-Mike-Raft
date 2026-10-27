package com.dungeonsillagers.client;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * Reports where a model's bones were drawn, so effects on a hand (Niagara SocketName J_L_Hand...) follow it. GeckoLib
 * captures a tracked bone's world matrix at its pivot, after the bone's rotation and scale.
 */
final class HandTracking {

    private HandTracking() {
    }

    /** Call after rendering: turns matrix tracking on for both bones and hands their last world positions on. */
    static void report(GeoModel<?> model, Entity entity, String leftBone, String rightBone) {
        VfxLibrary.trackHands(entity, point(model.getBone(leftBone), 0, 0, 0), point(model.getBone(rightBone), 0, 0, 0));
    }

    /**
     * World position of a point {@code (dx, dy, dz)} blocks from the bone's pivot, in the bone's axes (front -Z),
     * as last drawn; null before the first tracked frame.
     */
    @Nullable
    static Vec3 point(Optional<GeoBone> bone, float dx, float dy, float dz) {
        if (bone.isEmpty()) {
            return null;
        }
        GeoBone b = bone.get();
        if (!b.isTrackingMatrices()) {
            b.setTrackingMatrices(true);
            return null;
        }
        Vector4f p = b.getWorldSpaceMatrix().transform(new Vector4f(dx, dy, dz, 1.0F));
        return new Vec3(p.x(), p.y(), p.z());
    }
}
