package com.rendertweaks.apoli;

import com.mojang.logging.LogUtils;
import com.rendertweaks.Tweak;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

/**
 * Apoli 2.9.2 answers {@code hasPower(entity, type)} by copying all of the entity's powers into a
 * new list and streaming over it, and {@code getPowers(type)} by walking all of them. Apoli's own
 * patches on vanilla methods (on fire, invisible, shaking, fire immune, ...) call these constantly,
 * on both sides: about 2.4% of the client frame, and the same per entity on the server.
 *
 * <p>Both now read a per-entity index of powers by type ({@link PowerTypeIndex}), rebuilt whenever
 * Apoli writes to its power map. Activity ({@code isActive()}) and the optional filter are still
 * checked live on every call, in the same order, so the answers are exactly Apoli's.
 *
 * <p>Apoli is reached by reflection: its jar uses Fabric names, so it cannot be a compile
 * dependency.
 */
public final class IndexedPowers {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean resolved;
    private static MethodHandle componentOf;
    private static MethodHandle isActive;

    private IndexedPowers() {}

    /** Apoli's hasPower, or null to let Apoli answer itself. */
    public static Boolean hasPower(Entity entity, Class<?> type, Predicate<Object> filter) {
        if (!Tweak.APOLI_INDEX.isEnabled()) {
            return null;
        }
        if (!(entity instanceof LivingEntity)) {
            return Boolean.FALSE;
        }
        if (!resolve()) {
            return null;
        }
        try {
            Object component = (Object) componentOf.invokeExact((Object) entity);
            if (!(component instanceof PowerTypeIndexHolder holder)) {
                return null;
            }
            for (Object power : holder.renderTweaks$powersOf(type)) {
                if ((boolean) isActive.invokeExact(power) && (filter == null || filter.test(power))) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        } catch (Throwable t) {
            disable(t);
            return null;
        }
    }

    /**
     * The entity's power index, or null when the index is off, unavailable, or the entity has no
     * power holder.
     */
    public static PowerTypeIndexHolder holderOf(Entity entity) {
        if (!Tweak.APOLI_INDEX.isEnabled() || !(entity instanceof LivingEntity) || !resolve()) {
            return null;
        }
        try {
            Object component = (Object) componentOf.invokeExact((Object) entity);
            return component instanceof PowerTypeIndexHolder holder ? holder : null;
        } catch (Throwable t) {
            disable(t);
            return null;
        }
    }

    /** True if any power of this type is active, checking activity only on powers of that type. */
    public static boolean anyActive(PowerTypeIndexHolder holder, Class<?> type) throws Throwable {
        for (Object power : holder.renderTweaks$powersOf(type)) {
            if ((boolean) isActive.invokeExact(power)) {
                return true;
            }
        }
        return false;
    }

    /** Apoli's getPowers(type, includeInactive), as a fresh list like Apoli's, or null to let Apoli answer. */
    public static List<Object> getPowers(PowerTypeIndexHolder holder, Class<?> type, boolean includeInactive) {
        if (!Tweak.APOLI_INDEX.isEnabled() || !resolve()) {
            return null;
        }
        try {
            List<Object> result = new LinkedList<>();
            for (Object power : holder.renderTweaks$powersOf(type)) {
                if (includeInactive || (boolean) isActive.invokeExact(power)) {
                    result.add(power);
                }
            }
            return result;
        } catch (Throwable t) {
            disable(t);
            return null;
        }
    }

    private static boolean resolve() {
        if (resolved) {
            return componentOf != null;
        }
        resolved = true;
        try {
            Class<?> holderType = Class.forName("io.github.apace100.apoli.component.PowerHolderComponent");
            Object key = holderType.getField("KEY").get(null);
            Class<?> componentType = Class.forName("dev.onyxstudios.cca.api.v3.component.Component");
            MethodHandle get = MethodHandles.publicLookup()
                    .findVirtual(key.getClass(), "get", MethodType.methodType(componentType, Object.class))
                    .bindTo(key);
            Class<?> powerType = Class.forName("io.github.apace100.apoli.power.Power");
            MethodHandle active = MethodHandles.publicLookup()
                    .findVirtual(powerType, "isActive", MethodType.methodType(boolean.class));
            componentOf = get.asType(MethodType.methodType(Object.class, Object.class));
            isActive = active.asType(MethodType.methodType(boolean.class, Object.class));
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            disable(e);
            return false;
        }
    }

    private static void disable(Throwable cause) {
        componentOf = null;
        resolved = true;
        LOGGER.warn("Apoli power index unavailable, Apoli answers power lookups itself", cause);
    }
}
