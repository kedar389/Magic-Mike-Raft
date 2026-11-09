package com.rendertweaks.pehkui;

/** Added to {@code Entity}: whether all of its Pehkui scales are at their defaults. */
public interface PehkuiStateHolder {
    int UNKNOWN = 0;
    int ALL_DEFAULT = 1;
    int CHANGED = 2;

    int renderTweaks$pehkuiState();

    void renderTweaks$setPehkuiState(int state);
}
