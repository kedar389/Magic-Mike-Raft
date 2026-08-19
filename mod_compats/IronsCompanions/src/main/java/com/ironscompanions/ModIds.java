package com.ironscompanions;

import net.minecraft.resources.ResourceLocation;

/**
 * The mod ids this compat names, and the {@link ResourceLocation} factories that go with them.
 *
 * <p>Every id we build for another mod goes through here, so a namespace is spelled out exactly once and
 * a typo is a compile error rather than a silently missing lookup.
 */
public final class ModIds {

    public static final String COMPANIONS = "companions";
    public static final String IRONS_SPELLBOOKS = "irons_spellbooks";
    /** Owns the {@code geo} spell school used by Stone Spikes. */
    public static final String GEOMANCY_PLUS = "gtbcs_geomancy_plus";

    private ModIds() {
    }

    public static ResourceLocation companions(String path) {
        return new ResourceLocation(COMPANIONS, path);
    }

    public static ResourceLocation ironsSpells(String path) {
        return new ResourceLocation(IRONS_SPELLBOOKS, path);
    }

    public static ResourceLocation geomancy(String path) {
        return new ResourceLocation(GEOMANCY_PLUS, path);
    }
}
