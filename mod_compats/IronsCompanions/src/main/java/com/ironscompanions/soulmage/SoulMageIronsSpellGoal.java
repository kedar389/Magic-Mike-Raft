package com.ironscompanions.soulmage;

import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * Makes the Soul Mage cast whatever Iron's Spells scrolls sit in its three slots, at its current
 * target. The stock {@code SoulMage*Goal}s go inert by themselves once the books are gone, since their
 * {@code hasRequiredBook()} can no longer pass, so this goal is simply added alongside them.
 *
 * <p>Scrolls reach those slots via {@code mixin/SoulMageBookSlotMixin}.
 */
public class SoulMageIronsSpellGoal extends Goal {

    /** Tick within the goal at which the spell goes off, leaving a short wind-up. */
    private static final int CAST_TICK = 6;

    /** How long the goal holds the mage still after starting, wind-up plus recovery. */
    private static final int DURATION_TICKS = 12;

    private static final float LOOK_SPEED = 30f;
    private static final double RADIANS_TO_DEGREES = 180d / Math.PI;

    private final SoulMageEntity mage;
    private final int minCooldown;
    private final int maxCooldown;

    private int nextUseTick;
    private int castTicks;
    private SpellData chosen;

    public SoulMageIronsSpellGoal(SoulMageEntity mage, int minCooldown, int maxCooldown) {
        this.mage = mage;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Cooldown first: this runs every tick, and picking a spell walks the mage's inventory.
        if (mage.tickCount < nextUseTick || !hasLivingTarget()) {
            return false;
        }
        chosen = pickSpell();
        return chosen != null;
    }

    @Override
    public boolean canContinueToUse() {
        return castTicks < DURATION_TICKS && hasLivingTarget();
    }

    @Override
    public void start() {
        castTicks = 0;
        faceTarget();
    }

    @Override
    public void tick() {
        faceTarget();
        castTicks++;
        if (castTicks == CAST_TICK && chosen != null && !mage.level().isClientSide) {
            cast(chosen);
        }
    }

    @Override
    public void stop() {
        int window = Math.max(1, maxCooldown - minCooldown + 1);
        nextUseTick = mage.tickCount + minCooldown + mage.getRandom().nextInt(window);
        chosen = null;
    }

    private boolean hasLivingTarget() {
        LivingEntity target = mage.getTarget();
        return target != null && target.isAlive();
    }

    /**
     * Picks one spell uniformly from every scroll and spellbook in the Soul Mage's inventory, by
     * reservoir sampling, so no list is built for a check that runs on every tick of the cooldown.
     */
    private SpellData pickSpell() {
        SpellData picked = null;
        int seen = 0;
        for (int i = 0; i < mage.inventory.getContainerSize(); i++) {
            ItemStack stack = mage.inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            // Scrolls and spellbooks both qualify. getActiveSpells() hands back slots, not the
            // SpellData itself, so unwrap each one.
            ISpellContainer container = ISpellContainer.get(stack);
            if (container == null || container.isEmpty()) {
                continue;
            }
            for (SpellSlot slot : container.getActiveSpells()) {
                if (mage.getRandom().nextInt(++seen) == 0) {
                    picked = slot.spellData();
                }
            }
        }
        return picked;
    }

    private void cast(SpellData data) {
        // A mob has no mana pool or cooldown ledger of its own, so the spell gets a throwaway one.
        MagicData magicData = new MagicData(false);
        data.getSpell().onCast(mage.level(), data.getLevel(), mage, CastSource.MOB, magicData);
    }

    /**
     * Points the whole mage at its target, body and head, so the spell leaves along its look vector.
     * The look control alone only turns the head.
     */
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
