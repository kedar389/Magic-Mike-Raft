package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Naginata, recreation of the Companions {@code book_naginata} effect as a LEGENDARY Holy spell.
 * Rains naginatas down onto the target ({@link CompanionsProjectiles#NAGINATA}). This spell is a
 * boss drop (Sacred Pontiff, see {@code CompanionsLootEvents}); the book is removed.
 * Baseline: NAGINATA_COOLDOWN=140t (7s). Damage is hardcoded in the projectile class, VERIFY from
 * {@code HolinessNaginataProjectile} in the Companions source and mirror it into baseSpellPower.
 */
public class NaginataSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE) // VERIFY constant name
            .setMaxLevel(10)
            .setCooldownSeconds(7)
            .build();

    public NaginataSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8; // VERIFY vs HolinessNaginataProjectile hardcoded damage
        this.spellPowerPerLevel = 2;
        this.castTime = 20; // brief channel ("press key to increase accuracy")
        this.baseManaCost = 90;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
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
            // TODO(build): spawn the volley above the aimed target, as the book does.
            Vec3 pos = entity.position().add(entity.getLookAngle().multiply(4, 0, 4)).add(0, 5, 0);
            CompanionsProjectiles.spawn(level, CompanionsProjectiles.NAGINATA, entity, pos);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
