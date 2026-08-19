package com.ironscompanions.attribute;

import com.ironscompanions.IronsCompanions;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One attribute a worn Companions item grants, resolved and built once.
 *
 * <p>{@link AttributeModifier} is immutable, so a single instance can be handed to every stack that
 * grants it, the way vanilla shares its own default modifiers. That matters here: the events these
 * feed are queried constantly, and building a modifier per query would allocate on a very hot path.
 *
 * @param attribute the attribute to add to
 * @param modifier  the modifier that carries the bonus
 */
record AttributeBonus(Attribute attribute, AttributeModifier modifier) {

    /**
     * Resolves an attribute and builds the modifier that carries the bonus.
     *
     * @param attributeId the attribute to grant
     * @param name        modifier name, also what its id is derived from, so it must be unique in this mod
     * @param amount      the bonus
     * @param operation   how it stacks
     * @return the resolved bonus, or null when the attribute is not registered
     */
    static AttributeBonus of(ResourceLocation attributeId, String name, double amount,
                             AttributeModifier.Operation operation) {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
        if (attribute == null) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] Attribute {} is not registered, skipping {}",
                    attributeId, name);
            return null;
        }
        return new AttributeBonus(attribute, new AttributeModifier(modifierId(name), name, amount, operation));
    }

    /**
     * A stable modifier id derived from the modifier's own name, so two bonuses can never collide and
     * no hand-written UUID series has to be kept unique across files. Stability within a run is all
     * that is required: modifiers granted through {@code ItemAttributeModifierEvent} and
     * {@code CurioAttributeModifierEvent} are rebuilt on every query and never written to disk.
     */
    private static UUID modifierId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }
}
