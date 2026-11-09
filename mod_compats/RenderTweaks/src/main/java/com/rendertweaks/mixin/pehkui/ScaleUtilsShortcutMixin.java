package com.rendertweaks.mixin.pehkui;

import com.rendertweaks.pehkui.PehkuiDefaults;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * {@code ScaleUtils.getTypedScale(Entity, ScaleType, float)} is the entry point every Pehkui scale
 * query goes through (model, hitbox, interaction box, reach, motion...). One shortcut here skips the
 * whole recursive computation below it for all-default entities.
 */
@Mixin(value = ScaleUtils.class, remap = false)
public abstract class ScaleUtilsShortcutMixin {

    private static final String GET_TYPED_SCALE =
            "getTypedScale(Lnet/minecraft/world/entity/Entity;Lvirtuoel/pehkui/api/ScaleType;F)F";

    @Inject(method = GET_TYPED_SCALE, at = @At("HEAD"), cancellable = true)
    private static void renderTweaks$defaultScale(Entity entity, ScaleType type, float delta, CallbackInfoReturnable<Float> cir) {
        float scale = PehkuiDefaults.shortcut(entity, type);
        if (!Float.isNaN(scale)) {
            cir.setReturnValue(scale);
        }
    }

    @Inject(method = GET_TYPED_SCALE, at = @At("RETURN"))
    private static void renderTweaks$observeScale(Entity entity, ScaleType type, float delta, CallbackInfoReturnable<Float> cir) {
        PehkuiDefaults.observe(entity, type, cir.getReturnValueF());
    }
}
