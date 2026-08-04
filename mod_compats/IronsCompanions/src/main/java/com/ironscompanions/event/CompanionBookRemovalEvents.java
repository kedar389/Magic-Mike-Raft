package com.ironscompanions.event;

import com.ironscompanions.CompanionBooks;
import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.SpellScrolls;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
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

    /** Level the swapped-in scrolls are inscribed at. */
    private static final int SWAP_SPELL_LEVEL = 1;

    /**
     * A random <b>common</b> scroll, drawn from the same lootable pool Iron's Spells' own chest loot uses
     * ({@code isEnabled() && allowLooting()}).
     *
     * <p>Not a 1:1 book-to-its-spell swap: Companions rolls from a fixed pair per Minion variant (an
     * overworld Minion gives ice_shard or ice_tornado, 50/50), so a 1:1 map would hand out the same
     * scroll half the time. Restricting to common keeps a Minion from being a high-tier scroll farm.
     */
    private static ItemStack randomScroll(RandomSource random) {
        AbstractSpell spell = new SpellFilter().getRandomSpell(random, CompanionBookRemovalEvents::isCommon);
        if (spell == null || spell.equals(SpellRegistry.none())) {
            return ItemStack.EMPTY;
        }
        return SpellScrolls.of(spell, SWAP_SPELL_LEVEL);
    }

    private static boolean isCommon(AbstractSpell spell) {
        return spell.getMinRarity() == SpellRarity.COMMON.ordinal();
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        ResourceLocation bookId = ForgeRegistries.ITEMS.getKey(itemEntity.getItem().getItem());
        if (bookId == null || !CompanionBooks.isRemoved(bookId)) {
            return;
        }

        ItemStack replacement = randomScroll(itemEntity.level().getRandom());

        if (replacement.isEmpty()) {
            itemEntity.discard();
            return;
        }
        // Swap the stack in place. Discarding a just-joined ItemEntity leaves a client-side ghost that
        // renders but cannot be picked up.
        itemEntity.setItem(replacement);
        IronsCompanions.LOGGER.debug("[IronsCompanions] Swapped dropped {} for {}", bookId, replacement);
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
