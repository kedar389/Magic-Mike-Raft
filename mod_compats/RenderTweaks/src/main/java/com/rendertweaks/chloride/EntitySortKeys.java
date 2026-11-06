package com.rendertweaks.chloride;

import com.rendertweaks.Tweak;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Chloride's entity limit sorts every loaded entity by distance to the player each frame, and its
 * comparator recomputes both distances (with a square root) on every comparison: about 3.5% of the
 * frame with 460 entities loaded. This computes each entity's key once per frame instead.
 *
 * <p>Squared distance orders the same as distance, so the sort result is unchanged.
 */
public final class EntitySortKeys {

    /** Bumped at the start of every world render. Starts at 1 so a fresh entity's 0 never matches. */
    private static int frame = 1;

    private EntitySortKeys() {}

    public static void nextFrame() {
        frame++;
    }

    public static int compare(Entity a, Entity b) {
        if (!Tweak.CHLORIDE.isEnabled()) {
            // Chloride's original comparator.
            Player player = Minecraft.getInstance().player;
            return Double.compare(a.distanceTo(player), b.distanceTo(player));
        }
        return Double.compare(key(a), key(b));
    }

    private static double key(Entity entity) {
        SortKeyHolder holder = (SortKeyHolder) entity;
        if (holder.renderTweaks$sortFrame() != frame) {
            Player player = Minecraft.getInstance().player;
            holder.renderTweaks$setSortKey(frame, player == null ? 0.0 : entity.distanceToSqr(player));
        }
        return holder.renderTweaks$sortKey();
    }
}
