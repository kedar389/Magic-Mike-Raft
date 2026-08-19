package com.ironscompanions.books;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.ItemIds;
import com.ironscompanions.ModIds;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;

/**
 * The eight Companions magic books this pack takes out of circulation, in one place.
 *
 * <p>Three are replaced by the spells in {@code spell.IronsCompanionsSpells} and five are cut, so none
 * of them stay obtainable: recipes are stripped by {@code recipe.RemovedBookRecipes}, chest loot by
 * {@link RemovedBookLoot}, Minion handouts by {@link RemovedBookDrops}, and the items are hidden from
 * JEI by {@code compat.IronsCompanionsJeiPlugin}.
 *
 * <p>Companions is not forked, so the registry entries still exist. They are merely unreachable.
 *
 * <p>{@link ItemIds#NAGINATA_BOOK} is deliberately not in this set: it keeps Companions' own recipe and
 * becomes an Iron's Spells spellbook instead (see {@code mixin/NaginataBookSpellbookMixin}).
 */
public final class RemovedBooks {

    private static final List<String> PATHS = List.of(
            // Converted to spells.
            "book_ice_tornado",
            "book_stone_spikes",
            "book_heal_ring",
            // Cut outright.
            "book_ice_shard",
            "book_magic_ray",
            "book_fire_mark",
            "book_brace",
            "book_black_hole");

    /**
     * The resolved items, looked up once. An id Companions no longer ships is skipped rather than fatal,
     * so a rename upstream degrades to "that one book is not removed".
     *
     * <p>Identity-hashed, which is what lets {@link #contains} sit on the entity-join path.
     */
    private static final Lazy<Set<Item>> ITEMS = Lazy.concurrentOf(RemovedBooks::resolve);

    private RemovedBooks() {
    }

    /** Whether {@code item} is one of the removed books. */
    public static boolean contains(Item item) {
        return ITEMS.get().contains(item);
    }

    /** The removed books that are actually registered, in declaration order. */
    public static Set<Item> items() {
        return ITEMS.get();
    }

    private static Set<Item> resolve() {
        Set<Item> items = new LinkedHashSet<>(PATHS.size());
        for (String path : PATHS) {
            ResourceLocation id = ModIds.companions(path);
            Item item = ItemIds.resolve(id);
            if (item != null) {
                items.add(item);
            } else {
                IronsCompanions.LOGGER.debug("[IronsCompanions] book {} is not registered, skipping", id);
            }
        }
        return Collections.unmodifiableSet(items);
    }
}
