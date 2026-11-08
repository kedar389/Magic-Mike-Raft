package com.rendertweaks;

/**
 * The fixes that can be switched off in game with {@code /rendertweaks}, for A/B testing in one
 * session. All start on and reset on restart. Read and written on the render thread only.
 */
public enum Tweak {
    BOUNDS("bounds", "Block-entity render bounds cache"),
    CATACLYSM("cataclysm", "Batched Lionfish (Cataclysm) models"),
    CHLORIDE("chloride", "Chloride entity limit: nearest pick on a flat array"),
    KNIGHTLIB("knightlib", "KnightLib hitbox picker spatial query"),
    APOLI("apoli", "Apoli layer check once per mob per frame");

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
