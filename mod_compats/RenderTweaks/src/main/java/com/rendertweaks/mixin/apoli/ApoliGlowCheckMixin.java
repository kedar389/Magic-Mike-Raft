package com.rendertweaks.mixin.apoli;

import com.bawnorton.mixinsquared.TargetHandler;
import com.rendertweaks.Tweak;
import com.rendertweaks.apoli.ApoliPowers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runs in front of Apoli's {@code MinecraftClientMixin.makeEntitiesGlow}, its inject into
 * {@code Minecraft.shouldEntityAppearGlowing}. That handler only makes an entity glow when the
 * player has an Entity Glow power, or the entity (a living one) has a Self Glow power. With
 * neither, it is skipped, which leaves the vanilla answer exactly as Apoli would.
 */
@Mixin(value = Minecraft.class, priority = 1500, remap = false)
public abstract class ApoliGlowCheckMixin {

    @TargetHandler(mixin = "io.github.apace100.apoli.mixin.MinecraftClientMixin", name = "makeEntitiesGlow")
    // require = 0: if an Apoli update renames the handler, skip this tweak instead of failing startup.
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    private void renderTweaks$skipWithoutGlowPower(Entity entity, CallbackInfoReturnable<Boolean> apoliCir, CallbackInfo ci) {
        if (!Tweak.APOLI.isEnabled()) {
            return;
        }
        boolean selfGlow = entity instanceof LivingEntity && ApoliPowers.mayGlowItself(entity);
        if (!selfGlow && !ApoliPowers.playerMayMakeEntitiesGlow()) {
            ci.cancel();
        }
    }
}
