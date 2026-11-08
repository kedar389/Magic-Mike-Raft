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

    @Unique
    private int renderTweaks$glowFrame;

    @Unique
    private boolean renderTweaks$selfGlow;

    @Override
    public int renderTweaks$glowFrame() {
        return renderTweaks$glowFrame;
    }

    @Override
    public boolean renderTweaks$selfGlow() {
        return renderTweaks$selfGlow;
    }

    @Override
    public void renderTweaks$setGlow(int frame, boolean selfGlow) {
        renderTweaks$glowFrame = frame;
        renderTweaks$selfGlow = selfGlow;
    }

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
