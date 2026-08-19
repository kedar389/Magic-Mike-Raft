package com.ironscompanions.mixin;

import com.ironscompanions.spell.NaginataShockwave;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives the Naginata volley a shockwave: sparkles and splash damage wherever a blade lands, whether it
 * hits a mob or the ground.
 *
 * <p>The projectile is a {@code ThrownTrident} whose hitbox is a single blade, and it deals damage from
 * its own hit methods, so there is no event to widen. {@link NaginataShockwave} ignores blades this mod did
 * not spawn, leaving the Sacred Pontiff's naginatas alone.
 *
 * <p>{@code remap = false} with SRG names, as in {@link SoulMageBookSlotMixin}: {@code m_8060_} is
 * {@code onHitBlock} and {@code m_5790_} is {@code onHitEntity}.
 */
// Mixin injectors are called by injected bytecode, never from Java, and their signature is fixed by
// the target method, so unused handlers and unread parameters are expected here rather than dead code.
@SuppressWarnings({"UnusedMethod", "UnusedVariable"})
@Mixin(targets = "dev.xylonity.companions.common.entity.projectile.HolinessNaginataProjectile", remap = false)
public class HolinessNaginataMixin {

    @Inject(method = "m_8060_(Lnet/minecraft/world/phys/BlockHitResult;)V", at = @At("TAIL"))
    private void ironsCompanions$groundImpact(BlockHitResult hit, CallbackInfo ci) {
        NaginataShockwave.onImpact((Entity) (Object) this, null);
    }

    @Inject(method = "m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At("TAIL"))
    private void ironsCompanions$entityImpact(EntityHitResult hit, CallbackInfo ci) {
        NaginataShockwave.onImpact((Entity) (Object) this, hit.getEntity());
    }
}
