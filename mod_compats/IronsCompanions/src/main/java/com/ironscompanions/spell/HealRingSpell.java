package com.ironscompanions.spell;

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
 * {@code CompanionsEntities.HEAL_RING_PROJECTILE} at the caster's feet, healing everything friendly it
 * covers.
 *
 * <p>Healing scales with the scroll through {@code mixin/HealRingScalingMixin}, and the numbers sit
 * between IS's own heals: 4 + 1 per level against Heal's 5 (single target, 30s) and Healing Circle's 2
 * (lingering, 25s), on a 20s cooldown because this one is an instant burst over an area.
 */
public class HealRingSpell extends CompanionsSpell {

    public HealRingSpell() {
        super("heal_ring",
                new DefaultConfig()
                        .setMinRarity(SpellRarity.UNCOMMON)
                        .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
                        .setMaxLevel(10)
                        .setCooldownSeconds(20)
                        .build(),
                CompanionsSounds.SPELL_RELEASE_HEAL);
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
        this.baseManaCost = 40;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }
    /**
     * Healing follows {@link #getSpellPower}, the way the damage spells do, so the tooltip prints it
     * rather than the Companions config number the projectile would otherwise apply.
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.healing",
                Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)));
    }

    /**
     * Tagged with the spell and its level, which is what {@code mixin/HealRingScalingMixin} reads to scale
     * the heal. An untagged ring, the kind Companions' own mobs cast, keeps the config amount.
     */
    @Override
    protected void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster) {
        CompanionsProjectiles.spawn(level, CompanionsEntities.HEAL_RING_PROJECTILE, caster, caster.position(),
                this, spellLevel);
    }
}
