package com.ironscompanions.event;

import com.ironscompanions.CompanionBooks;
import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.SpellScrolls;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Makes the removed Companions magic books unobtainable: no recipes, and no Minion handouts.
 *
 * <p><b>Minion rewards.</b> {@code MinionEntity#mobInteract} pops one of six books
 * (ice_shard, ice_tornado, brace, fire_mark, heal_ring, stone_spikes) as a reward via
 * {@code Block.popResource}, which no recipe strip can touch. Since it arrives as an
 * {@link ItemEntity}, we intercept it on join: a converted book becomes its scroll, a cut book is
 * discarded. This also upgrades any legacy book a player still has once they drop it.
 *
 * <p>Forge has no "remove recipe" API, so we filter the {@link RecipeManager} after it has loaded.
 * Listeners added through {@link AddReloadListenerEvent} are appended after the vanilla ones, so our
 * apply stage runs once the recipe manager holds the fully parsed set. Matching on the output item
 * (rather than on recipe ids) catches the recipe whatever path or recipe type Companions uses, which
 * is what the old KubeJS {@code event.remove({output: id})} did.
 *
 * <p>Runs on every datapack load and on {@code /reload}.
 */
public class CompanionBookRemovalEvents {

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        RecipeManager recipeManager = event.getServerResources().getRecipeManager();
        RegistryAccess registries = event.getRegistryAccess();
        event.addListener((ResourceManagerReloadListener) resourceManager ->
                stripBookRecipes(recipeManager, registries));
    }

    /**
     * VERIFY(build): {@code RecipeManager#getRecipes()} and {@code #replaceRecipes(Iterable)} are both
     * public in 1.20.1 official mappings (the client uses the latter when the server syncs recipes).
     * Confirm at compile time; if {@code replaceRecipes} turns out to be unavailable, the fallback is
     * reflection on the {@code recipes} / {@code byName} maps.
     */
    private static void stripBookRecipes(RecipeManager recipeManager, RegistryAccess registries) {
        Set<Item> books = Set.copyOf(CompanionBooks.items());
        if (books.isEmpty()) {
            return; // Companions absent, or every id drifted: nothing to strip.
        }

        List<Recipe<?>> kept = new ArrayList<>();
        int removed = 0;
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            if (books.contains(resultItem(recipe, registries))) {
                removed++;
            } else {
                kept.add(recipe);
            }
        }

        if (removed == 0) {
            return;
        }
        recipeManager.replaceRecipes(kept);
        IronsCompanions.LOGGER.info("[IronsCompanions] Removed {} recipe(s) for Companions magic books.", removed);
    }

    /** Level the swapped-in scrolls are inscribed at (matches the chest-loot scrolls). */
    private static final int SWAP_SPELL_LEVEL = 1;

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        ResourceLocation bookId = ForgeRegistries.ITEMS.getKey(itemEntity.getItem().getItem());
        if (bookId == null || !CompanionBooks.isRemoved(bookId)) {
            return;
        }

        RegistryObject<AbstractSpell> spell = CompanionBooks.spellFor(bookId);
        if (spell == null) {
            // Cut book, nothing replaces it (README 5B).
            IronsCompanions.LOGGER.debug("[IronsCompanions] Discarded dropped cut book {}", bookId);
            itemEntity.discard();
            return;
        }
        itemEntity.setItem(SpellScrolls.of(spell.get(), SWAP_SPELL_LEVEL));
        IronsCompanions.LOGGER.debug("[IronsCompanions] Swapped dropped {} for its scroll", bookId);
    }

    /**
     * Some modded recipes build their result lazily and can throw when asked for it outside their own
     * context. One bad recipe must not abort the sweep, so failures are treated as "not a book".
     */
    private static Item resultItem(Recipe<?> recipe, RegistryAccess registries) {
        try {
            return recipe.getResultItem(registries).getItem();
        } catch (Throwable t) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] Could not read result of recipe {}: {}",
                    recipe.getId(), t.toString());
            return Items.AIR;
        }
    }
}
