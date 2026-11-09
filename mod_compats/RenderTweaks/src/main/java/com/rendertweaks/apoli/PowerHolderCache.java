package com.rendertweaks.apoli;

/**
 * Added to {@code Entity}: its Apoli power holder component, looked up once. The component system
 * creates one per entity when the entity is constructed and never replaces it (a respawned player
 * is a new entity), so the reference stays valid for the entity's whole life.
 */
public interface PowerHolderCache {
    Object renderTweaks$powerHolder();

    void renderTweaks$setPowerHolder(Object holder);
}
