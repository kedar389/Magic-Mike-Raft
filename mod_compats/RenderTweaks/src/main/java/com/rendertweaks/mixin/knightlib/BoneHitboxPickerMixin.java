package com.rendertweaks.mixin.knightlib;

import com.rendertweaks.Tweak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * KnightLib 2.0.5 {@code BoneHitboxPicker.pick} runs twice a frame and walks every entity in the
 * level ({@code entitiesForRendering}) to find the few with bone hitboxes near the crosshair: about
 * 2% of the frame with 460 entities loaded. This hands it only the entities near the look ray,
 * from the level's spatial index. pick's own filters still run on each one.
 */
@Mixin(targets = "dev.xylonity.knightlib.client.entity.layer.BoneHitboxPicker", remap = false)
public abstract class BoneHitboxPickerMixin {

    /** pick's longest reach, creative's 6 blocks. Survival uses at most 3. */
    @Unique
    private static final double REACH = 6.0;

    /**
     * pick keeps an entity only if its box grown by 4 touches the ray. The extra 12 covers entities
     * wider than the 2-block slack the section lookup allows, such as large bosses.
     */
    @Unique
    private static final double MARGIN = 4.0 + 12.0;

    /** {@code m_104735_} is {@code ClientLevel#entitiesForRendering}. */
    @Redirect(
            method = "pick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;m_104735_()Ljava/lang/Iterable;"))
    private static Iterable<Entity> renderTweaks$entitiesNearRay(ClientLevel level, Minecraft minecraft, float partialTick) {
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || !Tweak.KNIGHTLIB.isEnabled()) {
            return level.entitiesForRendering();
        }
        Vec3 eye = camera.getEyePosition(partialTick);
        Vec3 end = eye.add(camera.getViewVector(partialTick).scale(REACH));
        return level.getEntities(camera, new AABB(eye, end).inflate(MARGIN));
    }
}
