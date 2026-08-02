package com.attributemerge;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The merge table, plus its resolved {@link Attribute} lookup.
 *
 * <p>Why these two families need a code hook at all, while the seven Iron's Lib duplicates are
 * merged by {@code datapacks/Attribute_Merge/} (Iron's Lib's own datapack remap):
 *
 * <ul>
 *   <li><b>Cataclysm</b> counts in percent POINTS, {@code The_Annihilator} grants
 *       {@code ADDITION 75.0} meaning "+75% crit damage" (The Immolator 60.0). Apothic's
 *       {@code crit_damage} is a 1.5-based multiplier, so moving 75.0 across unchanged would be a
 *       76.5x crit. Iron's Lib's remap cannot rescale, so it cannot be used. → x0.01.</li>
 *   <li><b>Obscure API</b> (used by Aquamirae) reads its attributes with a hand-rolled formula
 *       ({@code ObscureAPIAttributes.getPercent}) that SUMS the amounts of ADDITION +
 *       MULTIPLY_BASE + MULTIPLY_TOTAL with plain addition, and Aquamirae's gear grants them as
 *       {@code MULTIPLY_BASE} (e.g. 0.3 = +30%). Under the vanilla math Apothic uses,
 *       MULTIPLY_BASE on a base-0 attribute (dodge, crit chance) contributes NOTHING, moving it
 *       across unchanged would silently delete the bonus. → force ADDITION.</li>
 * </ul>
 *
 * <p>Obscure's {@code penetration} is a percentage of armor ignored, so its Apothic twin is
 * {@code armor_shred} (percentage armor reduction), not {@code armor_pierce} (flat armor points).
 *
 * <p><b>Additional Entity Attributes</b> ships inside Origins (Origins jar-in-jars Apoli, which
 * jar-in-jars AEA) and is loaded by Sinytra Connector. Three of its 13 attributes duplicate an
 * Apothic stat. All three carry the same unit as their Apothic twin, so they transfer at scale 1.0
 * and keep their original operation: AEA reads its values through vanilla {@code getValue()}, so
 * unlike Obscure API there is no custom formula to work around. Measured base / min / max:
 *
 * <table>
 *   <tr><th>AEA</th><th>Apothic</th><th>Unit</th></tr>
 *   <tr><td>{@code player.critical_bonus_damage} 0.5 / -1 / 1024</td>
 *       <td>{@code crit_damage} 1.5 / 1 / 100</td>
 *       <td>both land on the same crit multiplier, AEA counts the bonus above 1.0 and Apothic
 *           counts the multiplier itself, so ADDITION is identical on both</td></tr>
 *   <tr><td>{@code player.dig_speed} 0.0 / 0 / 2048</td>
 *       <td>{@code mining_speed} 1.0 / 0 / 10</td>
 *       <td>AEA applies {@code speed * (1 + value)}, Apothic multiplies by the value</td></tr>
 *   <tr><td>{@code player.dropped_experience} 1.0 / 0 / 1024</td>
 *       <td>{@code experience_gained} 1.0 / 0 / 1000</td>
 *       <td>both are multipliers based at 1.0</td></tr>
 * </table>
 *
 * <p>Because the base values differ, only ADDITION transfers exactly. A MULTIPLY_BASE or
 * MULTIPLY_TOTAL modifier scales off a different base on each side. Nothing in the pack grants
 * these three today, and Apoli's {@code origins:attribute} power emits ADDITION, so this is a
 * forward-compatibility note rather than a live concern.
 *
 * <p>Not merged, no Apothic counterpart exists: {@code obscure_api} parry / accuracy / resilience /
 * regeneration / magic_damage / magic_resistance, {@code cataclysm} nature_heal / eat_speed /
 * charge_time, and the ten remaining {@code additionalentityattributes} ones. Of those ten, Origins'
 * Merling grants {@code generic.water_speed} and Medieval Origins' Goblin grants
 * {@code player.bonus_rare_loot_rolls} and {@code player.bonus_loot_count_rolls}.
 */
public final class MergeRules {

    private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADDITION;

