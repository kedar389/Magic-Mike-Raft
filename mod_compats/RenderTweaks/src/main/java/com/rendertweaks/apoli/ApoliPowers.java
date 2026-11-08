package com.rendertweaks.apoli;

import com.mojang.logging.LogUtils;
import com.rendertweaks.FrameCounter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

/**
 * Apoli 2.9.2 (Fabric, through Connector) checks two power lists for every render layer of every
 * mob, every frame: {@code InvisibilityPower} and {@code PreventFeatureRenderPower}. Each check is
 * a component lookup, a fresh {@code LinkedList} and a stream, almost always over an empty list:
 * about 2% of the frame.
 *
 * <p>This answers "does this entity have either power?" once per entity per frame. When it has
 * neither, the layer is rendered directly, which is what Apoli would do. When it has one, Apoli's
 * own check runs unchanged.
 *
 * <p>Apoli is called by reflection, once per entity per frame, so there is no compile dependency on
 * its Fabric-named jar.
 */
public final class ApoliPowers {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean resolved;
    private static boolean available;
    private static MethodHandle getPowers;
    private static Class<?> invisibilityPower;
    private static Class<?> preventFeatureRenderPower;

    private ApoliPowers() {}

    /** True if the entity has no Invisibility or Prevent Feature Render power right now. */
    public static boolean isPlain(Entity entity) {
        if (!resolved) {
            resolve();
        }
        if (!available) {
            return false;
        }
        ApoliCacheHolder holder = (ApoliCacheHolder) entity;
        int frame = FrameCounter.current();
        if (holder.renderTweaks$apoliFrame() != frame) {
            holder.renderTweaks$setApoli(frame, lookup(entity));
        }
        return holder.renderTweaks$apoliPlain();
    }

    private static boolean lookup(Entity entity) {
        try {
            List<?> invisibility = (List<?>) getPowers.invokeExact(entity, invisibilityPower);
            if (!invisibility.isEmpty()) {
                return false;
            }
            List<?> prevent = (List<?>) getPowers.invokeExact(entity, preventFeatureRenderPower);
            return prevent.isEmpty();
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    private static void resolve() {
        resolved = true;
        try {
            Class<?> component = Class.forName("io.github.apace100.apoli.component.PowerHolderComponent");
            invisibilityPower = Class.forName("io.github.apace100.apoli.power.InvisibilityPower");
            preventFeatureRenderPower = Class.forName("io.github.apace100.apoli.power.PreventFeatureRenderPower");
            getPowers = MethodHandles.publicLookup()
                    .findStatic(component, "getPowers", MethodType.methodType(List.class, Entity.class, Class.class));
            available = true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            disable(e);
        }
    }

    private static void disable(Throwable cause) {
        available = false;
        LOGGER.warn("Apoli power lookup unavailable, Apoli layer check fast path disabled", cause);
    }
}
