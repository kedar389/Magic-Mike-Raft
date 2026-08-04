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
import net.minecraft.world.phys.Vec3;

/**
 * Ice Tornado, the {@code book_ice_tornado} effect as an ice spell: one
 * {@link CompanionsProjectiles#TORNADO} a block ahead of the caster, which then works out its own
 * ground height and travel. Numbers follow the book (damage 3.0, cooldown 200t).
 */
public class IceTornadoSpell extends CompanionsSpell {

    public IceTornadoSpell() {
        super("ice_tornado",
                new DefaultConfig()
                        .setMinRarity(SpellRarity.UNCOMMON)
                        .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
                        .setMaxLevel(10)
                        .setCooldownSeconds(10)
                        .build(),
                CompanionsSounds.SPELL_RELEASE_TORNADO);
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 look = entity.getLookAngle();
            Vec3 ahead = new Vec3(look.x, 0, look.z).normalize();
            Vec3 pos = new Vec3(entity.getX() + ahead.x, entity.getY(), entity.getZ() + ahead.z);
            CompanionsProjectiles.spawn(level, CompanionsProjectiles.TORNADO, entity, pos, this, spellLevel);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
