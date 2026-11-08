package com.rendertweaks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Skips each tweak whose target mod is not installed, so the jar is safe in any instance. */
public final class RenderTweaksMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE = "com.rendertweaks.mixin.";

    /** Mixin sub-package to the mod ids it needs. */
    private static final Map<String, List<String>> REQUIRED_MODS = Map.of(
            "apoli.", List.of("apoli", "mixinsquared"),
            "bagus.", List.of("bagus_lib"),
            "chloride.", List.of("chloride", "mixinsquared"),
            "gtbc.", List.of("gtbcs_spell_lib"),
            "knightlib.", List.of("knightlib"),
            "lionfish.", List.of("lionfishapi", "embeddium"),
            "pehkui.", List.of("pehkui"),
            "subtle.", List.of("subtle_effects", "mixinsquared"));

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String relative = mixinClassName.substring(MIXIN_PACKAGE.length());
        for (Map.Entry<String, List<String>> entry : REQUIRED_MODS.entrySet()) {
            if (relative.startsWith(entry.getKey())) {
                return entry.getValue().stream().allMatch(RenderTweaksMixinPlugin::isLoaded);
            }
        }
        return true;
    }

    private static boolean isLoaded(String modId) {
        LoadingModList mods = FMLLoader.getLoadingModList();
        return mods != null && mods.getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
