package com.rendertweaks.lionfish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.rendertweaks.Tweak;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;

/**
 * Gate in front of {@link AcceleratedLionfish}: that class names Accelerated Rendering types, so it
 * is only touched when AR is installed. Any failure turns the AR path off for the session; the
 * Embeddium and vanilla paths keep working.
 */
public final class AcceleratedRenderingSupport {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean enabled = isInstalled();

    private AcceleratedRenderingSupport() {}

    /** True if Accelerated Rendering drew the boxes. */
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
        if (!enabled || !Tweak.CATACLYSM.isEnabled()) {
            return false;
        }
        try {
            return AcceleratedLionfish.render(holder, boxes, pose, consumer, light, overlay, red, green, blue, alpha);
        } catch (LinkageError | RuntimeException e) {
            enabled = false;
            LOGGER.warn("Accelerated Rendering path for Lionfish models failed, using the normal path", e);
            return false;
        }
    }

    private static boolean isInstalled() {
        LoadingModList mods = FMLLoader.getLoadingModList();
        return mods != null && mods.getModFileById("acceleratedrendering") != null;
    }
}
