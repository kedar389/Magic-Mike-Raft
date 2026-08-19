package com.ironscompanions.books;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.SpellScrolls;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Turns a dropped removed book into a spell scroll.
 *
 * <p>{@code MinionEntity#mobInteract} pops one of six books (ice_shard, ice_tornado, brace, fire_mark,
 * heal_ring, stone_spikes) as a reward through {@code Block.popResource}, which no recipe strip or loot
 * edit can touch. Since it arrives as an {@link ItemEntity}, it is intercepted on join. This also
 * upgrades any legacy book a player still owns, the moment they drop it.
 */
public final class RemovedBookDrops {

    /** Level the swapped-in scrolls are inscribed at. */
    private static final int SCROLL_LEVEL = 1;

    /**
     * Iron's Spells' own lootable-spell pool, the one its chest loot draws from
     * ({@code isEnabled() && allowLooting()}). Built once; constructing it reads the spell registry.
     */
    private static final Lazy<SpellFilter> LOOTABLE_SPELLS = Lazy.concurrentOf(SpellFilter::new);

    private RemovedBookDrops() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        ItemStack dropped = itemEntity.getItem();
        if (!RemovedBooks.contains(dropped.getItem())) {
            return;
        }
        ItemStack replacement = randomScroll(itemEntity.level().getRandom());
        if (replacement.isEmpty()) {
            itemEntity.discard();
            return;
        }
        // Swap the stack in place. Discarding a just-joined ItemEntity and spawning a new one leaves a
        // client-side ghost that renders but cannot be picked up.
        itemEntity.setItem(replacement);
        IronsCompanions.LOGGER.debug("[IronsCompanions] Swapped dropped {} for {}", dropped, replacement);
    }

    /**
     * A random <b>common</b> scroll.
     *
     * <p>Not a 1:1 book-to-its-spell swap: Companions rolls from a fixed pair per Minion variant, an
     * overworld Minion gives ice_shard or ice_tornado 50/50, so a 1:1 map would hand out the same
     * scroll half the time. Restricting to common keeps a Minion from being a high-tier scroll farm.
     */
    private static ItemStack randomScroll(RandomSource random) {
        AbstractSpell spell = LOOTABLE_SPELLS.get().getRandomSpell(random, RemovedBookDrops::isCommon);
        if (spell == null || spell.equals(SpellRegistry.none())) {
            return ItemStack.EMPTY;
        }
        return SpellScrolls.of(spell, SCROLL_LEVEL);
    }

    /**
     * Whether the spell is still common at the level we inscribe, which is the question that actually
     * matters: {@code getRarity} answers it for the level being handed out, where the deprecated
     * {@code getMinRarity()} answered it only for the spell's floor and did so as a raw enum ordinal.
     */
    private static boolean isCommon(AbstractSpell spell) {
        return spell.getRarity(SCROLL_LEVEL) == SpellRarity.COMMON;
    }
}
