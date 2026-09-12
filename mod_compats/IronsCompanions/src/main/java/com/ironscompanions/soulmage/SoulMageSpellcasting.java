package com.ironscompanions.soulmage;

import com.ironscompanions.IronsCompanions;
import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import java.lang.reflect.Field;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Adds {@link SoulMageIronsSpellGoal} and {@link SoulMageLineUpGoal} to every Soul Mage as it joins
 * the world.
 *
 * <p>{@code Mob.goalSelector} is protected, and reflecting on it (SRG {@code f_21345_}) keeps this
 * feature event-driven rather than adding a fifth mixin or an access transformer for one field. The
 * lookup happens once, at class load; only the read is per Soul Mage.
 *
 * <p>The Companions book goals are left in place. Their {@code hasRequiredBook()} can no longer pass
 * once the books are removed, so they never fire and simply coexist inertly with the spell goal, at the
 * same priority.
 */
public final class SoulMageSpellcasting {

    private static final int GOAL_PRIORITY = 3;

    /** A hit worth fleeing from, as a share of the mage's maximum health. */
    private static final float BIG_HIT_FRACTION = 0.2f;

    /** How long after such a hit the mage still counts as in danger. */
    private static final int DANGER_TICKS = 60;

    /**
     * When the mage was last hit hard, kept in its persistent data rather than a map here: the entity
     * outlives any bookkeeping of ours, and the tag rides along with it through save and reload.
     */
    private static final String BIG_HIT_TAG = "irons_companions:big_hit_tick";

    /**
     * Gap between casts, 0.4s to 1.4s. With the goal's own 12-tick cast that is a spell every 1-2s, well
     * short of the Companions book goals' 70..160 ticks, which is the pace a mage with one book needs
     * rather than one holding three scrolls.
     */
    private static final int MIN_COOLDOWN = 8;
    private static final int MAX_COOLDOWN = 28;

    private static final Field GOAL_SELECTOR = resolveGoalSelector();

    private SoulMageSpellcasting() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof SoulMageEntity mage)) {
            return;
        }
        GoalSelector goals = goalSelectorOf(mage);
        if (goals != null) {
            goals.addGoal(GOAL_PRIORITY, new SoulMageIronsSpellGoal(mage, MIN_COOLDOWN, MAX_COOLDOWN));
            goals.addGoal(GOAL_PRIORITY, new SoulMageLineUpGoal(mage));
        }
    }

    /** Records a hit big enough that the mage should think about getting out rather than trading. */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof SoulMageEntity mage
                && event.getAmount() >= mage.getMaxHealth() * BIG_HIT_FRACTION) {
            mage.getPersistentData().putInt(BIG_HIT_TAG, mage.tickCount);
        }
    }

    /** Whether {@code mage} was hit hard within the last {@link #DANGER_TICKS}. */
    public static boolean tookBigHitRecently(SoulMageEntity mage) {
        if (!mage.getPersistentData().contains(BIG_HIT_TAG)) {
            return false;
        }
        int since = mage.tickCount - mage.getPersistentData().getInt(BIG_HIT_TAG);
        return since >= 0 && since < DANGER_TICKS;
    }

    private static Field resolveGoalSelector() {
        try {
            return ObfuscationReflectionHelper.findField(Mob.class, "f_21345_"); // goalSelector
        } catch (RuntimeException e) {
            IronsCompanions.LOGGER.error("[IronsCompanions] Could not resolve Mob.goalSelector; "
                    + "Soul Mage spellcasting is disabled", e);
            return null;
        }
    }

    private static GoalSelector goalSelectorOf(Mob mob) {
        if (GOAL_SELECTOR == null) {
            return null;
        }
        try {
            return (GoalSelector) GOAL_SELECTOR.get(mob);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
