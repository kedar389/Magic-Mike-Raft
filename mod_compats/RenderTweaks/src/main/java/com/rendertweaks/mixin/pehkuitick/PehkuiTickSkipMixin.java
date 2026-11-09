package com.rendertweaks.mixin.pehkuitick;

import com.bawnorton.mixinsquared.TargetHandler;
import com.rendertweaks.pehkui.PehkuiDefaults;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs in front of Pehkui 3.8.2 {@code EntityMixin.pehkui$tick}, its inject at the head of
 * {@code Entity.tick}: every tick, for every entity, it fetches (creating if needed) the entity's
 * data for every registered scale type and ticks it. For an entity whose scales are all default,
 * none of that changes anything; it is skipped on the client. Reached through MixinSquared, so the
 * priority is above Pehkui's default 1000.
 */
@Mixin(value = Entity.class, priority = 1500, remap = false)
public abstract class PehkuiTickSkipMixin {

    @TargetHandler(mixin = "virtuoel.pehkui.mixin.EntityMixin", name = "pehkui$tick")
    // require = 0: if a Pehkui update renames the handler, skip this tweak instead of failing startup.
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderTweaks$skipDefaultScaleTick(CallbackInfo pehkuiCi, CallbackInfo ci) {
        if (PehkuiDefaults.canSkipTick((Entity) (Object) this)) {
            ci.cancel();
        }
    }
}
