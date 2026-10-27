package com.dungeonsillagers.client;

/** Regions of the effects atlas (textures/particle/vfx.png, built by tools/dungeons2 vfx.js). */
public enum VfxSprite {
    /** M_PixelParticleEmissive / Masked: one solid pixel. */
    SOLID(0, 0, 4, 4, 1, 1),
    /** T_IceSymbol: four 3-pixel ice glints. */
    ICE(8, 0, 3, 3, 2, 4),
    /** T_Minecraft_Particles_Generic_4x4: four 4-pixel sparkles. */
    SPARKLE(16, 0, 4, 4, 2, 4),
    /** T_PixelSpiral. */
    SPIRAL(32, 0, 11, 11, 1, 1),
    /** MI_HollowSquare. */
    SQUARE(48, 0, 8, 8, 1, 1),
    /** Octagon_Hollow (protective bubble). */
    OCTAGON(64, 0, 16, 16, 1, 1),
    /** MI_PixelCircleParticles: thin pixel ring. */
    RING(96, 0, 32, 32, 1, 1),
    /** Thick ring for shockwaves. */
    RING_THICK(128, 0, 32, 32, 1, 1),
    /** T_Runes (MI_Runes): 8 x 8 glyphs; the first 37 are drawn. */
    RUNE(0, 32, 8, 8, 8, 37),
    /** T_ElectricArcs: 4 x 4 arc flipbook. */
    ARC(64, 32, 16, 16, 4, 16),
    /** T_ElectricStraight_Horizontal: four lightning strips. */
    BOLT(128, 32, 128, 16, 1, 4),
    /** T_GodRayLight: four light shafts. */
    GOD_RAY(0, 96, 16, 32, 2, 4),
    /** T_WhirlWind. */
    WIND(32, 96, 32, 32, 1, 1),
    /** MI_LinearGradient: opaque at v0, clear at v1. */
    GRADIENT(64, 96, 8, 32, 1, 1);

    private final int x, y, w, h, columns;
    final int frames;

    VfxSprite(int x, int y, int w, int h, int columns, int frames) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.columns = columns;
        this.frames = frames;
    }

    /** u0, v0, u1, v1 of frame {@code index}, inset a hair so neighbours never bleed in. */
    float[] uv(int index) {
        int fx = this.x + (index % this.columns) * this.w, fy = this.y + (index / this.columns) * this.h;
        float inset = 0.01F;
        return new float[] {(fx + inset) / 256.0F, (fy + inset) / 256.0F, (fx + this.w - inset) / 256.0F, (fy + this.h - inset) / 256.0F};
    }
}
