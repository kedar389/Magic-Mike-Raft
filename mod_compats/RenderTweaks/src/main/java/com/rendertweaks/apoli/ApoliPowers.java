package com.rendertweaks.apoli;

import com.mojang.logging.LogUtils;
import com.rendertweaks.FrameCounter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
 * <p>Apoli also checks, for every entity about to render, whether the local player has a
 * {@code PreventEntityRenderPower} (same stream pattern). That answer is per player, so it is
 * looked up once per frame.
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
    private static Class<?> preventEntityRenderPower;

    private static int playerFrame;
    private static boolean playerMayHide;

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

    /**
     * True unless the local player is known to have no Prevent Entity Render power this frame. True
     * also when it cannot be told, so Apoli's own check runs.
     */
    public static boolean playerMayHideEntities() {
        if (!resolved) {
            resolve();
        }
        if (!available) {
            return true;
        }
        int frame = FrameCounter.current();
        if (playerFrame != frame) {
            Player player = Minecraft.getInstance().player;
            playerMayHide = player == null || hasAny(player, preventEntityRenderPower);
            playerFrame = frame;
        }
        return playerMayHide;
    }

    private static boolean hasAny(Entity entity, Class<?> power) {
        try {
            return !((List<?>) getPowers.invokeExact(entity, power)).isEmpty();
        } catch (Throwable t) {
            disable(t);
            return true;
        }
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
            preventEntityRenderPower = Class.forName("io.github.apace100.apoli.power.PreventEntityRenderPower");
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
