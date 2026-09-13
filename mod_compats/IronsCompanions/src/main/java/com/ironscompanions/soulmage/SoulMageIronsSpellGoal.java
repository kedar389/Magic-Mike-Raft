package com.ironscompanions.soulmage;

import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Casts the Iron's Spells scrolls in the Soul Mage's three slots. Companions' own book goals go inert once
 * the books are gone, so this is added alongside them.
 *
 * <p>{@code chooseSpell} decides what: counterspell, escape, heal, or the attack rotation, by
 * {@link SpellRoles}. How depends on {@link CastType} — {@code INSTANT} is one {@code onCast}, the rest
 * are driven a tick at a time through {@link MagicData}, because a channelled spell does its work in
 * {@code onServerCastTick}.
 */
public class SoulMageIronsSpellGoal extends Goal {

    /** Tick within the goal at which an instant spell goes off, leaving a short wind-up. */
    private static final int INSTANT_CAST_TICK = 6;

    /** How long the goal holds an instant cast, wind-up plus recovery. */
    private static final int INSTANT_DURATION_TICKS = 12;

    /** The slot IS records a cast against. A mob has no spellbook, but the field cannot be empty. */
    private static final String CASTING_SLOT = "mainhand";

    /** Below this share of its health, the mage would rather heal itself than attack. */
    private static final float OWN_HEALTH_PANIC = 0.4f;

    /** Below this share, an ally is worth an area heal. */
    private static final float ALLY_HURT = 0.6f;

    /** And this is how close that ally has to be, in blocks, for the heal to reach it. */
    private static final double HEAL_RADIUS = 8d;

    /** Escaping is for a mage both freshly hit and already hurt. Either alone is a normal fight. */
    private static final float DANGER_HEALTH = 0.6f;

    private static final float LOOK_SPEED = 30f;
    private static final double RADIANS_TO_DEGREES = 180d / Math.PI;

    /** How long the mage circles one way before turning around, and how fast it circles. */
    private static final int STRAFE_SWITCH_TICKS = 30;
    private static final float STRAFE_SPEED = 0.35f;

    /** How far the mage will look for a sheep to dye. */
    private static final double SHEEP_RADIUS = 8d;

    /**
     * Multiplies each spell's own cooldown. IS tunes those for a player with a full book (Heal 30s, Blaze
     * Storm 20s), so at 1.0 a mage holding heavy scrolls goes quiet between casts. This is the dial.
     */
    private static final float COOLDOWN_SCALE = 1.0f;

    private final SoulMageEntity mage;
    private final int minCooldown;
    private final int maxCooldown;

    /**
     * One ledger for every cast: a mob has no capability to keep mana or cast state in. Built the way
     * {@code AbstractSpellCastingMob} builds its own — {@code isMob} true, and a {@link SyncedSpellData} of
     * its own, which is what {@code initiateCast} writes the casting flag into.
     */
    private final MagicData magicData;

    /** Spell ids cast since the rotation last wrapped. Cleared once every spell has had its turn. */
    private final Set<String> castThisRound = new HashSet<>();

    private int nextUseTick;
    private int castTicks;
    private int channelTicksLeft;
    private boolean channelling;
    private boolean attackCast;
    private SpellData chosen;

    private boolean strafingClockwise;
    private int strafeSwitchIn;

    /** When each spell may be cast again, by spell id. Per mage, since the cooldowns are its own. */
    private final Map<String, Integer> readyAt = new HashMap<>();

    /** What the chosen spell is aimed at, when it takes an entity rather than a direction. */
    private LivingEntity castTarget;

    /** Which fight the mage is in, and whether it has opened that one with a buff yet. */
    private UUID lastTargetId;
    private boolean buffPending;

