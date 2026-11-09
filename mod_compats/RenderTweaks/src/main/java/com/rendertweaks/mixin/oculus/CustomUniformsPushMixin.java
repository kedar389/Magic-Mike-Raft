package com.rendertweaks.mixin.oculus;

import com.rendertweaks.Tweak;
import com.rendertweaks.oculus.ProgramUniforms;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Oculus 1.8.0 {@code CustomUniforms.push(program)}: called right after a program is bound, uploads
 * the program's custom uniforms. Replaced by {@link ProgramUniforms}, which only uploads values the
 * program does not already hold. One {@code CustomUniforms} exists per shader pipeline, so the
 * per-program state is dropped with it on a shader reload.
 */
@Mixin(value = CustomUniforms.class, remap = false)
public abstract class CustomUniformsPushMixin {

    @Shadow
    @Final
    private Map<Object, Object2IntMap<CachedUniform>> locationMap;

    @Unique
    private final Map<Object, ProgramUniforms> renderTweaks$programs = new IdentityHashMap<>();

    @Inject(method = "push(Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true)
    private void renderTweaks$pushChangedOnly(Object program, CallbackInfo ci) {
        if (!Tweak.UNIFORMS.isEnabled()) {
            return;
        }
        Object2IntMap<CachedUniform> uniforms = locationMap.get(program);
        if (uniforms == null) {
            return;
        }
        ProgramUniforms state = renderTweaks$programs.get(program);
        if (state == null || !state.matches(uniforms)) {
            state = new ProgramUniforms(uniforms);
            renderTweaks$programs.put(program, state);
        }
        state.push();
        ci.cancel();
    }
}
