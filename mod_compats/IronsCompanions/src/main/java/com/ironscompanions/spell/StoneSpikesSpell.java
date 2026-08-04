package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stone Spikes, the {@code book_stone_spikes} effect in Geomancy Plus's {@code geo} school: a cone of
 * {@link CompanionsProjectiles#STONE_SPIKE} erupting forward from the caster, each snapped to the
 * ground beneath it. Numbers follow the book (damage 4.0, cooldown 140t), with a channel added.
 *
 * <p>The book plays no release sound of its own, so the school's default cast sound is used.
 */
public class StoneSpikesSpell extends CompanionsSpell {

    /** Owned by Geomancy Plus. {@code setSchoolResource} takes the school's plain id. */
    private static final ResourceLocation GEO_SCHOOL = new ResourceLocation("gtbcs_geomancy_plus", "geo");

    private static final int SPIKE_COUNT = 10;
    private static final float CONE_DEGREES = 60f;  // total arc, so 30 either side of the look vector
    private static final double FIRST_SPIKE = 1.5;  // distance of the nearest spike
    private static final double SPIKE_STEP = 0.9;   // extra distance per spike

    public StoneSpikesSpell() {
        super("stone_spikes",
                new DefaultConfig()
                        .setMinRarity(SpellRarity.RARE)
                        .setSchoolResource(GEO_SCHOOL)
                        .setMaxLevel(10)
                        .setCooldownSeconds(7)
                        .build(),
                null);
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 45;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 look = entity.getLookAngle();
            Vec3 forward = new Vec3(look.x, 0, look.z).normalize();

            // Walk outward from the caster, fanning across the cone as the spikes get further away.
            for (int i = 0; i < SPIKE_COUNT; i++) {
                float yaw = -CONE_DEGREES / 2f + CONE_DEGREES * (i / (float) (SPIKE_COUNT - 1));
                Vec3 dir = forward.yRot((float) Math.toRadians(yaw));
                double distance = FIRST_SPIKE + SPIKE_STEP * i;
                double x = entity.getX() + dir.x * distance;
                double z = entity.getZ() + dir.z * distance;
                double y = CompanionsProjectiles.groundBelow(level, x, entity.getY(), z);
                CompanionsProjectiles.spawn(level, CompanionsProjectiles.STONE_SPIKE, entity,
                        new Vec3(x, y, z), this, spellLevel);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
