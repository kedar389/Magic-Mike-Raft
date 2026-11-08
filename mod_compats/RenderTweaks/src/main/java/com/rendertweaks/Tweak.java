package com.rendertweaks;

/**
 * The fixes that can be switched off in game with {@code /rendertweaks}, for A/B testing in one
 * session. All start on and reset on restart. Read and written on the render thread only.
 */
public enum Tweak {
    BOUNDS("bounds", "Block-entity render bounds cache"),
    LIGHT("light", "Block-entity light, re-read every 8 frames"),
    ENTITY_LIGHT("entitylight", "Entity light kept while in the same block, re-read every 8 frames"),
    CATACLYSM("cataclysm", "Batched Lionfish (Cataclysm) models"),
    CHLORIDE("chloride", "Chloride entity limit: nearest pick on a flat array"),
    KNIGHTLIB("knightlib", "KnightLib hitbox picker spatial query"),
    APOLI("apoli", "Apoli layer and render checks once per frame"),
    BAGUS("bagus", "Bagus Lib pose reset from a cached part list"),
    PEHKUI("pehkui", "Pehkui scales cached per frame on the client"),
    RAIN("rain", "Subtle Effects rain tint per column, kept 20 frames");

    private final String id;
    private final String description;
    private boolean enabled = true;

    Tweak(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
