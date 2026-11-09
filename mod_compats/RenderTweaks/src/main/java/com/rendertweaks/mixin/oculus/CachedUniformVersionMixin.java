package com.rendertweaks.mixin.oculus;

import com.rendertweaks.oculus.UniformVersion;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Oculus 1.8.0 {@code CachedUniform.update()} calls {@code doUpdate()} (which recomputes the value
 * and returns whether it differs from the last one) and then ignores that answer. This keeps the
 * answer as a version number. {@code doUpdate} is still called exactly once, as before.
 */
@Mixin(value = CachedUniform.class, remap = false)
public abstract class CachedUniformVersionMixin implements UniformVersion {

    @Unique
    private int renderTweaks$version;

    @Shadow
    protected abstract boolean doUpdate();

    @Override
    public int renderTweaks$version() {
        return renderTweaks$version;
    }

    @Redirect(
            method = "update()V",
            at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/uniforms/custom/cached/CachedUniform;doUpdate()Z"))
    private boolean renderTweaks$countChanges(CachedUniform self) {
        boolean changed = doUpdate();
        if (changed) {
            renderTweaks$version++;
        }
        return changed;
    }
}
