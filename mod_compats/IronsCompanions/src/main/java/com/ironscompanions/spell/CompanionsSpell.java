package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shared base for the four spells converted from Companions magic books.
 *
 * <p>Holds the identity, config and tooltip plumbing that Iron's Spells requires of every spell, so
 * subclasses carry only their own numbers and their {@code onCast}.
 */
abstract class CompanionsSpell extends AbstractSpell {

    private final ResourceLocation spellId;
    private final DefaultConfig config;
    private final Supplier<SoundEvent> releaseSound;

    /**
     * @param name         registry path, matching the id used in {@link IronsCompanionsSpells}
     * @param config       built {@link DefaultConfig}
     * @param releaseSound the original book's release sound, or null to fall back to the school default
     */
    protected CompanionsSpell(String name, DefaultConfig config, Supplier<SoundEvent> releaseSound) {
        this.spellId = new ResourceLocation(IronsCompanions.MOD_ID, name);
        this.config = config;
        this.releaseSound = releaseSound;
    }

    @Override
    public final ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public final DefaultConfig getDefaultConfig() {
        return config;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return releaseSound == null ? Optional.empty() : Optional.of(releaseSound.get());
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable(infoKey(), getSpellPower(spellLevel, caster)));
    }

    /** Translation key for the single tooltip stat line. Override for spells that do not deal damage. */
    protected String infoKey() {
        return "ui.irons_spellbooks.damage";
    }
}
