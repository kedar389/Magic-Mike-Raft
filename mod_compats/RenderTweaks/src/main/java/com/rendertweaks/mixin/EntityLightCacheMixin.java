package com.rendertweaks.mixin;

import com.rendertweaks.light.EntityLightHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityLightCacheMixin implements EntityLightHolder {

    @Unique
    private int renderTweaks$skyFrame;

    @Unique
    private long renderTweaks$skyPos;

    @Unique
    private int renderTweaks$skyValue;

    @Unique
    private int renderTweaks$blockFrame;

    @Unique
    private long renderTweaks$blockPos;

    @Unique
    private int renderTweaks$blockValue;

    @Override
    public int renderTweaks$lightFrame(boolean sky) {
        return sky ? renderTweaks$skyFrame : renderTweaks$blockFrame;
    }

    @Override
    public long renderTweaks$lightPos(boolean sky) {
        return sky ? renderTweaks$skyPos : renderTweaks$blockPos;
    }

    @Override
    public int renderTweaks$lightValue(boolean sky) {
        return sky ? renderTweaks$skyValue : renderTweaks$blockValue;
    }

    @Override
    public void renderTweaks$setLight(boolean sky, int frame, long pos, int value) {
        if (sky) {
            renderTweaks$skyFrame = frame;
            renderTweaks$skyPos = pos;
            renderTweaks$skyValue = value;
        } else {
            renderTweaks$blockFrame = frame;
            renderTweaks$blockPos = pos;
            renderTweaks$blockValue = value;
        }
    }
}
