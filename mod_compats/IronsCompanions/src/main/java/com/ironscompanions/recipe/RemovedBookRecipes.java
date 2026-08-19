package com.ironscompanions.recipe;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.books.RemovedBooks;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Drops every recipe that produces one of the {@link RemovedBooks}, so they cannot be crafted.
 */
final class RemovedBookRecipes implements RecipeRule {

    private final Set<Item> books;
    private final RegistryAccess registries;
    private int removed;

    private RemovedBookRecipes(Set<Item> books, RegistryAccess registries) {
        this.books = books;
        this.registries = registries;
    }

    /**
     * Builds the rule over the books that actually resolved.
     *
     * @return the rule, or empty when Companions is absent or every book id has drifted
     */
    static Optional<RecipeRule> create(RegistryAccess registries) {
        Set<Item> books = RemovedBooks.items();
        return books.isEmpty() ? Optional.empty() : Optional.of(new RemovedBookRecipes(books, registries));
    }

    @Override
    public Recipe<?> apply(Recipe<?> recipe) {
        if (!books.contains(resultItem(recipe))) {
            return recipe;
        }
        removed++;
        return null;
    }

    @Override
    public String summary() {
        return removed == 0 ? null : "Removed " + removed + " recipe(s) for Companions magic books.";
    }

    /**
     * Some modded recipes build their result lazily and can throw when asked for it outside their own
     * context. One bad recipe must not abort the sweep, so a failure is treated as "not a book".
     */
    private Item resultItem(Recipe<?> recipe) {
        try {
            return recipe.getResultItem(registries).getItem();
        } catch (Throwable t) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] Could not read result of recipe {}: {}",
                    recipe.getId(), t.toString());
            return Items.AIR;
        }
    }
}
