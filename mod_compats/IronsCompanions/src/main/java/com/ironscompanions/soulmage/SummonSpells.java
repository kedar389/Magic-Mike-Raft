package com.ironscompanions.soulmage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Set;

/**
 * The summoning spells, which the Soul Mage will not cast. IS marks them in no way: every one is a plain
 * {@code AbstractSpell} and the shared machinery ({@code SummonManager}, {@code IMagicSummon}) is only
 * reached at cast time. The list came from a bytecode scan of every spell mod in the pack.
 *
 * <p>{@link #NAME_HINTS} catches ones added later. It would not catch
 * {@code gtbcs_geomancy_plus:solar_chorus}, which is why the list is explicit.
 */
public final class SummonSpells {

    /** Verified summoners, by spell id. {@code summon_ender_chest} summons no minion but is no use to a mob either. */
    private static final Set<String> SUMMON_IDS = Set.of(
            "irons_spellbooks:raise_dead",
            "irons_spellbooks:summon_vex",
            "irons_spellbooks:summon_horse",
            "irons_spellbooks:summon_polar_bear",
            "irons_spellbooks:summon_swords",
            "irons_spellbooks:summon_ender_chest",
            "gtbcs_geomancy_plus:solar_chorus");

    /** Matched against the spell id's path, for summons this list has not seen. */
    private static final Set<String> NAME_HINTS = Set.of("summon", "conjure", "minion", "raise_dead");

    private SummonSpells() {
    }

    /** Whether {@code spell} summons, and so is one the Soul Mage skips. */
    public static boolean isSummon(AbstractSpell spell) {
        if (SUMMON_IDS.contains(spell.getSpellId())) {
            return true;
        }
        String name = spell.getSpellName();
        for (String hint : NAME_HINTS) {
            if (name.contains(hint)) {
                return true;
            }
        }
        return false;
    }
}
