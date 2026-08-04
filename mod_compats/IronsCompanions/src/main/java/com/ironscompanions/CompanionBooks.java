package com.ironscompanions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The nine Companions magic books, in one place. Four are replaced by the spells in
 * {@link com.ironscompanions.spell.IronsCompanionsSpells} and five are cut, so none stay obtainable:
 * recipes are stripped and Minion rewards swapped by
 * {@link com.ironscompanions.event.CompanionBookRemovalEvents}, and the items are hidden from JEI by
 * {@code com.ironscompanions.compat.IronsCompanionsJeiPlugin}.
 *
 * <p>Companions is not forked, so the registry entries still exist. They are merely unreachable.
 */
public final class CompanionBooks {

    private static final Set<ResourceLocation> BOOKS = new LinkedHashSet<>();

    static {
        // Converted to spells.
        add("book_ice_tornado");
        add("book_stone_spikes");
        add("book_heal_ring");
        add("book_naginata");
        // Cut outright.
        add("book_ice_shard");
        add("book_magic_ray");
        add("book_fire_mark");
        add("book_brace");
        add("book_black_hole");
    }

    private CompanionBooks() {
    }

    private static void add(String path) {
        BOOKS.add(new ResourceLocation("companions", path));
    }

    public static boolean isRemoved(ResourceLocation bookId) {
        return BOOKS.contains(bookId);
    }

    /**
     * Resolved items. An id Companions no longer ships is skipped rather than fatal, so a rename
     * upstream degrades to "that one book is not removed". Call once registries are frozen.
     */
    public static List<Item> items() {
        List<Item> items = new ArrayList<>(BOOKS.size());
        for (ResourceLocation id : BOOKS) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) {
                items.add(item);
            } else {
                IronsCompanions.LOGGER.debug("[IronsCompanions] book {} is not registered, skipping", id);
            }
        }
        return items;
    }
}
