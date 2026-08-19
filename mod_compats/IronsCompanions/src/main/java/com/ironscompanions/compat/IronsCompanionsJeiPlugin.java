package com.ironscompanions.compat;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.books.RemovedBooks;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Hides the removed Companions magic books from JEI. They have no recipes left (see
 * {@code recipe.RemovedBookRecipes}) and cannot be obtained, so leaving them in the item list is just
 * noise.
 *
 * <p>JEI is an optional client-side dependency. This class is only ever loaded by JEI's own
 * {@code @JeiPlugin} scan, which does not happen on a dedicated server, so nothing else in the mod may
 * reference it.
 */
@JeiPlugin
public class IronsCompanionsJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(IronsCompanions.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        Set<Item> books = RemovedBooks.items();
        if (books.isEmpty()) {
            return;
        }
        List<ItemStack> hidden = new ArrayList<>(books.size());
        for (Item book : books) {
            hidden.add(new ItemStack(book));
        }
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
        IronsCompanions.LOGGER.info("[IronsCompanions] Hid {} Companions magic book(s) from JEI.", hidden.size());
    }
}
