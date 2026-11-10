package com.rendertweaks.lionfish;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.IMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Lionfish API (Cataclysm) model boxes through Accelerated Rendering: each box's vertices are
 * written once, in the box's own space, into a mesh AR keeps on the GPU; after that every frame
 * only hands AR the pose, and AR's compute shader transforms the vertices.
 *
 * <p>This mirrors AR 1.0.14's own acceleration of vanilla {@code ModelPart} step by step: same
 * feature switches (entity acceleration on, accelerated pipeline, "accelerate vanilla" models, level
 * or allowed GUI rendering, accelerated buffer), same vertex layout (positions in blocks, white
 * vertex colour, the box UVs and normals), same per-buffer mesh cache and identical-mesh merging,
 * same colour packing. Boxes are Lionfish's own ModelPart equivalent, so they follow AR's "vanilla
 * models" switch.
 *
 * <p>Only loaded when Accelerated Rendering is installed ({@link AcceleratedRenderingSupport}).
 */
public final class AcceleratedLionfish {

    private AcceleratedLionfish() {}

    /**
     * Draws the boxes through AR and returns true, or returns false (nothing drawn) when AR would
     * not accelerate this draw or the box data cannot be read.
     */
    public static boolean render(
            AcceleratedBoxHolder holder,
            Iterable<?> boxes,
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha) {
        IAcceleratedVertexConsumer accelerated = VertexConsumerExtension.getAccelerated(consumer);
        if (accelerated == null
                || !AcceleratedEntityRenderingFeature.isEnabled()
                || !AcceleratedEntityRenderingFeature.shouldUseAcceleratedPipeline()
                || !ModsFeature.isEnabled()
                || !ModsFeature.shouldAccelerateVanilla()) {
            return false;
        }
        if (!CoreFeature.isRenderingLevel()
                && !(CoreFeature.isRenderingGui() && AcceleratedEntityRenderingFeature.shouldAccelerateInGui())) {
            return false;
        }
        if (!accelerated.isAccelerated() || !LionfishFastRender.prepare(boxes)) {
            return false;
        }
        BoxRenderer renderer = (BoxRenderer) holder.renderTweaks$acceleratedRenderer();
        if (renderer == null) {
            renderer = new BoxRenderer(boxes);
            holder.renderTweaks$setAcceleratedRenderer(renderer);
        }
        int color = FastColor.ARGB32.color((int) (alpha * 255.0F), (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
        accelerated.doRender(renderer, null, pose.pose(), pose.normal(), light, overlay, color);
        return true;
    }

    /** One model box's GPU meshes, one per kind of buffer it was drawn into. */
    private static final class BoxRenderer implements IAcceleratedRenderer<Void> {

        private final Iterable<?> boxes;
        private final Map<IBufferGraph, IMesh> meshes = new Object2ObjectOpenHashMap<>();
        private final Map<MeshData, IMesh> merges = new Object2ObjectOpenHashMap<>();

        private BoxRenderer(Iterable<?> boxes) {
            this.boxes = boxes;
        }

        @Override
        public void render(VertexConsumer consumer, Void context, Matrix4f pose, Matrix3f normal, int light, int overlay, int color) {
            IAcceleratedVertexConsumer accelerated = VertexConsumerExtension.getAccelerated(consumer);
            IMesh mesh = meshes.get(accelerated);
            accelerated.beginTransform(pose, normal);
            if (mesh == null) {
                mesh = build(accelerated, overlay);
                meshes.put(accelerated, mesh);
            }
            mesh.write(accelerated, color, light, overlay);
            accelerated.endTransform();
        }

        private IMesh build(IAcceleratedVertexConsumer accelerated, int overlay) {
            IMeshCollector collector = CoreFeature.createMeshCollector(accelerated);
            VertexConsumer out = accelerated.decorate(collector);
            for (Object box : boxes) {
                float[] data = ((LionfishBoxCache) box).renderTweaks$data();
                int o = 0;
                int quads = data.length / LionfishBoxData.FLOATS_PER_QUAD;
                for (int q = 0; q < quads; q++) {
                    float nx = data[o];
                    float ny = data[o + 1];
                    float nz = data[o + 2];
                    o += 3;
                    for (int v = 0; v < 4; v++) {
                        out.vertex(
                                data[o], data[o + 1], data[o + 2],
                                1.0F, 1.0F, 1.0F, 1.0F,
                                data[o + 3], data[o + 4],
                                overlay, 0,
                                nx, ny, nz);
                        o += LionfishBoxData.FLOATS_PER_VERTEX;
                    }
                }
            }
            collector.flush();
            MeshData data = collector.getData();
            ByteBufferBuilder buffer = collector.getBuffer();
            IMesh mesh = merges.get(data);
            if (mesh != null) {
                buffer.close();
                return mesh;
            }
            mesh = AcceleratedEntityRenderingFeature.getMeshType().getBuilder().build(collector);
            merges.put(data, mesh);
            return mesh;
        }
    }
}
