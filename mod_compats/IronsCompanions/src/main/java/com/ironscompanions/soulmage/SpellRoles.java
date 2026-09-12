package com.ironscompanions.soulmage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Set;

/**
 * What each spell is for, as far as the Soul Mage is concerned. Anything unlisted is an attack with reach,
 * which is the default and covers most of the registry.
 *
 * <p>Decided per spell from its own guide text and its {@code onCast}. Summons are handled separately, in
 * {@link SummonSpells}.
 */
public final class SpellRoles {

    /** Does nothing in a mob's hands: player utility, travel, or a buff to attacks it never makes. */
    private static final Set<String> WASTED = Set.of(
            "irons_spellbooks:none",
            "irons_spellbooks:portal",              // player travel
            "irons_spellbooks:recall",              // warps to the player's spawn
            "irons_spellbooks:pocket_dimension",    // player inventory space
            "irons_spellbooks:summon_ender_chest",  // player inventory
            "irons_spellbooks:touch_dig",           // breaks a block
            "irons_spellbooks:spectral_hammer",     // mines stone
            "irons_spellbooks:planar_sight",        // sight through walls
            "irons_spellbooks:throw",               // throws the held item
            "irons_spellbooks:gluttony",            // food into mana; a mob has neither
            "irons_spellbooks:angel_wing",          // elytra wings, on a mob that walks
            "irons_spellbooks:ascension",           // launches the caster into the air
            "irons_spellbooks:echoing_strikes",     // buffs non-magical attacks; it only casts
            "irons_spellbooks:sacrifice",           // detonates your own summons; it has none
            "gtbcs_geomancy_plus:seismic_surf");    // a rideable boulder

    /** Gets the mage out of trouble. */
    private static final Set<String> ESCAPE = Set.of(
            "irons_spellbooks:teleport",
            "irons_spellbooks:blood_step",
            "irons_spellbooks:frost_step",
            "irons_spellbooks:thunder_step",
            "irons_spellbooks:burning_dash",
            "irons_spellbooks:scapegoat",           // decoy that pulls aggro off it
            "irons_spellbooks:invisibility");       // mobs lose aggro; breaks when it next deals damage

    /** Heals the caster and nobody else. */
    private static final Set<String> SELF_HEAL = Set.of(
            "irons_spellbooks:heal",
            "irons_spellbooks:greater_heal");

    /** Heals or shields everyone near the caster. */
    private static final Set<String> AREA_ALLY_HEAL = Set.of(
            "irons_spellbooks:healing_circle",
            "irons_spellbooks:cloud_of_regeneration",
            "irons_spellbooks:fortify",
            "irons_companions:heal_ring",
            "gtbcs_geomancy_plus:geo_conductor",
            "gtbcs_geomancy_plus:crystalline_conduit");

    /** Heals one creature, read from the cast data, so the mage can aim it at the hurt ally. */
    private static final Set<String> TARGETED_ALLY_HEAL = Set.of("irons_spellbooks:blessing_of_life");

    /** Clears harmful effects from the caster and nearby allies. */
    private static final Set<String> CLEANSE = Set.of("irons_spellbooks:cleanse");

    /** Worth one cast as a fight opens, and nothing after that. */
    private static final Set<String> BUFF = Set.of(
            "irons_spellbooks:oakskin",
            "irons_spellbooks:abyssal_shroud",
            "irons_spellbooks:heartstop",
            "irons_spellbooks:evasion",
            "irons_spellbooks:charge",
            "irons_spellbooks:spider_aspect",
            "irons_spellbooks:frostbite",
            "irons_spellbooks:shield",
            "irons_spellbooks:haste",
            "gtbcs_geomancy_plus:tremor_step",
            "gtbcs_geomancy_plus:weeping_wall");

    /** Cones, sweeps and anything else that goes off around the caster. */
    private static final Set<String> SHORT_RANGE = Set.of(
            "irons_spellbooks:cone_of_cold",
            "irons_spellbooks:fire_breath",
            "irons_spellbooks:poison_breath",
            "irons_spellbooks:dragon_breath",
            "irons_spellbooks:electrocute",
            "irons_spellbooks:frostwave",
            "irons_spellbooks:heat_surge",
            "irons_spellbooks:stomp",
            "irons_spellbooks:devour",
            "irons_spellbooks:blood_needles",
            "irons_spellbooks:shadow_slash",
            "irons_spellbooks:volt_strike",
            "irons_spellbooks:divine_smite",
            "irons_spellbooks:flaming_strike",
            "irons_spellbooks:raise_hell",
            "irons_spellbooks:gust",
            "irons_spellbooks:fang_ward",
            "irons_spellbooks:fang_strike",
            "irons_spellbooks:wall_of_fire",
            "irons_spellbooks:sculk_tentacles",
            "irons_spellbooks:void_tentacles",
            "gtbcs_geomancy_plus:tremor_spike");

    private static final String COUNTERSPELL = "irons_spellbooks:counterspell";

    /** Dyes a sheep. That is the whole spell. */
    private static final String WOLOLO = "irons_spellbooks:wololo";

    private static final double SHORT_RANGE_BLOCKS = 8d;

    private SpellRoles() {
    }

    /** Whether casting this as a mob does nothing whatsoever. */
    public static boolean isWasted(AbstractSpell spell) {
        return WASTED.contains(spell.getSpellId());
    }

    public static boolean isEscape(AbstractSpell spell) {
        return ESCAPE.contains(spell.getSpellId());
    }

    public static boolean isSelfHeal(AbstractSpell spell) {
        return SELF_HEAL.contains(spell.getSpellId());
    }

    public static boolean isAreaAllyHeal(AbstractSpell spell) {
        return AREA_ALLY_HEAL.contains(spell.getSpellId());
    }

    /** Aimed at one ally through {@code TargetEntityCastData}, so the caller must set a target. */
    public static boolean isTargetedAllyHeal(AbstractSpell spell) {
        return TARGETED_ALLY_HEAL.contains(spell.getSpellId());
    }

    public static boolean isCleanse(AbstractSpell spell) {
        return CLEANSE.contains(spell.getSpellId());
    }

    public static boolean isBuff(AbstractSpell spell) {
        return BUFF.contains(spell.getSpellId());
    }

    public static boolean isCounterspell(AbstractSpell spell) {
        return COUNTERSPELL.equals(spell.getSpellId());
    }

    public static boolean isWololo(AbstractSpell spell) {
        return WOLOLO.equals(spell.getSpellId());
    }

    /** How far the target may be for this spell to be worth casting, in blocks. */
    public static double maxRange(AbstractSpell spell) {
        return SHORT_RANGE.contains(spell.getSpellId()) ? SHORT_RANGE_BLOCKS : Double.MAX_VALUE;
    }

    /** Everything with its own moment, which the attack rotation must not draw from. */
    public static boolean isSituational(AbstractSpell spell) {
        return isEscape(spell) || isSelfHeal(spell) || isAreaAllyHeal(spell) || isTargetedAllyHeal(spell)
                || isCleanse(spell) || isBuff(spell) || isCounterspell(spell) || isWololo(spell);
    }
}
