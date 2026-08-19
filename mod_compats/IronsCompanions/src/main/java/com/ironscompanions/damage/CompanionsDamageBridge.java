package com.ironscompanions.damage;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.ModIds;
import com.ironscompanions.spell.SpellTag;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Runs Companions damage through Iron's Spells by cancelling the hit and re-dealing it. A hit from one
 * of this mod's spell projectiles is re-dealt as that spell, so it scales off the caster rather than the
 * Companions config value; a hit from a Companions mob or weapon projectile is retyped into the matching
 * school, so spell resistance applies.
 *
 * <p>{@link LivingAttackEvent} and not {@code LivingHurtEvent}: the latter fires after
 * {@code Entity.hurt} has set {@code invulnerableTime = 20}, so a re-deal of the same amount lands in
 * the "already invulnerable" branch and is discarded.
 *
 * <p>Hot path: this sees every hit dealt in the world. Checks run cheapest-first and nothing allocates
 * before a hit is known to be ours — an entity-type lookup gates reading persistent data (see
 * {@link SpellTag}), {@link CompanionsSchools} answers from a resolved type table, and the re-entrancy
 * guard is read only after a match.
 */
public final class CompanionsDamageBridge {

    /**
     * Our re-deal posts this event again, so without a guard it recurses until the stack runs out.
     * {@link ThreadLocal} because the integrated server and the client run on different threads.
     */
    private static final ThreadLocal<Boolean> REDEALING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private CompanionsDamageBridge() {
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        Entity direct = source.getDirectEntity();

        if (direct != null) {
            SpellTag tag = SpellTag.read(direct);
            if (tag != null) {
                redealAsSpell(event, target, source, direct, tag);
                return;
            }
        }
        ResourceLocation school = CompanionsSchools.schoolFor(direct, source.getEntity());
        if (school != null) {
            retype(event, target, source, direct, school);
        }
    }

    /**
     * Re-deals through {@code DamageSources.applyDamage} with a real {@code SpellDamageSource}, the call
     * every stock Iron's Spells spell makes. Spell power, school resistance, {@code SpellDamageEvent},
     * knockback, friendly fire and the spell's own death message all follow from it.
     */
    private static void redealAsSpell(LivingAttackEvent event, LivingEntity target, DamageSource source,
                                      Entity projectile, SpellTag tag) {
        if (REDEALING.get()) {
            return; // this is our own re-deal coming back around; let it land
        }
        Entity caster = source.getEntity();
        float amount = tag.power(caster);
        event.setCanceled(true);
        redealing(() -> DamageSources.applyDamage(target, amount, tag.spell().getDamageSource(projectile, caster)));
    }

    /** Re-deals the hit unchanged in amount, under the school's own damage type. */
    private static void retype(LivingAttackEvent event, LivingEntity target, DamageSource source,
                               Entity direct, ResourceLocation school) {
        if (REDEALING.get() || isAlreadyMagic(source)) {
            return;
        }
        DamageSource magic = schoolDamage(target.level(), school, source.getEntity(), direct);
        if (magic == null) {
            return; // the school or its damage type is missing; leave the hit untouched
        }
        float amount = event.getAmount();
        event.setCanceled(true);
        redealing(() -> target.hurt(magic, amount));
    }

    private static void redealing(Runnable hit) {
        REDEALING.set(Boolean.TRUE);
        try {
            hit.run();
        } finally {
            REDEALING.set(Boolean.FALSE);
        }
    }

    /** True for damage Iron's Spells already dealt, which must never be typed a second time. */
    private static boolean isAlreadyMagic(DamageSource source) {
        ResourceKey<DamageType> key = source.typeHolder().unwrapKey().orElse(null);
        return key != null && ModIds.IRONS_SPELLBOOKS.equals(key.location().getNamespace());
    }

    /**
     * Asks the {@link SchoolType} for its damage type instead of assembling
     * {@code irons_spellbooks:<school>_magic}, which only holds for the built-in schools and throws for
     * addon ones such as Geomancy Plus's {@code geo}.
     */
    private static DamageSource schoolDamage(Level level, ResourceLocation schoolId, Entity attacker, Entity direct) {
        SchoolType school = SchoolRegistry.getSchool(schoolId);
        if (school == null) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] No such spell school: {}", schoolId);
            return null;
        }
        ResourceKey<DamageType> typeKey = school.getDamageType();
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolder(typeKey)
                .orElse(null);
        if (holder == null) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] School {} has no damage type {}", schoolId, typeKey);
            return null;
        }
        return new DamageSource(holder, direct, attacker);
    }
}
