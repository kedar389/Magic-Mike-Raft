package com.ironscompanions.mixin;

import com.ironscompanions.spell.SpellTag;
import dev.xylonity.companions.config.CompanionsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales the heal ring with the scroll that cast it, instead of the flat
 * {@code CompanionsConfig.HEAL_RING_HEALING} the projectile reads for itself.
 *
 * <p>{@code HealRingProjectile} reads that field twice in its tick, once to heal the living and once to
 * hurt the undead, so redirecting the field read covers both. A ring carrying a {@link SpellTag} answers
 * with the spell's power for its owner; one cast by a Companions mob keeps the config amount.
 *
 * <p>{@code remap = false} with the SRG name: {@code m_8119_} is {@code Entity#tick}, which the target
 * overrides, and the target is a Companions class.
 */
@Mixin(targets = "dev.xylonity.companions.common.entity.projectile.HealRingProjectile", remap = false)
public class HealRingScalingMixin {

    @Redirect(method = "m_8119_()V",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
                    target = "Ldev/xylonity/companions/config/CompanionsConfig;HEAL_RING_HEALING:D"))
    private double ironsCompanions$scaleWithSpell() {
        Entity self = (Entity) (Object) this;
        SpellTag tag = SpellTag.read(self);
        if (tag == null) {
            return CompanionsConfig.HEAL_RING_HEALING;
        }
        Entity owner = self instanceof Projectile projectile ? projectile.getOwner() : null;
        return tag.power(owner == null ? self : owner);
    }
}
