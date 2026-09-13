package com.ironscompanions.attribute;

import com.ironscompanions.ItemIds;
import com.ironscompanions.ModIds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Each <b>Holy Robe</b> piece grants holy spell power, each <b>Crystallized Blood</b> piece grants blood
 * magic resistance. The Mage set and the Crystallized Blood set's own low-health damage reduction are
 * left as Companions ships them.
 *
 * <p>The bonus goes on the item, not the wearer: this event backs {@code ItemStack#getAttributeModifiers},
 * so vanilla applies the attribute while the piece is worn and drops it on unequip.
 *
 * <p>Hot path: {@code getAttributeModifiers} is queried for every stack on every equipment change, attack
 * and tooltip, so the lookup is one identity-hashed map probe and the modifiers are pre-built (see
 * {@link AttributeBonus}).
 */
public final class CompanionArmorBonuses {

    /** Three Holy Robe pieces at +10% holy spell power, so +30% on the full set. */
    private static final double HOLY_BONUS_PER_PIECE = 0.10d;

    /**
     * Four Crystallized Blood pieces at +5% blood magic resistance, +20% on the full set. Iron's Spells
     * resistance is a straight multiplier ({@code 2 - softCapFormula(value)}, the identity up to 1.5), so
     * the full set puts blood magic at 80% damage.
     */
    private static final double BLOOD_BONUS_PER_PIECE = 0.05d;

    /**
     * Three Mage pieces at +5% resistance to every school, so 15% off all magic on the full set. The set's
     * own tooltip is "wards that absorb magical damage", which Companions applies to its own damage only;
     * {@code spell_resist} is the same promise against Iron's Spells.
     */
    private static final double MAGE_BONUS_PER_PIECE = 0.05d;

    /**
     * {@code MULTIPLY_BASE} so the tooltip reads "+10%" rather than "+0.1". The effect matches
     * {@code ADDITION} here: the attributes are Iron's Spells {@code MagicPercentAttribute}s with a base
     * of 1.0, and multiply-base modifiers sum rather than compound.
     */
    private static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.MULTIPLY_BASE;

    /** The declared table, by Companions item path. Resolved by {@link #BONUSES}. */
    private static final List<Piece> PIECES = List.of(
            new Piece("holy_robe_mask", EquipmentSlot.HEAD,
                    IronsSpellsAttributes.HOLY_SPELL_POWER, HOLY_BONUS_PER_PIECE),
            new Piece("holy_robe_coat", EquipmentSlot.CHEST,
                    IronsSpellsAttributes.HOLY_SPELL_POWER, HOLY_BONUS_PER_PIECE),
            new Piece("holy_robe_leggings", EquipmentSlot.LEGS,
                    IronsSpellsAttributes.HOLY_SPELL_POWER, HOLY_BONUS_PER_PIECE),
            new Piece("crystallized_blood_helmet", EquipmentSlot.HEAD,
                    IronsSpellsAttributes.BLOOD_MAGIC_RESIST, BLOOD_BONUS_PER_PIECE),
            new Piece("crystallized_blood_chestplate", EquipmentSlot.CHEST,
                    IronsSpellsAttributes.BLOOD_MAGIC_RESIST, BLOOD_BONUS_PER_PIECE),
            new Piece("crystallized_blood_leggings", EquipmentSlot.LEGS,
                    IronsSpellsAttributes.BLOOD_MAGIC_RESIST, BLOOD_BONUS_PER_PIECE),
            new Piece("crystallized_blood_boots", EquipmentSlot.FEET,
                    IronsSpellsAttributes.BLOOD_MAGIC_RESIST, BLOOD_BONUS_PER_PIECE),
            new Piece("mage_hat", EquipmentSlot.HEAD,
                    IronsSpellsAttributes.SPELL_RESIST, MAGE_BONUS_PER_PIECE),
            new Piece("mage_coat", EquipmentSlot.CHEST,
                    IronsSpellsAttributes.SPELL_RESIST, MAGE_BONUS_PER_PIECE),
            new Piece("mage_leggings", EquipmentSlot.LEGS,
                    IronsSpellsAttributes.SPELL_RESIST, MAGE_BONUS_PER_PIECE));

    private static final Lazy<Map<Item, WornPiece>> BONUSES = Lazy.concurrentOf(CompanionArmorBonuses::resolve);

    private CompanionArmorBonuses() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        WornPiece piece = BONUSES.get().get(event.getItemStack().getItem());
        // The slot check keeps the bonus off a piece held in hand or worn in the wrong slot.
        if (piece == null || piece.slot() != event.getSlotType()) {
            return;
        }
        event.addModifier(piece.bonus().attribute(), piece.bonus().modifier());
    }

    private static Map<Item, WornPiece> resolve() {
        Map<Item, WornPiece> bonuses = new HashMap<>(PIECES.size());
        for (Piece piece : PIECES) {
            Item item = ItemIds.resolve(ModIds.companions(piece.item()));
            if (item == null) {
                continue; // Companions renamed or dropped the piece: it simply grants nothing
            }
            AttributeBonus bonus = AttributeBonus.of(piece.attribute(),
                    "irons_companions." + piece.item(), piece.amount(), OPERATION);
            if (bonus != null) {
                bonuses.put(item, new WornPiece(piece.slot(), bonus));
            }
        }
        return Map.copyOf(bonuses);
    }

    /**
     * @param item      Companions item path
     * @param slot      the slot the piece has to be worn in for the bonus to apply
     * @param attribute the Iron's Spells attribute it grants
     * @param amount    how much of it
     */
    private record Piece(String item, EquipmentSlot slot, ResourceLocation attribute, double amount) {
    }

    /** {@link Piece} once its item, attribute and modifier are resolved. */
    private record WornPiece(EquipmentSlot slot, AttributeBonus bonus) {
    }
}
