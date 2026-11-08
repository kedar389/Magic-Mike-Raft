package com.rendertweaks.mixin;

import com.rendertweaks.light.LightCacheHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlockEntity.class, remap = false)
public abstract class BlockEntityLightCacheMixin implements LightCacheHolder {

    @Unique
    private int renderTweaks$lightFrame;

    @Unique
    private int renderTweaks$light;

    @Override
    public int renderTweaks$lightFrame() {
        return renderTweaks$lightFrame;
    }

    @Override
    public int renderTweaks$light() {
        return renderTweaks$light;
    }

    @Override
    public void renderTweaks$setLight(int frame, int light) {
        renderTweaks$lightFrame = frame;
        renderTweaks$light = light;
    }
}
