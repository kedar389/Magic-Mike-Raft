package com.ironscompanions.recipe;

import net.minecraft.world.item.crafting.Recipe;

/**
 * One edit this mod makes to the loaded recipe set. Rules are created fresh for each sweep by
 * {@link RecipeRewrites} and see every recipe exactly once, in one pass.
 */
interface RecipeRule {

    /**
     * Applies this rule to one recipe.
     *
     * @param recipe the recipe as the previous rules left it
     * @return the recipe to keep, which may be {@code recipe} itself (edited in place or untouched) or
     *         a rebuilt replacement, or null to drop it
     */
    Recipe<?> apply(Recipe<?> recipe);

    /**
     * Reports what this rule did, once the sweep is over.
     *
     * @return one line for the log, or null when this rule changed nothing
     */
    String summary();
}
