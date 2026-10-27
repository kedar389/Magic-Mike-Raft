package com.dungeonsillagers.client;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/** Reports where a model's hand bones were drawn, so effects on a hand (Niagara SocketName J_L_Hand...) follow it. */
final class HandTracking {

    private HandTracking() {
    }

    /** Call after rendering: turns matrix tracking on for both bones and hands their last world positions on. */
    static void report(GeoModel<?> model, Entity entity, String leftBone, String rightBone) {
        VfxLibrary.trackHands(entity, position(model.getBone(leftBone)), position(model.getBone(rightBone)));
    }

    @Nullable
    private static Vec3 position(Optional<GeoBone> bone) {
        if (bone.isEmpty()) {
            return null;
        }
        GeoBone b = bone.get();
        b.setTrackingMatrices(true);
        Vector3d p = b.getWorldPosition();
        return p.lengthSquared() == 0 ? null : new Vec3(p.x, p.y, p.z);
    }
}