    public SoulMageIronsSpellGoal(SoulMageEntity mage, int minCooldown, int maxCooldown) {
        this.mage = mage;
        this.magicData = new MagicData(true);
        this.magicData.setSyncedData(new SyncedSpellData(mage));
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        // MOVE as well as LOOK, because a channelled spell circles the target while it runs.
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Cooldown first: this runs every tick, and choosing a spell walks the mage's inventory.
        if (mage.tickCount < nextUseTick) {
            return false;
        }
        chosen = chooseSpell();
        return chosen != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (chosen == null) {
            return false;
        }
        if (attackCast && !canSeeLivingTarget()) {
            return false;
        }
        if (!channelling) {
            return castTicks < INSTANT_DURATION_TICKS;
        }
        LivingEntity target = mage.getTarget();
        // IS's own hook for a channel that has stopped being worth it: target dead, gone, out of range.
        if (target != null && chosen.getSpell().shouldAIStopCasting(chosen.getLevel(), mage, target)) {
            return false;
        }
        return channelTicksLeft > 0;
    }

    @Override
    public void start() {
        castTicks = 0;
        channelling = false;
        channelTicksLeft = 0;
        strafeSwitchIn = 0;
        faceTarget();
        if (mage.level().isClientSide) {
            return;
        }
        AbstractSpell spell = chosen.getSpell();
        beginCast(spell);
        if (spell.getCastType() == CastType.INSTANT) {
            return;
        }
        // Charged and channelled spells are driven from MagicData the way IS drives a player's: the cast
        // runs for its own cast time and the spell is told about every tick of it.
        int castTime = Math.max(1, spell.getEffectiveCastTime(chosen.getLevel(), mage));
        magicData.initiateCast(spell, chosen.getLevel(), castTime, CastSource.MOB, CASTING_SLOT);
        spell.onServerPreCast(mage.level(), chosen.getLevel(), mage, magicData);
        channelling = true;
        channelTicksLeft = castTime;
        if (spell.getCastType() == CastType.CONTINUOUS) {
            // A channel starts its effect at once and keeps it up; a charge releases at the end instead.
            spell.onCast(mage.level(), chosen.getLevel(), mage, CastSource.MOB, magicData);
        }
    }

    @Override
    public void tick() {
        faceTarget();
        castTicks++;
        if (mage.level().isClientSide) {
            return;
        }
        if (!channelling) {
            if (castTicks == INSTANT_CAST_TICK) {
                chosen.getSpell().onCast(mage.level(), chosen.getLevel(), mage, CastSource.MOB, magicData);
            }
            return;
        }
        circleTarget();
        magicData.handleCastDuration();
        chosen.getSpell().onServerCastTick(mage.level(), chosen.getLevel(), mage, magicData);
        if (--channelTicksLeft <= 0) {
            finishChannel(false);
        }
    }

    @Override
    public void stop() {
        if (channelling) {
            finishChannel(true);
        }
        magicData.resetAdditionalCastData();
        castTarget = null;
        int window = Math.max(1, maxCooldown - minCooldown + 1);
        nextUseTick = mage.tickCount + minCooldown + mage.getRandom().nextInt(window);
        chosen = null;
    }

    /** Closes a charged or channelled cast, releasing a charge that ran its course. */
    private void finishChannel(boolean cancelled) {
        AbstractSpell spell = chosen.getSpell();
        if (!cancelled && spell.getCastType() == CastType.LONG) {
            spell.onCast(mage.level(), chosen.getLevel(), mage, CastSource.MOB, magicData);
        }
        spell.onServerCastComplete(mage.level(), chosen.getLevel(), mage, magicData, cancelled);
        magicData.resetCastingState();
        channelling = false;
        channelTicksLeft = 0;
    }

    /** First match wins. Situational spells are looked up by role so the rotation never draws them. */
    private SpellData chooseSpell() {
        attackCast = false;
        castTarget = null;
        if (canSeeLivingTarget() && targetIsCasting()) {
            SpellData counter = findHeld(SpellRoles::isCounterspell);
            if (counter != null) {
                attackCast = true;
                return counter;
            }
        }
        if (inDanger()) {
            SpellData escape = findHeld(SpellRoles::isEscape);
            if (escape != null) {
                return escape;
            }
        }
        if (healthFraction(mage) < OWN_HEALTH_PANIC) {
            SpellData heal = findHeld(SpellRoles::isSelfHeal);
            if (heal != null) {
                return heal;
            }
        }
        if (isAfflicted()) {
            SpellData cleanse = findHeld(SpellRoles::isCleanse);
            if (cleanse != null) {
                return cleanse;
            }
        }
        SpellData allyHeal = allyHeal();
        if (allyHeal != null) {
            return allyHeal;
        }
        SpellData opener = openingBuff();
        if (opener != null) {
            return opener;
        }
        if (canSeeLivingTarget()) {
            attackCast = true;
            return pickAttackSpell();
        }
        return sheepToDye();
    }

