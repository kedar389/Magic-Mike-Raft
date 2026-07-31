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
 * Adds {@link SoulMageIronsSpellGoal} to every Soul Mage as it joins the world. Uses reflection on
 * {@code Mob.goalSelector} (SRG {@code f_21345_}) so no Mixin/refmap is needed, consistent with the
 * rest of this mod being event-driven.
 *
 * <p>The Companions book goals are left in place; with the books removed they never fire, so they
 * simply coexist inertly with the IS-casting goal (both at priority 3).
 */
public class SoulMageAiEvents {

    private static final Field GOAL_SELECTOR = resolveGoalSelector();

    // Match the Companions book goals' cooldown window (registerGoals uses 70..160 ticks).
    private static final int MIN_COOLDOWN = 70;
    private static final int MAX_COOLDOWN = 160;

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof SoulMageEntity mage)) {
            return;
        }
        GoalSelector goals = goalSelectorOf(mage);
        if (goals != null) {
            goals.addGoal(3, new SoulMageIronsSpellGoal(mage, MIN_COOLDOWN, MAX_COOLDOWN));
        }
    }

    private static Field resolveGoalSelector() {
        try {
            return ObfuscationReflectionHelper.findField(Mob.class, "f_21345_"); // goalSelector
        } catch (Exception e) {
            IronsCompanions.LOGGER.error("[IronsCompanions] Could not resolve Mob.goalSelector; Soul Mage IS-casting disabled", e);
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
