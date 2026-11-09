package com.rendertweaks.oculus;

/** Added to Oculus's {@code CachedUniform}: bumped every time its value really changes. */
public interface UniformVersion {
    int renderTweaks$version();
}
