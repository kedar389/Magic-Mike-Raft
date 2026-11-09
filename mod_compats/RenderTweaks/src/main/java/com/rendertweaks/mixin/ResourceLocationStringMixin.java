package com.rendertweaks.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Vanilla {@code ResourceLocation.toString()} builds {@code namespace + ":" + path} into a new
 * string on every call, and a new string also has to compute its hash from scratch when used as a
 * map or set key. Gnetum does exactly that for every HUD element id every frame (about 0.8% of the
 * frame), and other mods do the same with texture and model ids.
 *
 * <p>A ResourceLocation never changes after construction (both fields are final), so the string is
 * built once and kept; the cached string also keeps its computed hash. Same text as before. A race
 * between threads only builds the string twice, both identical. (f_135804_ is the namespace,
 * f_135805_ the path.)
 */
@Mixin(value = ResourceLocation.class, remap = false)
public abstract class ResourceLocationStringMixin {

    @Shadow
    @Final
    private String f_135804_;

    @Shadow
    @Final
    private String f_135805_;

    @Unique
    private String renderTweaks$string;

    /**
     * @author render_tweaks
     * @reason Same string as vanilla, built once instead of on every call.
     */
    @Overwrite
    public String toString() {
        String string = renderTweaks$string;
        if (string == null) {
            string = f_135804_ + ":" + f_135805_;
            renderTweaks$string = string;
        }
        return string;
    }
}
