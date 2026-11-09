package com.rendertweaks.mixin.pehkui;

import com.rendertweaks.pehkui.PehkuiDefaults;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.ScaleData;

/**
 * Pehkui calls {@code invalidateCachedScales} from {@code tick}, {@code onUpdate} (every setter and
 * NBT/packet load), {@code resetScale} and {@code fromScale}: every way a scale can change.
 */
@Mixin(value = ScaleData.class, remap = false)
public abstract class ScaleDataInvalidateMixin {

    @Shadow
    public abstract Entity getEntity();

    @Inject(method = "invalidateCachedScales()V", at = @At("TAIL"))
    private void renderTweaks$forgetDefaultState(CallbackInfo ci) {
        PehkuiDefaults.invalidate(getEntity());
    }
}
