package com.dungeonsillagers.client;

import com.dungeonsillagers.DungeonsIllagersMod;
import com.dungeonsillagers.SupremeEvoker;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * The Supreme Evoker's protective bubble: SM_TotemOfShielding-Shield (exported by bubble.js as triangles in blocks),
 * drawn translucent and full-bright in MI_SupremeEvoker_ProtectiveBubble's Color #AA00E8. It fades as it takes damage.
 */
final class ShieldBubbleLayer extends GeoRenderLayer<SupremeEvoker> {

    private static final ResourceLocation MESH = new ResourceLocation(DungeonsIllagersMod.MOD_ID, "models/entity/shield_bubble.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonsIllagersMod.MOD_ID, "textures/entity/shield_bubble.png");
    private static final float R = 0.67F, G = 0.0F, B = 0.91F;
    private float[][][] triangles;

    ShieldBubbleLayer(GeoRenderer<SupremeEvoker> renderer) {
        super(renderer);
    }

    private float[][][] triangles() {
        if (this.triangles == null) {
            try (Reader reader = new InputStreamReader(
                    Minecraft.getInstance().getResourceManager().open(MESH), StandardCharsets.UTF_8)) {
                JsonArray tris = JsonParser.parseReader(reader).getAsJsonArray();
                this.triangles = new float[tris.size()][3][3];
                for (int t = 0; t < tris.size(); t++) {
                    for (int v = 0; v < 3; v++) {
                        JsonArray p = tris.get(t).getAsJsonArray().get(v).getAsJsonArray();
                        for (int c = 0; c < 3; c++) {
                            this.triangles[t][v][c] = p.get(c).getAsFloat();
                        }
                    }
                }
            } catch (Exception e) {
                this.triangles = new float[0][][];
            }
        }
        return this.triangles;
    }

    @Override
    public void render(PoseStack poseStack, SupremeEvoker animatable, BakedGeoModel bakedModel, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        float left = animatable.shieldLeft();
        if (left <= 0) {
            return;
        }
        float shimmer = 0.04F * (float) Math.sin((animatable.tickCount + partialTick) * 0.25F);
        float alpha = 0.18F + 0.17F * Math.min(1.0F, left / 10.0F) + shimmer;
        VertexConsumer out = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        for (float[][] tri : triangles()) {
            Vector3f a = new Vector3f(tri[0]), b = new Vector3f(tri[1]), c = new Vector3f(tri[2]);
            Vector3f n = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
            // quads: the triangle with its last corner doubled
            for (Vector3f p : new Vector3f[] {a, b, c, c}) {
                out.vertex(pose, p.x, p.y, p.z).color(R, G, B, alpha).uv(0.5F, 0.5F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(normal, n.x, n.y, n.z).endVertex();
            }
        }
    }
}
