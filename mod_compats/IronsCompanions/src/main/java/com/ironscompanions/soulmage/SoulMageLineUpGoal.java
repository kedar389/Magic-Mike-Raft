package com.ironscompanions.soulmage;

import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Walks the Soul Mage into a clear line on its target, since {@link SoulMageIronsSpellGoal} will not cast
 * without one. Companions gives the mage no goal that approaches a target at all — only follow-owner and
 * stroll, both at priority 4 — so without this a target behind a tree ends the fight.
 *
 * <p>Holding {@code Flag.MOVE} is also what keeps a sitting mage seated: {@code SitWhenOrderedToGoal}
 * holds the same flag at priority 1.
 */
public class SoulMageLineUpGoal extends Goal {

    /** How far the mage will chase a line. Past this it stays with its owner instead. */
    private static final double MAX_RANGE = 24d;

    /** Ticks between paths. A target that is moving needs a fresh one; every tick is wasted work. */
    private static final int REPATH_INTERVAL = 10;

    private static final double SPEED = 1.0d;

    private final SoulMageEntity mage;
    private int repathIn;

    public SoulMageLineUpGoal(SoulMageEntity mage) {
        this.mage = mage;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mage.getTarget();
        return target != null && target.isAlive()
                && mage.distanceToSqr(target) <= MAX_RANGE * MAX_RANGE
                && !mage.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repathIn = 0;
    }

    @Override
    public void stop() {
        mage.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = mage.getTarget();
        if (target == null || --repathIn > 0) {
            return;
        }
        repathIn = REPATH_INTERVAL;
        mage.getNavigation().moveTo(target, SPEED);
    }
}
