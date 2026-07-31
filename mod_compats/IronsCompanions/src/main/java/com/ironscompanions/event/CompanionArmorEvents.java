package com.ironscompanions.event;

import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Phase 4, Companions armour ↔ Iron's Spells bridges:
 * <ul>
 *   <li><b>Mage set</b> also reduces incoming IS spell damage (10% per worn piece, tunable).</li>
 *   <li><b>Holy Robe set</b> grants +10% holy spell power per worn piece.</li>
 * </ul>
 *
 * <p>VERIFY(build): item ids are confirmed from the Companions lang file; the holy spell-power
 * attribute id {@code irons_spellbooks:holy_spell_power} is confirmed from the IS lang file.
 */
public class CompanionArmorEvents {

    private static final Set<ResourceLocation> MAGE_SET = Set.of(
            new ResourceLocation("companions", "mage_hat"),
            new ResourceLocation("companions", "mage_coat"),
            new ResourceLocation("companions", "mage_leggings"));

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

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        Attribute holyPower = ForgeRegistries.ATTRIBUTES.getValue(HOLY_SPELL_POWER);
        if (holyPower == null) {
            return;
        }
        AttributeInstance inst = entity.getAttribute(holyPower);
        if (inst == null) {
            return;
        }
        switch (event.getSlot()) {
            case HEAD -> applyPiece(inst, HOLY_HEAD, "irons_companions.holy_robe_head", itemId(event.getTo()), HOLY_ROBE_MASK);
            case CHEST -> applyPiece(inst, HOLY_CHEST, "irons_companions.holy_robe_chest", itemId(event.getTo()), HOLY_ROBE_COAT);
            case LEGS -> applyPiece(inst, HOLY_LEGS, "irons_companions.holy_robe_legs", itemId(event.getTo()), HOLY_ROBE_LEGGINGS);
            default -> {
                /* other slots don't carry holy robe pieces */
            }
        }
    }

    private static void applyPiece(AttributeInstance inst, UUID id, String name, ResourceLocation worn, ResourceLocation expected) {
        inst.removeModifier(id); // idempotent
        if (expected.equals(worn)) {
            inst.addTransientModifier(new AttributeModifier(id, name, HOLY_BONUS_PER_PIECE, AttributeModifier.Operation.ADDITION));
        }
    }

    // ---- Mage set: reduce incoming IS spell damage ------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!isIronsMagic(event)) {
            return;
        }
        int pieces = 0;
        for (ItemStack armor : event.getEntity().getArmorSlots()) {
            if (MAGE_SET.contains(itemId(armor))) {
                pieces++;
            }
        }
        if (pieces > 0) {
            float factor = Math.max(0f, 1f - MAGE_REDUCTION_PER_PIECE * pieces);
            event.setAmount(event.getAmount() * factor);
        }
    }

    private static boolean isIronsMagic(LivingHurtEvent event) {
        return event.getSource()
                .typeHolder()
                .unwrapKey()
                .map(k -> k.location().getNamespace().equals("irons_spellbooks"))
                .orElse(false);
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
    }
}
