package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * Builds Iron's Spells scrolls for our spells.
 *
 * <p>Iron's Spells has one generic Scroll item and stores the spell in NBT, so there is no per-spell
 * item to register. {@code createScrollContainer} both builds the container and writes it onto the
 * stack, so the returned stack is ready to hand out as loot.
 */
public final class SpellScrolls {

    private SpellScrolls() {
    }

    public static ItemStack of(AbstractSpell spell, int spellLevel) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spellLevel, stack);
        return stack;
    }
}
