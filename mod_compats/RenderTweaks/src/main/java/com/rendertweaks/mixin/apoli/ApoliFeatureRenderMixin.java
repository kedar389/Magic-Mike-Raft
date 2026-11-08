package com.rendertweaks.mixin.apoli;

import com.bawnorton.mixinsquared.TargetHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rendertweaks.Tweak;
import com.rendertweaks.apoli.ApoliPowers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs in front of Apoli's {@code LivingEntityRendererMixin.preventFeatureRendering}, the redirect
 * Apoli puts around every {@code RenderLayer.render} call in {@code LivingEntityRenderer.render}.
 * Reached through MixinSquared, so the priority is above Apoli's default 1000: its handler must
 * already be merged when this applies.
 */
@Mixin(value = LivingEntityRenderer.class, priority = 1500, remap = false)
public abstract class ApoliFeatureRenderMixin {

    @TargetHandler(mixin = "io.github.apace100.apoli.mixin.LivingEntityRendererMixin", name = "preventFeatureRendering")
    // require = 0: if an Apoli update renames the handler, skip this tweak instead of failing startup.
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderTweaks$plainEntityLayer(
            RenderLayer layer,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            LivingEntity livingEntity,
            CallbackInfo ci) {
        // Apoli checks invisibility on livingEntity and layer powers on entity. They are the same
        // object in LivingEntityRenderer; anything else is left to Apoli.
        if (!Tweak.APOLI.isEnabled() || entity != livingEntity || !ApoliPowers.isPlain(entity)) {
            return;
        }
        layer.render(poseStack, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        ci.cancel();
    }
}
