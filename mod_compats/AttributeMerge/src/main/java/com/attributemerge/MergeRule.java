package com.attributemerge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

/**
 * One "this attribute is a duplicate of that attribute" rule.
 *
 * @param from            the duplicate attribute to drain (e.g. {@code cataclysm:additional_critical_damage})
 * @param to              the canonical attribute to feed (always an {@code attributeslib:*} one here)
 * @param scale           multiplied into the modifier amount; 1.0 when both sides use the same unit,
 *                        0.01 when the source counts percent POINTS and the target uses fractions
 * @param forceOperation  operation to use on the rewritten modifier, or {@code null} to keep the
 *                        original. Needed because some mods read their attributes with a custom
 *                        formula where MULTIPLY_BASE behaves additively, see {@link MergeRules}.
 */
public record MergeRule(
        ResourceLocation from,
        ResourceLocation to,
        double scale,
        @Nullable AttributeModifier.Operation forceOperation
) {
    public static MergeRule of(String from, String to, double scale, @Nullable AttributeModifier.Operation op) {
        return new MergeRule(new ResourceLocation(from), new ResourceLocation(to), scale, op);
    }
}
