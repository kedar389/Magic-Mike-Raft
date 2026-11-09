package com.rendertweaks.pehkui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.rendertweaks.Tweak;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.PehkuiEntityExtensions;

/**
 * Pehkui 3.8.2 computes every scale from scratch on the client: {@code getTypedScale} looks up the
 * entity's scale data, interpolates its base scale and walks its modifiers, each of which looks up
 * and computes another scale the same way (model width, then width, then base). Mob rendering, its
 * shadow pass and entity searches ask several times per mob per frame: about 3.8% of the frame with
 * 300 entities, nearly all for mobs whose scales were never changed.
 *
 * <p>When every scale of an entity is at its default (Pehkui's own {@code hasDefaultValues}: base,
 * previous, initial and target scale, no transition, no extra modifiers, no custom easing), each
 * scale type yields one constant for every such entity, whatever the partial tick: Pehkui's default
 * modifiers only multiply by other default scales or clamp to the configured range. That constant
 * is learned from Pehkui's own result the first time, then returned at the entry point for such
 * entities, skipping the recursion.
 *
 * <p>Safety: client render thread only (the server and other threads always run Pehkui). The
 * per-entity answer is dropped whenever Pehkui drops its own cache for any of the entity's scales
 * (every tick, every change, every load from save or packet). Every 1024th shortcut call runs
 * Pehkui instead and compares; a mismatch turns the shortcut off for the session and logs it.
 * Dwarf, Elf and other resized entities always take Pehkui's normal path.
 *
 * <p>The same state lets Pehkui's per-tick loop be skipped for all-default entities on the client
 * ({@link #canSkipTick}): ticking a default scale changes nothing. Skipping also skips Pehkui's
 * per-tick cache drop, so the state is rechecked every {@link #RECHECK_TICKS} ticks regardless, to
 * catch a scale changed by code that bypassed Pehkui's update call.
 */
public final class PehkuiDefaults {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VERIFY_EVERY = 1024;
    private static final int RECHECK_TICKS = 20;

    private static final Map<ScaleType, Float> DEFAULT_SCALES = new IdentityHashMap<>();
    private static boolean disabled;
    private static int calls;

    private PehkuiDefaults() {}

    /** The known default scale for this type if the entity qualifies, else NaN (run Pehkui). */
    public static float shortcut(Entity entity, ScaleType type) {
        if (disabled || entity == null || !Tweak.PEHKUI.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return Float.NaN;
        }
        Float known = DEFAULT_SCALES.get(type);
        if (known == null || !allDefault(entity)) {
            return Float.NaN;
        }
        if (++calls % VERIFY_EVERY == 0) {
            return Float.NaN;
        }
        return known;
    }

    /** Called with Pehkui's own result: learns the default scale, or checks it against what was learned. */
    public static void observe(Entity entity, ScaleType type, float scale) {
        if (disabled || entity == null || !Tweak.PEHKUI.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return;
        }
        if (!allDefault(entity)) {
            return;
        }
        Float known = DEFAULT_SCALES.get(type);
        if (known == null) {
            DEFAULT_SCALES.put(type, scale);
        } else if (Float.compare(known, scale) != 0) {
            disabled = true;
            LOGGER.warn(
                    "Pehkui default scale for {} changed from {} to {}; default-scale shortcut disabled for this session",
                    type, known, scale);
        }
    }

    /**
     * True if Pehkui's per-tick scale loop can be skipped for this entity: client render thread,
     * all scales at their defaults (rechecked every {@link #RECHECK_TICKS} ticks).
     */
    public static boolean canSkipTick(Entity entity) {
        if (disabled || entity == null || !Tweak.PEHKUI.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return false;
        }
        if (entity.tickCount % RECHECK_TICKS == 0) {
            invalidate(entity);
        }
        return allDefault(entity);
    }

    /** Pehkui dropped its cache for one of this entity's scales: its answer must be recomputed. */
    public static void invalidate(Entity entity) {
        if (entity instanceof PehkuiStateHolder holder) {
            holder.renderTweaks$setPehkuiState(PehkuiStateHolder.UNKNOWN);
        }
    }

    private static boolean allDefault(Entity entity) {
        PehkuiStateHolder holder = (PehkuiStateHolder) entity;
        int state = holder.renderTweaks$pehkuiState();
        if (state == PehkuiStateHolder.UNKNOWN) {
            state = compute(entity);
            holder.renderTweaks$setPehkuiState(state);
        }
        return state == PehkuiStateHolder.ALL_DEFAULT;
    }

    private static int compute(Entity entity) {
        if (entity.level() == null || !entity.level().isClientSide) {
            return PehkuiStateHolder.CHANGED;
        }
        Map<ScaleType, ScaleData> scales = ((PehkuiEntityExtensions) entity).pehkui_getScales();
        if (scales == null) {
            return PehkuiStateHolder.ALL_DEFAULT;
        }
        try {
            for (ScaleData data : scales.values()) {
                if (!data.hasDefaultValues()) {
                    return PehkuiStateHolder.CHANGED;
                }
            }
            return PehkuiStateHolder.ALL_DEFAULT;
        } catch (ConcurrentModificationException e) {
            // Another thread added a scale while reading; answer "not default" and recheck next time.
            return PehkuiStateHolder.UNKNOWN;
        }
    }
}
