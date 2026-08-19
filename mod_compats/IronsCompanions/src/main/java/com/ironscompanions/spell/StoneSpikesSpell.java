package com.ironscompanions.spell;

import com.ironscompanions.ModIds;
import dev.xylonity.companions.registry.CompanionsEntities;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Stone Spikes, the {@code book_stone_spikes} effect in Geomancy Plus's {@code geo} school: a cone of
 * {@code CompanionsEntities.STONE_SPIKE_PROJECTILE} erupting forward from the caster, each snapped to the
 * ground beneath it. Numbers follow the book (damage 4.0, cooldown 140t), with a channel added.
 *
 * <p>The book plays no release sound of its own, so the school's default cast sound is used.
 */
public class StoneSpikesSpell extends CompanionsSpell {

    /** Owned by Geomancy Plus. {@code setSchoolResource} takes the school's plain id. */
    private static final ResourceLocation GEO_SCHOOL = ModIds.geomancy("geo");

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
    protected void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster) {
        Vec3 forward = horizontalLook(caster);

        // Walk outward from the caster, fanning across the cone as the spikes get further away.
        for (int i = 0; i < SPIKE_COUNT; i++) {
            float yaw = -CONE_DEGREES / 2f + CONE_DEGREES * (i / (float) (SPIKE_COUNT - 1));
            Vec3 dir = forward.yRot((float) Math.toRadians(yaw));
            double distance = FIRST_SPIKE + SPIKE_STEP * i;
            double x = caster.getX() + dir.x * distance;
            double z = caster.getZ() + dir.z * distance;
            double y = CompanionsProjectiles.groundBelow(level, x, caster.getY(), z);
            CompanionsProjectiles.spawn(level, CompanionsEntities.STONE_SPIKE_PROJECTILE, caster,
                    new Vec3(x, y, z), this, spellLevel);
        }
    }
}
