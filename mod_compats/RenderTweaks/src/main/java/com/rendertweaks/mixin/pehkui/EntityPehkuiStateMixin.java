package com.rendertweaks.mixin.pehkui;

import com.rendertweaks.pehkui.PehkuiStateHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityPehkuiStateMixin implements PehkuiStateHolder {

    @Unique
    private volatile int renderTweaks$pehkuiState;

    @Override
    public int renderTweaks$pehkuiState() {
        return renderTweaks$pehkuiState;
    }

    @Override
    public void renderTweaks$setPehkuiState(int state) {
        renderTweaks$pehkuiState = state;
    }
}