    public static final List<MergeRule> TABLE = List.of(
            // Cataclysm, percent points -> Apothic's 1.5-based crit multiplier.
            MergeRule.of("cataclysm:additional_critical_damage", "attributeslib:crit_damage", 0.01D, ADD),

            // Obscure API / Aquamirae, fractions, but delivered as MULTIPLY_BASE.
            MergeRule.of("obscure_api:critical_damage", "attributeslib:crit_damage", 1.0D, ADD),
            MergeRule.of("obscure_api:critical_hit", "attributeslib:crit_chance", 1.0D, ADD),
            MergeRule.of("obscure_api:dodge", "attributeslib:dodge_chance", 1.0D, ADD),
            MergeRule.of("obscure_api:penetration", "attributeslib:armor_shred", 1.0D, ADD),
            MergeRule.of("obscure_api:healing_power", "attributeslib:healing_received", 1.0D, ADD),

            // Additional Entity Attributes (via Origins -> Apoli). Same units, vanilla math, so
            // the operation is left alone.
            MergeRule.of("additionalentityattributes:player.critical_bonus_damage",
                    "attributeslib:crit_damage", 1.0D, null),
            MergeRule.of("additionalentityattributes:player.dig_speed",
                    "attributeslib:mining_speed", 1.0D, null),
            MergeRule.of("additionalentityattributes:player.dropped_experience",
                    "attributeslib:experience_gained", 1.0D, null)
    );

    /**
     * Source attribute -> resolved rule. Identity-keyed: attributes are singletons and do not
     * override {@code hashCode}, so this is an identity hash lookup in the hot path.
     *
     * <p>Empty until {@link #resolve()} runs, and empty afterwards if none of the duplicate mods
     * are installed, which is the cheap early-out for the event handler.
     *
     * <p>{@code volatile} because it is written once on the mod-loading thread during
     * {@code FMLCommonSetupEvent} and then read from the server thread and the client render thread
     * (tooltips recompute an item's modifiers per frame). FML's loading pipeline almost certainly
     * publishes it safely already, but nothing in the JMM guarantees that, and a volatile read of a
     * field that never changes again costs nothing measurable. The map itself is never mutated after
     * publication, so the reads need no further synchronisation.
     */
    private static volatile Map<Attribute, Resolved> resolved = Map.of();

    private MergeRules() {
    }

    /** A rule with its attributes already looked up. */
    public record Resolved(Attribute target, String targetId, double scale,
                           AttributeModifier.Operation forceOperation) {
    }

    /** Called once from common setup, on both physical sides. */
    public static void resolve() {
        Map<Attribute, Resolved> out = new IdentityHashMap<>();
        for (MergeRule rule : TABLE) {
            Attribute from = ForgeRegistries.ATTRIBUTES.getValue(rule.from());
            Attribute to = ForgeRegistries.ATTRIBUTES.getValue(rule.to());
            if (from == null || to == null) {
                // Normal when a mod isn't installed; only worth a debug line.
                AttributeMerge.LOGGER.debug("Skipping {} -> {} ({} is not registered)",
                        rule.from(), rule.to(), from == null ? rule.from() : rule.to());
                continue;
            }
            if (from == to) {
                AttributeMerge.LOGGER.warn("Skipping self-merge for {}", rule.from());
                continue;
            }
            out.put(from, new Resolved(to, rule.to().toString(), rule.scale(), rule.forceOperation()));
        }
        resolved = out.isEmpty() ? Map.of() : out;
        AttributeMerge.LOGGER.info("Merging {} duplicate attribute(s) into Apothic Attributes", out.size());
        for (Map.Entry<Attribute, Resolved> e : out.entrySet()) {
            AttributeMerge.LOGGER.info("  {} -> {} (x{})",
                    ForgeRegistries.ATTRIBUTES.getKey(e.getKey()), e.getValue().targetId(), e.getValue().scale());
        }
    }

    public static boolean isEmpty() {
        return resolved.isEmpty();
    }

    public static Resolved get(Attribute source) {
        return resolved.get(source);
    }
}
