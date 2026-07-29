package com.attributemerge.event;

import com.attributemerge.AttributeMerge;
import com.attributemerge.MergeRules;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Rewrites duplicate-attribute modifiers onto their canonical Apothic attribute at the moment the
 * item declares them.
 *
 * <p>This is the whole mod. There is no ticking, no polling and no per-player state: the bonus is
 * simply never applied to the duplicate attribute in the first place, so
 *
 * <ul>
 *   <li>it stacks on the canonical attribute with every other source of that stat,</li>
 *   <li>the duplicate's owning mod sees its base value and contributes nothing (no double-dipping,
 *       and no cancelling modifier needed),</li>
 *   <li>the item's own tooltip lists the canonical stat, so tooltip and character sheet agree,</li>
 *   <li>mobs wearing/holding the gear get the same treatment for free.</li>
 * </ul>
 *
 * <p>Cost: the handler runs on {@link ItemAttributeModifierEvent}, which fires when an item's
 * modifiers are computed (equip, equipment tick, tooltip). For an item with no duplicate attributes
 * — every item in the pack except a handful — it does one {@code isEmpty()} check plus at most a
 * few identity-hash lookups, and allocates nothing.
 *
 * <p>Not covered (unchanged from any other approach): attributes granted by potions, spells or
 * per-tick code rather than item modifiers, and curio slots (nothing in the pack grants these
 * attributes from a curio — Unique Accessories already grants the Apothic ones directly). If that
 * changes, add a {@code CurioAttributeModifierEvent} listener alongside this one.
 */
@Mod.EventBusSubscriber(modid = AttributeMerge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ItemAttributeMergeHandler {

    private ItemAttributeMergeHandler() {
    }

    /**
     * LOW priority so other mods have already contributed their modifiers (Iron's Lib runs its own
     * remap at HIGHEST); we want to see the final set and be the last to rewrite it.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (MergeRules.isEmpty()) return;

        Multimap<Attribute, AttributeModifier> modifiers = event.getModifiers();
        if (modifiers.isEmpty()) return;

        // Collect first: the multimap view must not be mutated while iterating it.
        // Stays null — no allocation — for items that carry none of the duplicate attributes.
        List<Attribute> hits = null;
        for (Attribute attribute : modifiers.keySet()) {
            if (MergeRules.get(attribute) != null) {
                if (hits == null) hits = new ArrayList<>(2);
                hits.add(attribute);
            }
        }
        if (hits == null) return;

        for (Attribute source : hits) {
            MergeRules.Resolved rule = MergeRules.get(source);
            if (rule == null) continue;

            Collection<AttributeModifier> drained = event.removeAttribute(source);
            if (drained == null) continue;

            for (AttributeModifier original : drained) {
                event.addModifier(rule.target(), rewrite(original, rule));
            }
        }
    }

    private static AttributeModifier rewrite(AttributeModifier original, MergeRules.Resolved rule) {
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
    private static UUID deriveId(UUID originalId, String targetId) {
        return UUID.nameUUIDFromBytes((originalId + "|" + targetId).getBytes(StandardCharsets.UTF_8));
    }
}
