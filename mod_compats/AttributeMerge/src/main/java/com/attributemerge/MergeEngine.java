package com.attributemerge;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The rewrite itself, shared by every entry point.
 *
 * <p>Two entry points exist because the two events do not behave the same way:
 *
 * <ul>
 *   <li>{@link com.attributemerge.event.ItemAttributeMergeHandler} goes through
 *       {@code ItemAttributeModifierEvent}'s own {@code removeAttribute} / {@code addModifier},
 *       which write to the map the event will hand back.</li>
 *   <li>{@link com.attributemerge.mixin.CataclysmCuriosMixin} calls {@link #mergeInPlace} on the
 *       multimap directly, because Cataclysm's curio item discards anything written through the
 *       event (see that class).</li>
 * </ul>
 */
public final class MergeEngine {

    private MergeEngine() {
    }

    /**
     * Drain every duplicate attribute in {@code modifiers} and re-add its modifiers on the
     * canonical attribute, scaled and with the operation forced where the rule says so.
     *
     * <p>The multimap is mutated in place and must be mutable. Sources are collected before
     * anything is removed: mutating a multimap while iterating its {@code keySet} is undefined.
     *
     * @return true if anything was rewritten, for logging by the caller
     */
    public static boolean mergeInPlace(Multimap<Attribute, AttributeModifier> modifiers) {
        if (MergeRules.isEmpty() || modifiers == null || modifiers.isEmpty()) return false;

        // Stays null, no allocation, for the overwhelming majority of items that carry none.
        List<Attribute> hits = null;
        for (Attribute attribute : modifiers.keySet()) {
            if (MergeRules.get(attribute) != null) {
                if (hits == null) hits = new ArrayList<>(2);
                hits.add(attribute);
            }
        }
        if (hits == null) return false;

        for (Attribute source : hits) {
            MergeRules.Resolved rule = MergeRules.get(source);
            if (rule == null) continue;

            // Guava returns the removed values in a collection independent of the multimap, so
            // putting into the multimap while iterating it is safe.
            Collection<AttributeModifier> drained = modifiers.removeAll(source);
            for (AttributeModifier original : drained) {
                modifiers.put(rule.target(), rewrite(original, rule));
            }
        }
        return true;
    }

    /** Same modifier on the canonical attribute: amount scaled, operation forced if the rule says. */
    public static AttributeModifier rewrite(AttributeModifier original, MergeRules.Resolved rule) {
        return new AttributeModifier(
                deriveId(original.getId(), rule.targetId()),
                original.getName(),
                original.getAmount() * rule.scale(),
                rule.forceOperation() != null ? rule.forceOperation() : original.getOperation()
        );
    }

    /**
     * A new UUID per (original modifier, target attribute), derived deterministically.
     *
     * <p>Determinism is load-bearing: vanilla removes an item's modifiers by recomputing the same
     * multimap on unequip, so the rewritten modifier must come out identical every time. Deriving
     * from the target id as well keeps two different source attributes that map to the same target
     * (Cataclysm and Obscure crit damage both feed {@code crit_damage}) from colliding.
     */
    public static UUID deriveId(UUID originalId, String targetId) {
        return UUID.nameUUIDFromBytes((originalId + "|" + targetId).getBytes(StandardCharsets.UTF_8));
    }
}
