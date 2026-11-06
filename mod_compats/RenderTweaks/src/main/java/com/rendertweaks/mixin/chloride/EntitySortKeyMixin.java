package com.rendertweaks.mixin.chloride;

import com.rendertweaks.chloride.SortKeyHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, remap = false)
public abstract class EntitySortKeyMixin implements SortKeyHolder {

    @Unique
    private int renderTweaks$sortFrame;

    @Unique
    private double renderTweaks$sortKey;

    @Override
    public int renderTweaks$sortFrame() {
        return renderTweaks$sortFrame;
    }

    @Override
    public double renderTweaks$sortKey() {
        return renderTweaks$sortKey;
    }

    @Override
    public void renderTweaks$setSortKey(int frame, double key) {
        renderTweaks$sortFrame = frame;
        renderTweaks$sortKey = key;
    }
}
