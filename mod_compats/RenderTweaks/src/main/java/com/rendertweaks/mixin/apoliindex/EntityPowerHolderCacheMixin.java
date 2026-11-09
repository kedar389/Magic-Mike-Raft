package com.rendertweaks.mixin.apoliindex;

import com.rendertweaks.apoli.PowerHolderCache;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityPowerHolderCacheMixin implements PowerHolderCache {

    @Unique
    private Object renderTweaks$powerHolder;

    @Override
    public Object renderTweaks$powerHolder() {
        return renderTweaks$powerHolder;
    }

    @Override
    public void renderTweaks$setPowerHolder(Object holder) {
        renderTweaks$powerHolder = holder;
    }
}
