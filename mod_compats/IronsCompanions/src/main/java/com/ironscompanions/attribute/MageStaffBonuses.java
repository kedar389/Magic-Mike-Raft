package com.ironscompanions.attribute;

import com.ironscompanions.ItemIds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * What the <b>Mage Staff</b> carries in hand: 4 attack damage at 1 attack speed, <b>+10% spell
 * power</b>, <b>+15% summon damage</b> and <b>+50 max mana</b>.
 *
 * <p>Companions registers the staff as a plain item, so the numbers are added here rather than coming
 * from a {@code StaffTier}. {@code mixin/MageStaffCastingMixin} supplies the casting half.
 *
 * <p>The kit places it against the six Iron's Spells staves, all of which specialise in one school:
 *
 * <ul>
 *   <li>Spell power is school-agnostic, so it raises every companion ability that
 *       {@code damage.CompanionsDamageBridge} re-deals as spell damage, whatever school it carries.</li>
 *   <li>Summon damage is the headline rather than a perk, at 15% against the Blood Staff's 10%.</li>
 *   <li>Max mana is on no other staff. It is flat, because the attribute's base is 100 and a
 *       percentage of that is worth nothing next to the 200 a spellbook adds.</li>
 * </ul>
 *
 * <p>Damage and speed use Iron's Spells' own staff shape, {@code AttributeModifier}s under the two
 * vanilla base UUIDs so the tooltip reads them as the weapon's own values. 3.0 and -3.0 match the
 * Artificer Cane; the staff costs two sticks and a soul gem, one diamond smelted over three soul
 * charges, which puts it between the Graybeard Staff and the Ice Staff.
 *
 * <p>Hot path: {@code ItemAttributeModifierEvent} backs {@code ItemStack#getAttributeModifiers}, which
 * is queried per stack on every equipment change, attack and tooltip. The slot check runs first and the
 * modifiers are pre-built (see {@link AttributeBonus}).
 */
public final class MageStaffBonuses {

    /** Added to the player's base 1, so the tooltip reads 4 attack damage. */
    private static final double ATTACK_DAMAGE = 3.0d;

    /** Added to the player's base 4, so the tooltip reads 1 attack speed. */
    private static final double ATTACK_SPEED = -3.0d;

    /** {@code MULTIPLY_BASE} on a {@code MagicPercentAttribute} of base 1.0: sums, and renders "+10%". */
    private static final double SPELL_POWER = 0.10d;

    private static final double SUMMON_DAMAGE = 0.15d;

    /** A quarter of the 200 every Iron's Spells spellbook grants. */
    private static final double MAX_MANA = 50.0d;

    /** The name vanilla and Iron's Spells both give a weapon's damage and speed modifiers. */
    private static final String WEAPON_MODIFIER = "Weapon modifier";

    /**
     * {@code Item.BASE_ATTACK_DAMAGE_UUID} and {@code Item.BASE_ATTACK_SPEED_UUID}, spelled out because
     * both are {@code protected} and this class is not an {@link Item}. Under any other id the tooltip
     * lists the two as green bonuses instead of the weapon's own damage and speed.
     */
    private static final UUID BASE_ATTACK_DAMAGE_UUID =
            UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID BASE_ATTACK_SPEED_UUID =
            UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private static final Lazy<Item> STAFF = ItemIds.lazy(ItemIds.MAGE_STAFF);
    private static final Lazy<List<AttributeBonus>> BONUSES = Lazy.concurrentOf(MageStaffBonuses::resolve);

    private MageStaffBonuses() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        // The slot check keeps the staff from arming the player out of a hotbar slot or the offhand.
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        Item staff = STAFF.get();
        if (staff == null || event.getItemStack().getItem() != staff) {
            return;
        }
        for (AttributeBonus bonus : BONUSES.get()) {
            event.addModifier(bonus.attribute(), bonus.modifier());
        }
    }

    private static List<AttributeBonus> resolve() {
        List<AttributeBonus> bonuses = new ArrayList<>(5);
        bonuses.add(new AttributeBonus(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, WEAPON_MODIFIER, ATTACK_DAMAGE,
                        AttributeModifier.Operation.ADDITION)));
        bonuses.add(new AttributeBonus(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, WEAPON_MODIFIER, ATTACK_SPEED,
                        AttributeModifier.Operation.ADDITION)));
        add(bonuses, AttributeBonus.of(IronsSpellsAttributes.SPELL_POWER,
                "irons_companions.mage_staff_power", SPELL_POWER,
                AttributeModifier.Operation.MULTIPLY_BASE));
        add(bonuses, AttributeBonus.of(IronsSpellsAttributes.SUMMON_DAMAGE,
                "irons_companions.mage_staff_summons", SUMMON_DAMAGE,
                AttributeModifier.Operation.MULTIPLY_BASE));
        add(bonuses, AttributeBonus.of(IronsSpellsAttributes.MAX_MANA,
                "irons_companions.mage_staff_mana", MAX_MANA,
                AttributeModifier.Operation.ADDITION));
        return List.copyOf(bonuses);
    }

    private static void add(List<AttributeBonus> bonuses, AttributeBonus bonus) {
        if (bonus != null) {
            bonuses.add(bonus);
        }
    }
}
