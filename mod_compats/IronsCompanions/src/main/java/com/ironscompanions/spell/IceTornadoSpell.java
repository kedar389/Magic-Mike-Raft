package com.ironscompanions.spell;

import dev.xylonity.companions.registry.CompanionsEntities;
import dev.xylonity.companions.registry.CompanionsSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Ice Tornado, the {@code book_ice_tornado} effect as an ice spell: one
 * {@code CompanionsEntities.TORNADO_PROJECTILE} a block ahead of the caster, which then works out its own
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
    protected void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster) {
        Vec3 ahead = horizontalLook(caster);
        Vec3 pos = new Vec3(caster.getX() + ahead.x, caster.getY(), caster.getZ() + ahead.z);
        CompanionsProjectiles.spawn(level, CompanionsEntities.TORNADO_PROJECTILE, caster, pos, this, spellLevel);
    }
}