    /** A heal for a hurt ally: aimed at the worst of them if the spell takes a target, else cast on the spot. */
    private SpellData allyHeal() {
        SpellData targeted = findHeld(SpellRoles::isTargetedAllyHeal);
        SpellData area = findHeld(SpellRoles::isAreaAllyHeal);
        if (targeted == null && area == null) {
            return null;
        }
        LivingEntity hurt = hurtAllyNear();
        if (hurt == null) {
            return null;
        }
        if (targeted != null) {
            castTarget = hurt;
            return targeted;
        }
        return area;
    }

    /** One buff as a fight opens, then nothing until the mage picks a different target. */
    private SpellData openingBuff() {
        LivingEntity target = mage.getTarget();
        if (target == null) {
            return null;
        }
        if (!target.getUUID().equals(lastTargetId)) {
            lastTargetId = target.getUUID();
            buffPending = true;
        }
        if (!buffPending) {
            return null;
        }
        buffPending = false;
        return findHeld(SpellRoles::isBuff);
    }

    /** A casting mob answers {@code IMagicEntity}; a player keeps the same state in {@link MagicData}. */
    private boolean targetIsCasting() {
        LivingEntity target = mage.getTarget();
        if (target instanceof IMagicEntity caster) {
            return caster.isCasting();
        }
        return target instanceof Player && MagicData.getPlayerMagicData(target).isCasting();
    }

    /** Wololo dyes a sheep and does nothing else, so it waits for a lull. */
    private SpellData sheepToDye() {
        SpellData wololo = findHeld(SpellRoles::isWololo);
        if (wololo == null) {
            return null;
        }
        List<Sheep> flock = mage.level().getEntitiesOfClass(Sheep.class,
                mage.getBoundingBox().inflate(SHEEP_RADIUS));
        if (flock.isEmpty()) {
            return null;
        }
        castTarget = flock.get(mage.getRandom().nextInt(flock.size()));
        return wololo;
    }

    private boolean inDanger() {
        return SoulMageSpellcasting.tookBigHitRecently(mage) && healthFraction(mage) < DANGER_HEALTH;
    }

    /** The most hurt ally within {@link #HEAL_RADIUS}, or null. */
    private LivingEntity hurtAllyNear() {
        LivingEntity owner = mage.getOwner();
        if (owner == null) {
            return null;
        }
        LivingEntity worst = null;
        for (LivingEntity other : mage.level().getEntitiesOfClass(LivingEntity.class,
                mage.getBoundingBox().inflate(HEAL_RADIUS))) {
            if (other != mage && healthFraction(other) < ALLY_HURT && isAllyOf(owner, other)
                    && (worst == null || healthFraction(other) < healthFraction(worst))) {
                worst = other;
            }
        }
        return worst;
    }

    /** Whether anything is wearing the mage down that a cleanse would lift. */
    private boolean isAfflicted() {
        return mage.getActiveEffects().stream().anyMatch(effect -> !effect.getEffect().isBeneficial());
    }

    /** The owner, and anything else the owner owns: the rest of the pack, never the mage's target. */
    private static boolean isAllyOf(LivingEntity owner, LivingEntity other) {
        return other == owner || (other instanceof OwnableEntity owned && owned.getOwner() == owner);
    }

    private static float healthFraction(LivingEntity entity) {
        float max = entity.getMaxHealth();
        return max <= 0f ? 1f : entity.getHealth() / max;
    }

    /** A live target with a clear line to it. Without this the mage casts into cover. */
    private boolean canSeeLivingTarget() {
        LivingEntity target = mage.getTarget();
        return target != null && target.isAlive() && mage.getSensing().hasLineOfSight(target);
    }

    /** Uniform among the spells not cast this round; starts a new round when they have all been used. */
    private SpellData pickAttackSpell() {
        SpellData picked = sample(true);
        if (picked == null) {
            castThisRound.clear();
            picked = sample(false);
        }
        if (picked != null) {
            castThisRound.add(picked.getSpell().getSpellId());
        }
        return picked;
    }

