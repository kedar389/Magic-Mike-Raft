package com.rendertweaks.mixin.chloride;

import com.rendertweaks.chloride.EntitySortKeys;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/** Chloride 1.8.1 {@code EntityCulling.DISTANCE_COMPARATOR} is this lambda. */
@Mixin(targets = "me.srrapero720.chloride.impl.EntityCulling", remap = false)
public abstract class EntityCullingMixin {

    /**
     * @author render_tweaks
     * @reason Same order, but each entity's distance is computed once per frame, not per comparison.
     */
    @Overwrite
    private static int lambda$static$0(Entity a, Entity b) {
        return EntitySortKeys.compare(a, b);
    }
}
