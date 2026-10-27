package com.dungeonsillagers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * A white emissive mask re-rendered additively (RenderType.eyes, so shaders treat it as emissive) in a colour and
 * brightness the entity's state decides frame by frame: the Geomancer's beads while casting, a bomb pillar heating up.
 */
final class TintedGlowLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    private final ResourceLocation mask;
    /** RGB already scaled by brightness, or null for no glow this frame. */
    private final BiFunction<T, Float, float[]> tint;

    TintedGlowLayer(GeoRenderer<T> renderer, ResourceLocation mask, BiFunction<T, Float, float[]> tint) {
        super(renderer);
        this.mask = mask;
        this.tint = tint;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        @Nullable float[] rgb = this.tint.apply(animatable, partialTick);
        if (rgb == null) {
            return;
        }
        RenderType glow = RenderType.eyes(this.mask);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, rgb[0], rgb[1], rgb[2], 1.0F);
    }

    /** {@code rgb} (0xRRGGBB) scaled by {@code brightness}. */
    static float[] color(int rgb, float brightness) {
        return new float[] {(rgb >> 16 & 255) / 255.0F * brightness, (rgb >> 8 & 255) / 255.0F * brightness, (rgb & 255) / 255.0F * brightness};
    }
}
