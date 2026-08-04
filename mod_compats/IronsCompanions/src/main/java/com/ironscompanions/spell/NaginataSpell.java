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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Naginata, recreation of the Companions {@code book_naginata} effect as a LEGENDARY Holy spell.
 * Rains naginatas down onto the target ({@link CompanionsProjectiles#NAGINATA}). This spell is a
 * boss drop (Sacred Pontiff, see {@code CompanionsLootEvents}); the book is removed.
 * Baseline: NAGINATA_COOLDOWN=140t (7s). Damage is hardcoded in the projectile class, VERIFY from
 * {@code HolinessNaginataProjectile} in the Companions source and mirror it into baseSpellPower.
 */
public class NaginataSpell extends AbstractSpell {

    private final ResourceLocation spellId = new ResourceLocation(IronsCompanions.MOD_ID, "naginata");

    /** Volley shape, mirroring the original book's non-crouched cast (6 blades, ~30 block scatter). */
    private static final int BLADE_COUNT = 6;
    private static final double BLADE_SPREAD = 6.0;   // tighter than the book's 30, which often missed
    private static final double BLADE_HEIGHT = 20.0;
    private static final double BLADE_HEIGHT_JITTER = 10.0;
    private static final float BLADE_VELOCITY = 1.5f;
    private static final float BLADE_INACCURACY = 1.0f;
    private static final double AIM_RANGE = 40.0;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(7)
            .build();

    public NaginataSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8; // VERIFY vs HolinessNaginataProjectile hardcoded damage
        this.spellPowerPerLevel = 2;
        this.castTime = 45; // longer channel: it is a legendary volley, not a single throw
        this.baseManaCost = 90;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    /**
     * Keeps Naginata out of Iron's Spells' generic scroll rolls. {@code SpellFilter} drops any spell
     * whose {@code allowLooting()} is false (the default reads the holy school's flag, which is true),
     * so without this the legendary would turn up in ordinary chest loot instead of being the Sacred
     * Pontiff's drop. Note this only affects unforced filters; a {@code SpellFilter} built with
     * {@code force=true} ignores it.
     */
    @Override
    public boolean allowLooting() {
        return false;
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

    /** The book's own release sound, so the spell sounds like the original. */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(CompanionsSounds.SPELL_RELEASE_SPEARS.get());
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getSpellPower(spellLevel, caster)));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 target = aimPoint(level, entity);
            double damage = getSpellPower(spellLevel, entity);
            // The original rains a volley: 6 blades from ~20 blocks up, scattered around the target,
            // each falling toward it. One blade was the placeholder, not the intended effect.
            for (int i = 0; i < BLADE_COUNT; i++) {
                double x = target.x + (entity.getRandom().nextDouble() - 0.5) * BLADE_SPREAD;
                double z = target.z + (entity.getRandom().nextDouble() - 0.5) * BLADE_SPREAD;
                double y = target.y + BLADE_HEIGHT + entity.getRandom().nextDouble() * BLADE_HEIGHT_JITTER;
                CompanionsProjectiles.spawnAimed(level, CompanionsProjectiles.NAGINATA, entity,
                        new Vec3(x, y, z), target, BLADE_VELOCITY, BLADE_INACCURACY, damage,
                        this, spellLevel);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /**
     * Where the volley lands: the first block the caster is looking at within {@link #AIM_RANGE},
     * or that far along the look vector if nothing is hit. The book raycast 80 blocks; 40 keeps the
     * blades on screen.
     */
    private static Vec3 aimPoint(Level level, LivingEntity caster) {
        Vec3 eyes = caster.getEyePosition();
        Vec3 end = eyes.add(caster.getLookAngle().scale(AIM_RANGE));
        BlockHitResult hit = level.clip(
                new ClipContext(eyes, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
    }
}
