package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stone Spikes, recreation of the Companions {@code book_stone_spikes} effect in the Geomancy Plus
 * {@code geo} school (there is no "teramancy" school). Spawns the original
 * {@link CompanionsProjectiles#STONE_SPIKE} entity. Baseline: STONE_SPIKE_DAMAGE=4.0, cooldown=140t.
 */
public class StoneSpikesSpell extends AbstractSpell {

    /**
     * The geo school is owned by Geomancy Plus. {@code DefaultConfig#setSchoolResource} takes the
     * school's plain id (see {@code SchoolRegistry.ICE_RESOURCE} and friends), not a ResourceKey.
     */
    private static final ResourceLocation GEO_SCHOOL = new ResourceLocation("gtbcs_geomancy_plus", "geo");

    private final ResourceLocation spellId = new ResourceLocation(IronsCompanions.MOD_ID, "stone_spikes");

    /**
     * The original erupts a forward cone: 10 spikes sweeping a yaw arc in front of the caster, each at
     * an increasing distance and snapped to the ground.
     */
    private static final int SPIKE_COUNT = 10;
    private static final float CONE_DEGREES = 60f;   // total arc, so +/-30 either side of the look
    private static final double FIRST_SPIKE = 1.5;   // distance of the nearest spike
    private static final double SPIKE_STEP = 0.9;    // extra distance per spike

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(GEO_SCHOOL)
            .setMaxLevel(10)
            .setCooldownSeconds(7)
            .build();

    public StoneSpikesSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 4; // matches STONE_SPIKE_DAMAGE
        this.spellPowerPerLevel = 1;
        this.castTime = 30; // channelled: the ground has to split before the spikes come up
        this.baseManaCost = 45;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG; // LONG = a channel with a cast bar; INSTANT ignores castTime
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getSpellPower(spellLevel, caster)));
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
