package com.rendertweaks.gtbc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rendertweaks.Tweak;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * GTBC Spell Lib 2.2.0 searches all entities in a 100-block box around the player every frame, four
 * times: screen-flash and power-inversion entities from its HUD overlay, screen-shake entities from
 * its camera hook. About 1.1% of the frame. These effect entities only appear or disappear on game
 * ticks, so the answer is reused for the rest of the tick (20 searches a second instead of one per
 * frame each). In this pack only Geomancy Plus uses the library, and only for screen shake.
 */
public final class TickEntityQueries {

    private static final Map<Class<?>, Result> RESULTS = new IdentityHashMap<>();

    private TickEntityQueries() {}

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> entitiesOfClass(Level level, Class<T> type, AABB box) {
        if (!Tweak.GTBC.isEnabled() || !RenderSystem.isOnRenderThread()) {
            return level.getEntitiesOfClass(type, box);
        }
        long time = level.getGameTime();
        Result cached = RESULTS.get(type);
        if (cached != null && cached.level == level && cached.time == time) {
            return (List<T>) cached.entities;
        }
        List<T> entities = level.getEntitiesOfClass(type, box);
        RESULTS.put(type, new Result(level, time, entities));
        return entities;
    }

    /** Drops every cached search, so an unloaded level and its entities are not kept. */
    public static void clear() {
        RESULTS.clear();
    }

    private record Result(Level level, long time, List<?> entities) {}
}
