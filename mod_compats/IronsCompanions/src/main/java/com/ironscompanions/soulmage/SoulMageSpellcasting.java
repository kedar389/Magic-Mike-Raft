package com.ironscompanions.soulmage;

import com.ironscompanions.IronsCompanions;
import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import java.lang.reflect.Field;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Adds {@link SoulMageIronsSpellGoal} to every Soul Mage as it joins the world.
 *
 * <p>{@code Mob.goalSelector} is protected, and reflecting on it (SRG {@code f_21345_}) keeps this
 * feature event-driven rather than adding a fourth mixin or an access transformer for one field. The
 * lookup happens once, at class load; only the read is per Soul Mage.
 *
 * <p>The Companions book goals are left in place. Their {@code hasRequiredBook()} can no longer pass
 * once the books are removed, so they never fire and simply coexist inertly with the spell goal, at the
 * same priority.
 */
public final class SoulMageSpellcasting {

    private static final int GOAL_PRIORITY = 3;

    /** Matches the Companions book goals' own cooldown window ({@code registerGoals} uses 70..160 ticks). */
    private static final int MIN_COOLDOWN = 70;
    private static final int MAX_COOLDOWN = 160;

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
        }
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
