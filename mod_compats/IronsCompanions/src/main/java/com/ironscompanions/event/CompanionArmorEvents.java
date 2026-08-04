package com.ironscompanions.event;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Companions armour → Iron's Spells bridge: each <b>Holy Robe</b> piece grants holy spell power.
 *
 * <p>The Mage set is left exactly as Companions ships it.
 */
public class CompanionArmorEvents {

    private static final ResourceLocation HOLY_ROBE_MASK = new ResourceLocation("companions", "holy_robe_mask");
    private static final ResourceLocation HOLY_ROBE_COAT = new ResourceLocation("companions", "holy_robe_coat");
    private static final ResourceLocation HOLY_ROBE_LEGGINGS = new ResourceLocation("companions", "holy_robe_leggings");

    private static final ResourceLocation HOLY_SPELL_POWER = new ResourceLocation("irons_spellbooks", "holy_spell_power");

    private static final float MAGE_REDUCTION_PER_PIECE = 0.10f; // 10% per piece
    private static final double HOLY_BONUS_PER_PIECE = 0.10d; // +10% holy spell power per piece

    // Stable per-slot modifier UUIDs so add/remove is idempotent.
    private static final UUID HOLY_HEAD = UUID.fromString("b1a2c3d4-0001-4a00-8000-000000000001");
    private static final UUID HOLY_CHEST = UUID.fromString("b1a2c3d4-0002-4a00-8000-000000000002");
    private static final UUID HOLY_LEGS = UUID.fromString("b1a2c3d4-0003-4a00-8000-000000000003");

    // ---- Holy Robe: +holy spell power -------------------------------------------------------

    /**
     * Attaches the bonus to the <b>item</b>, not the wearer: this event is the hook behind
     * {@code ItemStack#getAttributeModifiers}, so vanilla applies the attribute while the piece is worn,
     * renders it in the tooltip, and removes it on unequip with no bookkeeping here.
     */
    @SubscribeEvent
    public void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        Attribute holyPower = ForgeRegistries.ATTRIBUTES.getValue(HOLY_SPELL_POWER);
        if (holyPower == null) {
            return;
        }
        ResourceLocation worn = itemId(event.getItemStack());
        if (worn == null) {
            return;
        }
        switch (event.getSlotType()) {
            case HEAD -> addPiece(event, holyPower, HOLY_HEAD, "irons_companions.holy_robe_head", worn, HOLY_ROBE_MASK);
            case CHEST -> addPiece(event, holyPower, HOLY_CHEST, "irons_companions.holy_robe_chest", worn, HOLY_ROBE_COAT);
            case LEGS -> addPiece(event, holyPower, HOLY_LEGS, "irons_companions.holy_robe_legs", worn, HOLY_ROBE_LEGGINGS);
            default -> {
                /* other slots don't carry holy robe pieces */
            }
        }
    }

    private static void addPiece(ItemAttributeModifierEvent event, Attribute attribute, UUID id, String name,
                                 ResourceLocation worn, ResourceLocation expected) {
        if (expected.equals(worn)) {
            event.addModifier(attribute,
                    new AttributeModifier(id, name, HOLY_BONUS_PER_PIECE, AttributeModifier.Operation.ADDITION));
        }
    }


    private static ResourceLocation itemId(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
    }
}
