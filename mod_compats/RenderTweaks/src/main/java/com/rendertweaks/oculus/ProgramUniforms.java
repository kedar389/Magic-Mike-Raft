package com.rendertweaks.oculus;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;

/**
 * One shader program's custom uniforms, with the value version last uploaded to it.
 *
 * <p>Oculus 1.8.0 {@code CachedUniform.update()} discards whether the value changed and marks every
 * custom uniform "changed" each frame, so {@code CustomUniforms.push} re-uploads every one of them
 * on every program bind: a {@code glUniform} driver call per uniform, per bind, hundreds of binds
 * per frame (entity batches, Create/Colorwheel draws, chunk layers, composite passes). That was 6.4%
 * of the frame with Complementary on AMD.
 *
 * <p>An OpenGL program keeps its uniform values between binds, so a value only has to reach each
 * program once after it changes. This uploads a uniform only when its version differs from the one
 * last uploaded to this program. The program ends up holding exactly the values Oculus would have
 * uploaded.
 */
public final class ProgramUniforms {

    private final int size;
    private final CachedUniform[] uniforms;
    private final int[] locations;
    private final int[] uploaded;

    public ProgramUniforms(Object2IntMap<CachedUniform> locationMap) {
        size = locationMap.size();
        uniforms = new CachedUniform[size];
        locations = new int[size];
        uploaded = new int[size];
        int i = 0;
        for (Object2IntMap.Entry<CachedUniform> entry : locationMap.object2IntEntrySet()) {
            uniforms[i] = entry.getKey();
            locations[i] = entry.getIntValue();
            // Never a real version: forces the first upload to this program.
            uploaded[i] = Integer.MIN_VALUE;
            i++;
        }
    }

    /** True if this was built from a map of the same size, i.e. no uniform was added since. */
    public boolean matches(Object2IntMap<CachedUniform> locationMap) {
        return locationMap.size() == size;
    }

    /** Uploads every uniform whose value changed since it was last uploaded to this program. */
    public void push() {
        for (int i = 0; i < size; i++) {
            CachedUniform uniform = uniforms[i];
            int version = ((UniformVersion) uniform).renderTweaks$version();
            if (uploaded[i] != version) {
                uniform.push(locations[i]);
                uploaded[i] = version;
            }
        }
    }
}
