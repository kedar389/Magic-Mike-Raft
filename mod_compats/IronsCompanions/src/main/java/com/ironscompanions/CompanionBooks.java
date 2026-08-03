package com.ironscompanions;

import com.ironscompanions.spell.IronsCompanionsSpells;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The nine Companions "magic books", in one place.
 *
 * <p>Four are converted into real Iron's Spells spells (the player gets a scroll instead of a book)
 * and five are cut outright, so none of them stay obtainable:
 * <ul>
 *   <li>recipes stripped by {@link com.ironscompanions.event.CompanionBookRemovalEvents},</li>
 *   <li>items hidden from JEI by {@code com.ironscompanions.compat.IronsCompanionsJeiPlugin},</li>
 *   <li>book rewards popped by a Minion swapped for the matching scroll, also in
 *       {@link com.ironscompanions.event.CompanionBookRemovalEvents}.</li>
 * </ul>
 *
 * <p>We do not fork Companions, so the registry entries still exist. See README.md section 5B.
 */
public final class CompanionBooks {

    private static final String COMPANIONS = "companions";

    /** Book id → the spell that replaces it. */
    private static final Map<ResourceLocation, RegistryObject<AbstractSpell>> CONVERTED = new LinkedHashMap<>();

    /** Cut outright, no replacement. */
    private static final Set<ResourceLocation> CUT = new LinkedHashSet<>();

    static {
        CONVERTED.put(id("book_ice_tornado"), IronsCompanionsSpells.ICE_TORNADO);
        CONVERTED.put(id("book_stone_spikes"), IronsCompanionsSpells.STONE_SPIKES);
        CONVERTED.put(id("book_heal_ring"), IronsCompanionsSpells.HEAL_RING);
        CONVERTED.put(id("book_naginata"), IronsCompanionsSpells.NAGINATA);

        CUT.add(id("book_ice_shard"));
        CUT.add(id("book_magic_ray"));
        CUT.add(id("book_fire_mark"));
        CUT.add(id("book_brace"));
        CUT.add(id("book_black_hole"));
    }

    private CompanionBooks() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(COMPANIONS, path);
    }

    public static List<ResourceLocation> ids() {
        List<ResourceLocation> ids = new ArrayList<>(CONVERTED.size() + CUT.size());
        ids.addAll(CONVERTED.keySet());
        ids.addAll(CUT);
        return ids;
    }

    /** The spell that replaces this book, or null if the book was cut (or is not one of ours). */
    public static RegistryObject<AbstractSpell> spellFor(ResourceLocation bookId) {
        return CONVERTED.get(bookId);
    }

    public static boolean isRemoved(ResourceLocation bookId) {
        return CONVERTED.containsKey(bookId) || CUT.contains(bookId);
    }

    /**
     * Resolved items. Ids that Companions no longer ships are skipped rather than fatal, so a
     * Companions update that renames a book degrades to "that one book is not removed" instead of a
     * crash. Call after registries are frozen.
     */
    public static List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (ResourceLocation id : ids()) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) {
                items.add(item);
            } else {
                IronsCompanions.LOGGER.debug("[IronsCompanions] book {} not in the item registry, skipping", id);
            }
        }
        return items;
    }
}
