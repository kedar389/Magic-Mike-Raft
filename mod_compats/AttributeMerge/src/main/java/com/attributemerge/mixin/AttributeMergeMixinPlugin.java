package com.attributemerge.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Skips {@link CataclysmCuriosMixin} when Cataclysm is not installed.
 *
 * <p>Without this, a mixin targeting a class that does not exist is an error at class-load time.
 * The rest of the mod resolves everything by {@code ResourceLocation} and no-ops when a mod is
 * absent, so the mixin must degrade the same way.
 *
 * <p>{@link LoadingModList} is used rather than {@code ModList}: mixin configs are queried during
 * class loading, well before {@code ModList} exists.
 */
public class AttributeMergeMixinPlugin implements IMixinConfigPlugin {

    private static final String CATACLYSM = "cataclysm";

    private boolean cataclysmPresent;

    @Override
    public void onLoad(String mixinPackage) {
        cataclysmPresent = LoadingModList.get().getModFileById(CATACLYSM) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return cataclysmPresent;
    }

    @Override
    public String getRefMapperConfig() {
        // Everything this mixin touches is a mod class, a JDK class or erased generics, so every
        // annotation sets remap = false and no refmap is generated or needed.
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
