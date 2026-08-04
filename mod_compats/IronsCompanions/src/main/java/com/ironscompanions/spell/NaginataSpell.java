package com.ironscompanions.spell;

import dev.xylonity.companions.registry.CompanionsSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Naginata, the {@code book_naginata} effect as a legendary holy spell: a volley of
 * {@link CompanionsProjectiles#NAGINATA} raining onto the aimed point. Cooldown follows the book
 * (140t); the base power is a placeholder until the projectile's own damage is read from source.
 *
 * <p>Obtained only from the Sacred Pontiff (see {@code CompanionsLootEvents}), which
 * {@link #allowLooting()} is what enforces.
 */
public class NaginataSpell extends CompanionsSpell {

    private static final int BLADE_COUNT = 6;
    private static final double BLADE_SPREAD = 6.0;
    private static final double BLADE_HEIGHT = 20.0;
    private static final double BLADE_HEIGHT_JITTER = 10.0;
    private static final float BLADE_VELOCITY = 1.5f;
    private static final float BLADE_INACCURACY = 1.0f;
    private static final double AIM_RANGE = 40.0;

    public NaginataSpell() {
        super("naginata",
                new DefaultConfig()
                        .setMinRarity(SpellRarity.LEGENDARY)
                        .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
                        .setMaxLevel(10)
                        .setCooldownSeconds(7)
                        .build(),
                CompanionsSounds.SPELL_RELEASE_SPEARS);
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 45;
        this.baseManaCost = 90;
    }

    /**
     * Keeps the legendary out of Iron's Spells' generic scroll rolls, which draw from every spell whose
     * {@code allowLooting()} is true (by default the school's own flag, true for holy). A
     * {@code SpellFilter} built with {@code force=true} still ignores this.
     */
    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 target = aimPoint(level, entity);
            double damage = getSpellPower(spellLevel, entity);
            for (int i = 0; i < BLADE_COUNT; i++) {
                double x = target.x + (entity.getRandom().nextDouble() - 0.5) * BLADE_SPREAD;
                double z = target.z + (entity.getRandom().nextDouble() - 0.5) * BLADE_SPREAD;
                double y = target.y + BLADE_HEIGHT + entity.getRandom().nextDouble() * BLADE_HEIGHT_JITTER;
                CompanionsProjectiles.spawnAimed(level, CompanionsProjectiles.NAGINATA, entity,
                        new Vec3(x, y, z), target, BLADE_VELOCITY, BLADE_INACCURACY, damage, this, spellLevel);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** The first block the caster is looking at within {@link #AIM_RANGE}, else that far along the look. */
    private static Vec3 aimPoint(Level level, LivingEntity caster) {
        Vec3 eyes = caster.getEyePosition();
        Vec3 end = eyes.add(caster.getLookAngle().scale(AIM_RANGE));
        BlockHitResult hit = level.clip(
                new ClipContext(eyes, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
    }
}
