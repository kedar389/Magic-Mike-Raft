package com.ironscompanions.event;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.CompanionsProjectiles;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.Map;
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
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Two jobs, both done by cancelling a hit and re-dealing it:
 * <ul>
 *   <li>Companions ability damage is retyped into the matching Iron's Spells school, so it respects IS
 *       spell resistance and reads as real magic.</li>
 *   <li>Our own spell projectiles are re-dealt through IS itself, so they scale off the caster's spell
 *       power instead of the Companions config value.</li>
 * </ul>
 *
 * <p>This must hook {@link LivingAttackEvent}, not {@code LivingHurtEvent}: the latter fires from
 * {@code actuallyHurt}, after {@code Entity.hurt} has set {@code invulnerableTime = 20} and
 * {@code lastHurt = amount}, so a re-deal of the same amount lands in the "already invulnerable" branch
 * and is discarded. A ThreadLocal guards the recursion.
 */
public class CompanionDamageEvents {

    private static final ThreadLocal<Boolean> RETYPING = ThreadLocal.withInitial(() -> false);

    /**
     * Companions entity id (source or direct) -> IS school name. All ids verified against
     * {@code CompanionsEntities} in the 1.2.3 jar. Note there is no {@code his_holiness} or
     * {@code sacred_pontiff_invisible} entity: those are model/phase names inside
     * {@code SacredPontiffEntity}, so the single {@code sacred_pontiff} id covers both phases.
     */
    private static final Map<ResourceLocation, ResourceLocation> ENTITY_SCHOOL = Map.ofEntries(
            e("sacred_pontiff", SchoolRegistry.HOLY_RESOURCE),
            e("shade_sword", SchoolRegistry.BLOOD_RESOURCE),
            e("shade_maw", SchoolRegistry.BLOOD_RESOURCE),
            e("dinamo", SchoolRegistry.LIGHTNING_RESOURCE),
            e("broken_dinamo", SchoolRegistry.LIGHTNING_RESOURCE),
            e("illager_golem", SchoolRegistry.LIGHTNING_RESOURCE),
            e("ember_pole", SchoolRegistry.FIRE_RESOURCE),
            e("living_candle", SchoolRegistry.FIRE_RESOURCE),
            e("firework_toad", SchoolRegistry.FIRE_RESOURCE),
            e("nether_bullfrog", SchoolRegistry.FIRE_RESOURCE),
            e("ender_frog", SchoolRegistry.ENDER_RESOURCE));

    /** Companions projectile id -> IS school (Pontiff attacks + the Crystallized Blood weapons). */
    private static final Map<ResourceLocation, ResourceLocation> PROJECTILE_SCHOOL = Map.ofEntries(
            e("pontiff_fire_ring", SchoolRegistry.FIRE_RESOURCE),
            e("holiness_star", SchoolRegistry.HOLY_RESOURCE),
            e("blood_slash_projectile", SchoolRegistry.BLOOD_RESOURCE), // Crystallized Blood SWORD
            e("blood_tornado_projectile", SchoolRegistry.BLOOD_RESOURCE), // AXE ("Blood Vortex")
            e("shade_sword_impact", SchoolRegistry.BLOOD_RESOURCE));

    private static Map.Entry<ResourceLocation, ResourceLocation> e(String path, ResourceLocation school) {
        return Map.entry(new ResourceLocation("companions", path), school);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || RETYPING.get()) {
            return;
        }
        DamageSource src = event.getSource();
        Entity direct = src.getDirectEntity();

        // Our own spell projectiles: hand the hit to Iron's Spells so it behaves like any IS spell.
        if (dealtAsSpell(event, target, src, direct)) {
            return;
        }

        if (isAlreadyMagic(src)) {
            return; // never double-type native IS damage
        }
        ResourceLocation school = schoolFor(src);
        if (school == null) {
            return;
        }
        DamageSource magic = buildSchoolDamage(target.level(), school, src.getEntity(), direct);
        if (magic == null) {
            return; // school or its damage type is missing; leave the hit untouched
        }
        event.setCanceled(true);
        RETYPING.set(true);
        try {
            target.hurt(magic, event.getAmount());
        } finally {
            RETYPING.set(false);
        }
    }

    /**
     * Re-deals a hit from one of our spell projectiles through {@code DamageSources.applyDamage} with a
     * real {@code SpellDamageSource}, the same call every stock IS spell makes. That is what earns the
     * spell-power scaling, school resistance, {@code SpellDamageEvent}, knockback and friendly-fire
     * handling, and the spell's death message. Power is read at hit time, so buffs gained mid-flight
     * count, as they do for IS projectiles.
     *
     * @return true when the hit was handled here
     */
    private static boolean dealtAsSpell(LivingAttackEvent event, LivingEntity target, DamageSource src, Entity direct) {
        if (direct == null || !direct.getPersistentData().contains(CompanionsProjectiles.TAG_SPELL)) {
            return false;
        }
        ResourceLocation spellId =
                ResourceLocation.tryParse(direct.getPersistentData().getString(CompanionsProjectiles.TAG_SPELL));
        AbstractSpell spell = spellId == null ? null : SpellRegistry.getSpell(spellId);
        if (spell == null || spell.equals(SpellRegistry.none())) {
            return false;
        }
        int spellLevel = Math.max(1, direct.getPersistentData().getInt(CompanionsProjectiles.TAG_SPELL_LEVEL));
        Entity caster = src.getEntity();
        float amount = spell.getSpellPower(spellLevel, caster);

        event.setCanceled(true);
        RETYPING.set(true);
        try {
            DamageSources.applyDamage(target, amount, spell.getDamageSource(direct, caster));
        } finally {
            RETYPING.set(false);
        }
        return true;
    }

    private static ResourceLocation schoolFor(DamageSource src) {
        Entity direct = src.getDirectEntity();
        if (direct != null) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(direct.getType());
            ResourceLocation school = PROJECTILE_SCHOOL.get(id);
            if (school == null) {
                school = ENTITY_SCHOOL.get(id);
            }
            if (school != null) {
                return school;
            }
        }
        Entity attacker = src.getEntity();
        if (attacker != null) {
            return ENTITY_SCHOOL.get(ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType()));
        }
        return null;
    }

    private static boolean isAlreadyMagic(DamageSource src) {
        return src.typeHolder()
                .unwrapKey()
                .map(k -> k.location().getNamespace().equals("irons_spellbooks"))
                .orElse(false);
    }

    /**
     * Asks the {@link SchoolType} for its damage type rather than assembling
     * {@code irons_spellbooks:<school>_magic} by hand, which only holds for IS's built-in schools and
     * throws for addon schools like Geomancy Plus's {@code geo}.
     */
    private static DamageSource buildSchoolDamage(Level level, ResourceLocation schoolId, Entity attacker, Entity direct) {
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
