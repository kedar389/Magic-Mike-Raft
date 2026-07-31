package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
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
     * The geo school is owned by Geomancy Plus. VERIFY: prefer the addon's own constant if exposed
     * (e.g. {@code com.gametechbc.gtbcs_geomancy_plus.api.init.GGSchools.GEO}); otherwise this
     * ResourceKey lookup by id works. VERIFY {@code SchoolRegistry.SCHOOL_REGISTRY_KEY} name too.
     */
    private static final ResourceKey<SchoolType> GEO_SCHOOL =
            ResourceKey.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, new ResourceLocation("gtbcs_geomancy_plus", "geo"));

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
        this.castTime = 0;
        this.baseManaCost = 45;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
            Vec3 pos = entity.position().add(entity.getLookAngle().multiply(2, 0, 2));
            CompanionsProjectiles.spawn(level, CompanionsProjectiles.STONE_SPIKE, entity, pos);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
