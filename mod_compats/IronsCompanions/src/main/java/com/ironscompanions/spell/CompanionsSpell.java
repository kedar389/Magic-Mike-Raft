package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Shared base for the spells converted from Companions magic books.
 *
 * <p>Holds the identity, config and tooltip plumbing that Iron's Spells requires of every spell, so
 * subclasses carry only their own numbers and their {@code onCast}.
 */
abstract class CompanionsSpell extends AbstractSpell {

    /** Iron's Spells' own string for a spell tooltip's damage line. */
    private static final String DAMAGE_INFO_KEY = "ui.irons_spellbooks.damage";

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

    /** The one stat line on the tooltip. Overridden by spells that do not deal damage. */
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable(DAMAGE_INFO_KEY,
                Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)));
    }

    /**
     * Final, so a spell cannot forget the client-side guard or the {@code super.onCast} that drives Iron's
     * Spells' own cast bookkeeping. Subclasses implement {@link #castOnServer} instead.
     */
    @Override
    public final void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource,
                             MagicData playerMagicData) {
        // Off the client, Level is always a ServerLevel, so this both guards the side and hands
        // subclasses the type they need for particles without an instanceof of their own.
        if (level instanceof ServerLevel serverLevel) {
            castOnServer(serverLevel, spellLevel, entity);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** The spell's actual effect, server side only. */
    protected abstract void castOnServer(ServerLevel level, int spellLevel, LivingEntity caster);

    /**
     * The caster's look direction flattened onto the ground plane and normalised, for the spells that
     * erupt along the ground rather than where the caster is aiming. Straight up or down normalises to
     * {@link Vec3#ZERO}, as it does in vanilla.
     */
    protected static Vec3 horizontalLook(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        return new Vec3(look.x, 0, look.z).normalize();
    }
}
