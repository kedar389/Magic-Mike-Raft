package com.rendertweaks.mixin.apoli;

import com.rendertweaks.apoli.ApoliCacheHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityApoliCacheMixin implements ApoliCacheHolder {

    @Unique
    private int renderTweaks$apoliFrame;

    @Unique
    private boolean renderTweaks$apoliPlain;

    @Override
    public int renderTweaks$apoliFrame() {
        return renderTweaks$apoliFrame;
    }

    @Override
    public boolean renderTweaks$apoliPlain() {
        return renderTweaks$apoliPlain;
    }

    @Override
    public void renderTweaks$setApoli(int frame, boolean plain) {
        renderTweaks$apoliFrame = frame;
        renderTweaks$apoliPlain = plain;
    }
}
