package com.ironscompanions.spell;

import dev.xylonity.companions.registry.CompanionsEntities;
import dev.xylonity.companions.registry.CompanionsSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Naginata, the {@code book_naginata} effect as a legendary holy spell: a volley of
 * {@code CompanionsEntities.HOLINESS_NAGINATA} raining onto the aimed point. Numbers follow the book
 * (`NAGINATA_DAMAGE` 8.0, cooldown 140t), with a long channel for the volley.
 *
 * <p>Obtained only from the crafted {@code companions:book_naginata} spellbook (see
 * {@code mixin/NaginataBookSpellbookMixin}); {@link #allowLooting()} is what keeps it out of every other
 * source.
 */
public class NaginataSpell extends CompanionsSpell {

    private static final int BLADE_COUNT = 8;
    private static final double BLADE_SPREAD = 12.0;
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
    protected void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster) {
        Vec3 target = aimPoint(level, caster);
        double damage = getSpellPower(spellLevel, caster);
        RandomSource random = caster.getRandom();
        for (int i = 0; i < BLADE_COUNT; i++) {
            double x = target.x + (random.nextDouble() - 0.5) * BLADE_SPREAD;
            double z = target.z + (random.nextDouble() - 0.5) * BLADE_SPREAD;
            double y = target.y + BLADE_HEIGHT + random.nextDouble() * BLADE_HEIGHT_JITTER;
            Vec3 from = new Vec3(x, y, z);
            CompanionsProjectiles.spawnAimed(level, CompanionsEntities.HOLINESS_NAGINATA, caster,
                    from, target, BLADE_VELOCITY, BLADE_INACCURACY, damage, this, spellLevel);
            NaginataShockwave.spawnSparkle(level, from);
        }
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
