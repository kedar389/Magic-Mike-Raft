package com.rendertweaks.chloride;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Arrays;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

/**
 * Chloride 1.8.1's entity limit copies every loaded entity into a list, sorts the whole list with
 * a comparator that reads both entities' positions on every comparison, then keeps the first
 * {@code entityLimit}. With 160 entities that sort is about 2% of the frame, nearly all of it
 * jumping between entity objects in memory.
 *
 * <p>This reads each entity's squared distance to the player once into a primitive array and sorts
 * that array instead. Same entities, same nearest-first order, as Chloride returns.
 */
public final class NearestEntities {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Chloride treats a limit at or above this as "no limit". */
    private static final int NO_LIMIT = 512;

    private static Entity[] entities = new Entity[256];
    private static double[] keys = new double[256];

    private static boolean resolved;
    private static Object cullingConfig;
    private static Field entityLimit;

    private NearestEntities() {}

    /**
     * The entities Chloride would render, or null to let Chloride's own code answer: no limit,
     * a zero limit, no player yet, or its config could not be read.
     */
    public static List<Entity> select(ClientLevel level) {
        int limit = limit();
        Player player = Minecraft.getInstance().player;
        if (limit <= 0 || limit >= NO_LIMIT || player == null) {
            return null;
        }
        int count = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (count == entities.length) {
                entities = java.util.Arrays.copyOf(entities, count * 2);
                keys = java.util.Arrays.copyOf(keys, count * 2);
            }
            entities[count] = entity;
            keys[count] = entity.distanceToSqr(player);
            count++;
        }
        Arrays.quickSort(0, count, NearestEntities::compareKeys, NearestEntities::swap);
        int kept = Math.min(count, limit);
        List<Entity> nearest = new ArrayList<>(kept);
        for (int i = 0; i < kept; i++) {
            nearest.add(entities[i]);
        }
        // Drop the references so unloaded entities are not kept alive until the next frame.
        java.util.Arrays.fill(entities, 0, count, null);
        return nearest;
    }

    private static int compareKeys(int a, int b) {
        return Double.compare(keys[a], keys[b]);
    }

    private static void swap(int a, int b) {
        Entity entity = entities[a];
        entities[a] = entities[b];
        entities[b] = entity;
        double key = keys[a];
        keys[a] = keys[b];
        keys[b] = key;
    }

    /** Chloride's live {@code entityLimit}, read each frame since the config can change in game. */
    private static int limit() {
        if (!resolved) {
            resolve();
        }
        if (entityLimit == null) {
            return -1;
        }
        try {
            return entityLimit.getInt(cullingConfig);
        } catch (IllegalAccessException e) {
            entityLimit = null;
            return -1;
        }
    }

    private static void resolve() {
        resolved = true;
        try {
            Class<?> config = Class.forName("me.srrapero720.chloride.ChlorideConfig");
            cullingConfig = config.getField("culling").get(null);
            entityLimit = cullingConfig.getClass().getField("entityLimit");
        } catch (ReflectiveOperationException | RuntimeException e) {
            entityLimit = null;
            LOGGER.warn("Chloride config unreadable, nearest-entity pick disabled", e);
        }
    }
}
