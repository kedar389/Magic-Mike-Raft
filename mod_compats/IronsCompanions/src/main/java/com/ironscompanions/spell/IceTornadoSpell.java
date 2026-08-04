package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import dev.xylonity.companions.registry.CompanionsSounds;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ice Tornado, recreation of the Companions {@code book_ice_tornado} effect as an Ice-school spell.
 * Spawns the original {@link CompanionsProjectiles#TORNADO} entity so the animation is identical.
 * Config baseline from companions.toml: ICE_TORNADO_DAMAGE=3.0, ICE_TORNADO_COOLDOWN=200t (10s).
 * "Tiers" are modelled as spell levels; scrolls are distributed at Uncommon/Rare/Epic (README 5A-i).
 */
public class IceTornadoSpell extends AbstractSpell {

    private final ResourceLocation spellId = new ResourceLocation(IronsCompanions.MOD_ID, "ice_tornado");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public IceTornadoSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 3; // matches ICE_TORNADO_DAMAGE
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
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

    /** The book's own release sound, so the spell sounds like the original. */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(CompanionsSounds.SPELL_RELEASE_TORNADO.get());
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getSpellPower(spellLevel, caster)));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            // Same placement as the original book: one block ahead on the horizontal look vector, at
            // the caster's feet. The tornado works out its own ground height and travel from there.
            Vec3 look = entity.getLookAngle();
            Vec3 ahead = new Vec3(look.x, 0, look.z).normalize();
            Vec3 pos = new Vec3(entity.getX() + ahead.x, entity.getY(), entity.getZ() + ahead.z);
            CompanionsProjectiles.spawn(level, CompanionsProjectiles.TORNADO, entity, pos, this, spellLevel);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
