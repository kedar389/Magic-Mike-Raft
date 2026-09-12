package com.ironscompanions.attribute;

import com.ironscompanions.ModIds;
import net.minecraft.resources.ResourceLocation;

/** The Iron's Spells attributes this mod grants from Companions gear. */
final class IronsSpellsAttributes {

    static final ResourceLocation MAX_MANA = ModIds.ironsSpells("max_mana");
    static final ResourceLocation SPELL_POWER = ModIds.ironsSpells("spell_power");
    static final ResourceLocation SUMMON_DAMAGE = ModIds.ironsSpells("summon_damage");
    static final ResourceLocation HOLY_SPELL_POWER = ModIds.ironsSpells("holy_spell_power");
    static final ResourceLocation BLOOD_MAGIC_RESIST = ModIds.ironsSpells("blood_magic_resist");

    private IronsSpellsAttributes() {
    }
}
