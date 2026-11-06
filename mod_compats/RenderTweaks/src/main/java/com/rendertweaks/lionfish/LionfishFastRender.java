package com.rendertweaks.lionfish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.rendertweaks.Tweak;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

/**
 * Lionfish API's {@code AdvancedModelBox.doRender} (Cataclysm's models) emits each vertex through
 * {@code VertexConsumer.vertex}, and under Oculus every call goes through the extended-format
 * vertex path one at a time: 6.5% of the frame with 8 Cataclysm mobs in view. This writes each box
 * in one batch through Embeddium's vertex writer, the same path Embeddium uses for vanilla models.
 */
public final class LionfishFastRender {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Turned off for the session if Lionfish's model data cannot be read. */
    private static boolean enabled = true;

    private LionfishFastRender() {}

    /**
     * Returns false when the caller must fall back to Lionfish's own path: the consumer is not a
     * buffer Embeddium can write into, or the fast path is disabled.
     */
    public static boolean render(
            Iterable<?> boxes,
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha) {
        if (!enabled || !Tweak.CATACLYSM.isEnabled()) {
            return false;
        }
        VertexBufferWriter writer = VertexBufferWriter.tryOf(consumer);
        if (writer == null) {
            return false;
        }
        if (!prepare(boxes)) {
            return false;
        }
        Matrix4f position = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        int color = ColorABGR.pack(red, green, blue, alpha);
        for (Object box : boxes) {
            float[] data = ((LionfishBoxCache) box).renderTweaks$data();
            int quadCount = data.length / LionfishBoxData.FLOATS_PER_QUAD;
            if (quadCount == 0) {
                continue;
            }
            int vertexCount = quadCount * 4;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                long buffer = stack.nmalloc(vertexCount * ModelVertex.STRIDE);
                long ptr = buffer;
                int o = 0;
                for (int q = 0; q < quadCount; q++) {
                    int normal = MatrixHelper.transformNormal(normalMatrix, data[o], data[o + 1], data[o + 2]);
                    o += 3;
                    for (int v = 0; v < 4; v++) {
                        float x = data[o];
                        float y = data[o + 1];
                        float z = data[o + 2];
                        ModelVertex.write(
                                ptr,
                                MatrixHelper.transformPositionX(position, x, y, z),
                                MatrixHelper.transformPositionY(position, x, y, z),
                                MatrixHelper.transformPositionZ(position, x, y, z),
                                color,
                                data[o + 3],
                                data[o + 4],
                                overlay,
                                light,
                                normal);
                        ptr += ModelVertex.STRIDE;
                        o += LionfishBoxData.FLOATS_PER_VERTEX;
                    }
                }
                writer.push(stack, buffer, vertexCount, ModelVertex.FORMAT);
            }
        }
        return true;
    }

    /**
     * Builds any box data not built yet, before anything is written, so a failure falls back
     * cleanly instead of leaving a half-drawn part. False if a box could not be read.
     */
    private static boolean prepare(Iterable<?> boxes) {
        for (Object box : boxes) {
            LionfishBoxCache cache = (LionfishBoxCache) box;
            if (cache.renderTweaks$data() != null) {
                continue;
            }
            try {
                cache.renderTweaks$setData(LionfishBoxData.build(box));
            } catch (ReflectiveOperationException | RuntimeException e) {
                enabled = false;
                LOGGER.warn("Lionfish model data unreadable, batched Cataclysm model rendering disabled", e);
                return false;
            }
        }
        return true;
    }
}