    /** One reservoir pass over the inventory, so nothing is allocated per cast. */
    private SpellData sample(boolean freshOnly) {
        LivingEntity target = mage.getTarget();
        double distance = target == null ? 0d : mage.distanceTo(target);
        SpellData picked = null;
        int seen = 0;
        for (int i = 0; i < mage.inventory.getContainerSize(); i++) {
            for (SpellSlot slot : spellsIn(mage.inventory.getItem(i))) {
                SpellData data = slot.spellData();
                if (!isRotationSpell(data, distance)
                        || (freshOnly && castThisRound.contains(data.getSpell().getSpellId()))) {
                    continue;
                }
                if (mage.getRandom().nextInt(++seen) == 0) {
                    picked = data;
                }
            }
        }
        return picked;
    }

    /** First spell in the inventory filling {@code role} and off cooldown, else null. */
    private SpellData findHeld(Predicate<AbstractSpell> role) {
        for (int i = 0; i < mage.inventory.getContainerSize(); i++) {
            for (SpellSlot slot : spellsIn(mage.inventory.getItem(i))) {
                SpellData data = slot.spellData();
                if (data != null && data.getSpell() != null && role.test(data.getSpell())
                        && isReady(data.getSpell())) {
                    return data;
                }
            }
        }
        return null;
    }

    /** Scrolls and spellbooks both qualify; anything else holds no spells. */
    private static List<SpellSlot> spellsIn(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        ISpellContainer container = ISpellContainer.get(stack);
        return container == null || container.isEmpty() ? List.of() : container.getActiveSpells();
    }

    /** No summons, nothing wasted, nothing situational, nothing on cooldown, nothing out of range. */
    private boolean isRotationSpell(SpellData data, double distance) {
        if (data == null || data.getSpell() == null) {
            return false;
        }
        AbstractSpell spell = data.getSpell();
        return !SummonSpells.isSummon(spell) && !SpellRoles.isWasted(spell)
                && !SpellRoles.isSituational(spell) && isReady(spell)
                && distance <= SpellRoles.maxRange(spell);
    }

    /** Whether the spell's own cooldown has run out since the mage last cast it. */
    private boolean isReady(AbstractSpell spell) {
        Integer ready = readyAt.get(spell.getSpellId());
        return ready == null || mage.tickCount >= ready;
    }

    /** Starts the spell's cooldown, and hands it a target entity when it takes one (wololo). */
    private void beginCast(AbstractSpell spell) {
        int cooldown = Math.round(spell.getSpellCooldown() * COOLDOWN_SCALE);
        if (cooldown > 0) {
            readyAt.put(spell.getSpellId(), mage.tickCount + cooldown);
        }
        if (castTarget != null) {
            magicData.setAdditionalCastData(new TargetEntityCastData(castTarget));
        }
    }

    /** Circles the target while a channel runs, so the mage is not a still target for its whole cast. */
    private void circleTarget() {
        if (mage.getTarget() == null) {
            return;
        }
        if (--strafeSwitchIn <= 0) {
            strafeSwitchIn = STRAFE_SWITCH_TICKS;
            strafingClockwise = mage.getRandom().nextBoolean();
        }
        mage.getNavigation().stop();
        mage.getMoveControl().strafe(0f, strafingClockwise ? STRAFE_SPEED : -STRAFE_SPEED);
    }

    /** Turns body and head, not just the head: the spell leaves along the mage's look vector. */
    private void faceTarget() {
        LivingEntity target = mage.getTarget();
        if (target == null) {
            return;
        }
        mage.getLookControl().setLookAt(target, LOOK_SPEED, LOOK_SPEED);
        double dx = target.getX() - mage.getX();
        double dz = target.getZ() - mage.getZ();
        double dy = target.getEyeY() - mage.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * RADIANS_TO_DEGREES) - 90f;
        float pitch = (float) -(Mth.atan2(dy, horizontal) * RADIANS_TO_DEGREES);
        mage.setYRot(yaw);
        mage.yBodyRot = yaw;
        mage.yHeadRot = yaw;
        mage.setXRot(pitch);
    }
}
