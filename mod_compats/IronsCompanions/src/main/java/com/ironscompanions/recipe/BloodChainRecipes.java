package com.ironscompanions.recipe;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.ItemIds;
import com.ironscompanions.ModIds;
import dev.xylonity.companions.common.recipe.SoulFurnaceRecipe;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Ties Companions' <b>Crystallized Blood</b> to Iron's Spells' <b>Blood Vial</b>, both ways: the Soul
 * Furnace recipe that produces Crystallized Blood takes a {@code blood_vial} instead of its shipped
 * rotten flesh, at the same charge cost and process time, and every Iron's Spells crafting-table recipe
 * that asks for a {@code blood_vial} asks for Crystallized Blood instead (today: the Netherite Spell
 * Book, which uses two).
 *
 * <p>That furnace recipe is Crystallized Blood's only source, so the whole line — the armour, both
 * weapons, the Shade altars, the Shadow Bell, this mod's blood-resistance bridge — runs on Blood Vials.
 *
 * <p>Only shaped and shapeless recipes are touched. The vial's own fill and empty recipes are neither,
 * so obtaining a Blood Vial is unchanged and the chain has a start.
 */
final class BloodChainRecipes implements RecipeRule {

    private final Item bloodVial;
    private final Item crystallizedBlood;
    private final Ingredient bloodVialInput;

    private int furnaceSwaps;
    private int craftingSwaps;

    private BloodChainRecipes(Item bloodVial, Item crystallizedBlood) {
        this.bloodVial = bloodVial;
        this.crystallizedBlood = crystallizedBlood;
        this.bloodVialInput = Ingredient.of(bloodVial);
    }

    /**
     * Builds the rule, resolving both ends of the chain up front.
     *
     * @return the rule, or empty when either of the two mods is absent
     */
    static Optional<RecipeRule> create() {
        Item bloodVial = ItemIds.resolve(ItemIds.BLOOD_VIAL);
        Item crystallizedBlood = ItemIds.resolve(ItemIds.CRYSTALLIZED_BLOOD);
        return bloodVial == null || crystallizedBlood == null
                ? Optional.empty()
                : Optional.of(new BloodChainRecipes(bloodVial, crystallizedBlood));
    }

    @Override
    public Recipe<?> apply(Recipe<?> recipe) {
        if (recipe instanceof SoulFurnaceRecipe soulFurnace
                && soulFurnace.outputsItem() && soulFurnace.resultItem() == crystallizedBlood) {
            furnaceSwaps++;
            return withInput(soulFurnace, bloodVialInput);
        }
        if (isIronsSpellsCrafting(recipe) && swapIngredient(recipe, bloodVial, crystallizedBlood)) {
            craftingSwaps++;
        }
        return recipe;
    }

    @Override
    public String summary() {
        if (furnaceSwaps == 0 && craftingSwaps == 0) {
            return null;
        }
        return "Blood chain: " + furnaceSwaps + " Soul Furnace recipe(s) now take a Blood Vial, "
                + craftingSwaps + " Iron's Spells recipe(s) now take Crystallized Blood.";
    }

    /** Same recipe, same id, same cost, different input. Companions exposes every field as an accessor. */
    private static SoulFurnaceRecipe withInput(SoulFurnaceRecipe recipe, Ingredient input) {
        return new SoulFurnaceRecipe(recipe.getId(), input, recipe.requiredCharges(), recipe.processTime(),
                recipe.resultItem(), recipe.resultCount(), recipe.resultEntity(), recipe.resultBlock());
    }

    private static boolean isIronsSpellsCrafting(Recipe<?> recipe) {
        return ModIds.IRONS_SPELLBOOKS.equals(recipe.getId().getNamespace())
                && (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe);
    }

    /**
     * Replaces every ingredient that is exactly {@code from} with {@code to}, in place. Shaped and
     * shapeless recipes both return their live ingredient list, so a {@code set} is enough and nothing
     * has to be rebuilt. The result is read back because a recipe class returning a copy would otherwise
     * fail silently.
     */
    private static boolean swapIngredient(Recipe<?> recipe, Item from, Item to) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        boolean swapped = false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (isExactly(ingredients.get(i), from)) {
                try {
                    ingredients.set(i, Ingredient.of(to));
                } catch (RuntimeException e) {
                    IronsCompanions.LOGGER.warn("[IronsCompanions] Could not edit the ingredients of {}: {}",
                            recipe.getId(), e.toString());
                    return false;
                }
                swapped = true;
            }
        }
        if (!swapped) {
            return false;
        }
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (isExactly(ingredient, from)) {
                IronsCompanions.LOGGER.warn("[IronsCompanions] {} still asks for {}: its ingredient list is a "
                        + "copy, so it needs rebuilding rather than editing.", recipe.getId(), from);
                return false;
            }
        }
        return true;
    }

    /**
     * True only for an ingredient that means that one item. A tag-based ingredient that merely contains
     * it (say {@code irons_spellbooks:blood_focus}) is left alone: the point is replacing a required
     * item, not shrinking a tag.
     */
    private static boolean isExactly(Ingredient ingredient, Item item) {
        try {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length == 0) {
                return false;
            }
            for (ItemStack stack : stacks) {
                if (stack.getItem() != item) {
                    return false;
                }
            }
            return true;
        } catch (RuntimeException e) {
            return false; // an ingredient that cannot be resolved is not the one we are looking for
        }
    }
}
