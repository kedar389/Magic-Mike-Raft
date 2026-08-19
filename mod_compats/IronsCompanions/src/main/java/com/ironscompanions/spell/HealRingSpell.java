package com.ironscompanions.spell;

import dev.xylonity.companions.config.CompanionsConfig;
import dev.xylonity.companions.registry.CompanionsEntities;
import dev.xylonity.companions.registry.CompanionsSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Heal Ring, the {@code book_heal_ring} effect as a nature spell: a restorative
 * {@code CompanionsEntities.HEAL_RING_PROJECTILE} at the caster's feet. Numbers follow the book (healing 6.0,
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

    /**
     * The ring heals {@code CompanionsConfig.HEAL_RING_HEALING} and nothing else:
     * {@code HealRingProjectile} reads that value itself and calls {@code heal} on everything in range,
     * so unlike the damage spells the amount does not follow {@link #getSpellPower}. The tooltip reports
     * the number the projectile will actually apply rather than the spell power the base class would
     * print.
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.healing",
                Utils.stringTruncation(CompanionsConfig.HEAL_RING_HEALING, 1)));
    }

    @Override
    protected void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster) {
        CompanionsProjectiles.spawn(level, CompanionsEntities.HEAL_RING_PROJECTILE, caster, caster.position());
    }
}
