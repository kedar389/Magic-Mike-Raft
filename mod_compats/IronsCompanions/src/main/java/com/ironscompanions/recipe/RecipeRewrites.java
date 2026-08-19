package com.ironscompanions.recipe;

import com.ironscompanions.IronsCompanions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Applies every recipe change this mod makes, in a single pass over the loaded {@link RecipeManager}.
 *
 * <p><b>Why code and not recipe files.</b> Forge has no "remove recipe" API, and overriding another
 * mod's recipe id from our own datapack depends on mod resource-pack ordering, where a silent miss
 * would leave the change off with no sign of it. Rewriting the loaded manager is order-independent, and
 * stating each change as a rule also catches recipes a future version of either mod adds. Matching on
 * the recipe's contents rather than on its id catches it whatever path or recipe type it ships under,
 * which is what the pack's old KubeJS {@code event.remove({output: id})} did.
 *
 * <p><b>Why one sweep.</b> {@link RecipeManager#getRecipes()} is every recipe in the game, several
 * thousand of them; a listener per feature would walk that list once per feature and rebuild the type
 * index once per feature. The rules are chained per recipe instead, so the cost is paid once per
 * reload no matter how many rules there are.
 *
 * <p>Listeners added through {@link AddReloadListenerEvent} run after the vanilla ones, so both the
 * recipes and the item tags are fully loaded by the time the sweep starts. It re-runs on every datapack
 * load and on {@code /reload}.
 */
public final class RecipeRewrites {

    private RecipeRewrites() {
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        RecipeManager recipes = event.getServerResources().getRecipeManager();
        RegistryAccess registries = event.getRegistryAccess();
        event.addListener((ResourceManagerReloadListener) resourceManager -> sweep(recipes, registries));
    }

    private static void sweep(RecipeManager manager, RegistryAccess registries) {
        List<RecipeRule> rules = new ArrayList<>(2);
        RemovedBookRecipes.create(registries).ifPresent(rules::add);
        BloodChainRecipes.create().ifPresent(rules::add);
        if (rules.isEmpty()) {
            return; // one of the bridged mods is absent: nothing to rewrite
        }

        List<Recipe<?>> kept = new ArrayList<>();
        boolean listChanged = false;
        for (Recipe<?> recipe : manager.getRecipes()) {
            Recipe<?> result = recipe;
            for (RecipeRule rule : rules) {
                result = rule.apply(result);
                if (result == null) {
                    break; // dropped; no later rule can have an opinion about it
                }
            }
            if (result == null) {
                listChanged = true;
                continue;
            }
            listChanged |= result != recipe;
            kept.add(result);
        }

        // Rules that edit a recipe in place have already taken effect; only a dropped or rebuilt recipe
        // makes the manager's own list stale, and replacing it rebuilds its by-type index.
        if (listChanged) {
            manager.replaceRecipes(kept);
        }
        for (RecipeRule rule : rules) {
            String summary = rule.summary();
            if (summary != null) {
                IronsCompanions.LOGGER.info("[IronsCompanions] {}", summary);
            }
        }
    }
}
