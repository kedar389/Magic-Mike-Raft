package com.ironscompanions.spell;


import dev.xylonity.companions.registry.CompanionsSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Heal Ring, the {@code book_heal_ring} effect as a nature spell: a restorative
 * {@link CompanionsProjectiles#HEAL_RING} at the caster's feet. Numbers follow the book (healing 6.0,
 * cooldown 200t), with a channel added.
 */
public class HealRingSpell extends CompanionsSpell {

    public HealRingSpell() {
        super("heal_ring",
                new DefaultConfig()
                        .setMinRarity(SpellRarity.UNCOMMON)
                        .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                        .setMaxLevel(10)
                        .setCooldownSeconds(10)
                        .build(),
                CompanionsSounds.SPELL_RELEASE_HEAL);
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    protected String infoKey() {
        return "ui.irons_spellbooks.heal";
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            CompanionsProjectiles.spawn(level, CompanionsProjectiles.HEAL_RING, entity, entity.position());
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
