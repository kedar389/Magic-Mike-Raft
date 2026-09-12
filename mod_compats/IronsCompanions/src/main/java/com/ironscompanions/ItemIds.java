package com.ironscompanions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Items from the two bridged mods that more than one feature names, and the one safe way to turn an id
 * into an {@link Item}.
 *
 * <p>Resolution must be deferred: these constants are read while the mod is being constructed, long
 * before the item registry is frozen. {@link #lazy} does the lookup on first use, which is always
 * in-game, and remembers the answer, {@code null} included.
 *
 * <p>Comparing a resolved {@link Item} by identity is also what keeps the hot event handlers off
 * {@code ForgeRegistries.ITEMS.getKey}, a reverse registry lookup they would otherwise do per call.
 */
public final class ItemIds {

    /** Kept obtainable and turned into a spellbook, unlike the books in {@code books.RemovedBooks}. */
    public static final ResourceLocation NAGINATA_BOOK = ModIds.companions("book_naginata");

    /** Companions' taming reagent for the Soul Mage, turned into an Iron's Spells staff. */
    public static final ResourceLocation MAGE_STAFF = ModIds.companions("mage_staff");

    public static final ResourceLocation CRYSTALLIZED_BLOOD = ModIds.companions("crystallized_blood");
    public static final ResourceLocation BLOOD_VIAL = ModIds.ironsSpells("blood_vial");

    private ItemIds() {
    }

    /** The item registered under {@code id}, or null when there is none. Never {@code minecraft:air}. */
    public static Item resolve(ResourceLocation id) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? null : item;
    }

    /** {@link #resolve} deferred to first use. The result, null included, is computed once. */
    public static Lazy<Item> lazy(ResourceLocation id) {
        return Lazy.concurrentOf(() -> resolve(id));
    }
}
