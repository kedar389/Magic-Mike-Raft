package com.rendertweaks.mixin.lionfish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rendertweaks.lionfish.LionfishFastRender;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lionfish API 3.0. doRender's int arguments are light, then overlay. */
@Mixin(targets = "com.github.L_Ender.lionfishapi.client.model.tools.AdvancedModelBox", remap = false)
public abstract class AdvancedModelBoxMixin {

    @Shadow
    public ObjectList<?> cubeList;

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    private void renderTweaks$batchedRender(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha,
            CallbackInfo ci) {
        if (LionfishFastRender.render(cubeList, pose, consumer, light, overlay, red, green, blue, alpha)) {
            ci.cancel();
        }
    }
}
