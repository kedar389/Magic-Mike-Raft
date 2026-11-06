package com.rendertweaks;

import net.minecraftforge.fml.common.Mod;

/** Client-only. All work happens in mixins; this class exists so Forge loads the mod. */
@Mod(RenderTweaks.MOD_ID)
public final class RenderTweaks {
    public static final String MOD_ID = "render_tweaks";
}
