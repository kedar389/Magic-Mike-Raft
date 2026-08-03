package com.ironscompanions.compat;

import com.ironscompanions.CompanionBooks;
import com.ironscompanions.IronsCompanions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Hides the removed Companions magic books from JEI. They have no recipes left (see
 * {@link com.ironscompanions.event.CompanionBookRemovalEvents}) and cannot be obtained, so leaving
 * them in the item list is just noise.
 *
 * <p>JEI is an optional client-side dependency. This class is only ever loaded by JEI's own
 * {@code @JeiPlugin} scan, which does not happen on a dedicated server, so nothing else in the mod
 * may reference it.
 */
@JeiPlugin
public class IronsCompanionsJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(IronsCompanions.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    /**
     * VERIFY(build): {@code IIngredientManager#removeIngredientsAtRuntime} is the JEI 15.x (1.20.1)
     * hiding API, matching the installed jei-1.20.1-forge-15.20.0.112.
     */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        List<Item> books = CompanionBooks.items();
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
