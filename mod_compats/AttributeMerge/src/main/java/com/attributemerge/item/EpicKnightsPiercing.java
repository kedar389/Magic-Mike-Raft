package com.attributemerge.item;

import com.attributemerge.AttributeMerge;
import com.attributemerge.MergeEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gives Epic Knights weapons a real {@code attributeslib:armor_shred} modifier for the armour
 * piercing they carry as a plain field.
 *
 * <p>Unlike everything else in this mod this is not a merge: Epic Knights registers no attribute at
 * all, so there is no source attribute to drain. {@code WeaponType.armorPiercing} is an {@code int}
 * percent baked into the weapon type at registration and editable in
 * {@code config/epicknights/weapons.json5}. Ten melee weapons carry a nonzero value, the rest sit
 * at 0.
 *
 * <p><b>Its own mechanic must be switched off separately.</b> Set
 * {@code disableArmorPiercing: true} in {@code config/epicknights/general.json5} on both sides, or
 * the stat is applied twice: once by this modifier and once by Epic Knights.
 * {@code CommonEvents.onLivingHurt} always returns {@code EventResult.pass()} and discards
 * {@code MedievalWeaponItem.onHurtEntity}'s return value, so the mod's piercing is a SECOND damage
 * instance on top of the normal hit rather than a replacement for it. That flag makes
 * {@code WeaponType.getArmorPiercing()} return 0, which fails the {@code != 0} gate in
 * {@code onHurtEntity} and also drops Epic Knights' own {@code armorpiercing} tooltip line, so this
 * modifier's line replaces it instead of sitting next to it.
 *
 * <p>Consequences of the flag being the off-switch:
 *
 * <ul>
 *   <li>The value is read from the private {@code armorPiercing} FIELD, never through
 *       {@code getArmorPiercing()}, which returns 0 once the flag is set and would leave every
 *       weapon with no modifier.</li>
 *   <li>{@code CombatHelper.getArmorPiercingFactor} returns {@code p / (1 - p)} for an Epic Knights
 *       weapon and {@code 1.0} for anything else, and scales shield durability damage in
 *       {@code MedievalShieldItem.onBlocked}. With the flag set, the ten weapons stop wearing down
 *       Epic Knights shields, matching the 18 that already sit at {@code armorPiercing: 0}.</li>
 * </ul>
 *
 * <p><b>Why {@code armor_shred} and not {@code armor_pierce}:</b> the field is a percentage, and
 * {@code armor_pierce} is flat armour points. Epic Knights deals {@code p * blocked} extra damage
 * and lets armour reduce it again, a net multiplier of {@code (1-r)(1 + p*r)} on the raw hit;
 * {@code ALCombatRules.getDamageAfterArmor} instead does
 * {@code armor -= armor * shred * (1 - min(toughness * 0.02, 0.6))}. Under Apothic's default
 * {@code a / (a + armor)} formula with a = 10 and no toughness the two land within 1 to 2 percent
 * (armour 20 at 15%: 0.367 against 0.370; armour 40 at 20%: 0.232 against 0.238). Shred is a hair
 * stronger at zero toughness and weaker against high-toughness targets, which Epic Knights ignores.
 *
 * <p>Resolved by name string, so this adds no compile dependency on Epic Knights and costs nothing
 * when it is absent.
 */
public final class EpicKnightsPiercing {

    private static final String WEAPON_ITEM = "com.magistuarmory.item.MedievalWeaponItem";
    private static final String TYPE_FIELD = "type";
    private static final String PIERCING_FIELD = "armorPiercing";

    private static final String TARGET_ID = "attributeslib:armor_shred";
    private static final String MODIFIER_NAME = "Armor Piercing";

    /** The field counts percent points, {@code armor_shred} is a fraction. */
    private static final double SCALE = 0.01D;


    /**
     * One weapon item that carries piercing.
     *
     * @param weaponType  the item's {@code WeaponType}, held so the amount can be re-read live.
     *                    Epic Knights' config objects ARE the weapon types, so an in-game config
     *                    save changes this value without a restart. Vanilla removes modifiers by
     *                    UUID, not by amount, so a value that changes under an equipped item still
     *                    unequips cleanly.
     * @param modifierId  deterministic, derived from the item id and the target attribute
     */
    private record Weapon(Object weaponType, UUID modifierId) {
    }

    /**
     * Item -> piercing, identity-keyed. Empty when Epic Knights is absent, which is the cheap
     * early-out. {@code volatile} for the same reason as {@code MergeRules.resolved}: written once
     * during common setup, read from the server thread and the client render thread.
     */
    private static volatile Map<Item, Weapon> weapons = Map.of();

    private static volatile Attribute armorShred;
    private static volatile Field piercingField;

    private EpicKnightsPiercing() {
    }

    /** Called once from common setup, on both physical sides. */
    public static void resolve() {
        Class<?> weaponClass;
        try {
            weaponClass = Class.forName(WEAPON_ITEM);
        } catch (ClassNotFoundException e) {
            AttributeMerge.LOGGER.debug("Epic Knights is not installed, skipping armor piercing");
            return;
        }

        Attribute target = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(TARGET_ID));
        if (target == null) {
            AttributeMerge.LOGGER.warn("{} is not registered, skipping Epic Knights armor piercing", TARGET_ID);
            return;
        }

        Field typeField;
        Field piercing;
        try {
            typeField = weaponClass.getField(TYPE_FIELD);
            piercing = typeField.getType().getDeclaredField(PIERCING_FIELD);
            piercing.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            // Loud: an Epic Knights update moved the field, and silently granting nothing would
            // look like the stat had simply been balanced away.
            AttributeMerge.LOGGER.error("Epic Knights changed shape, skipping armor piercing", e);
            return;
        }

        Map<Item, Weapon> out = new IdentityHashMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            if (!weaponClass.isInstance(item)) continue;

            Object weaponType;
            int amount;
            try {
                weaponType = typeField.get(item);
                if (weaponType == null) continue;
                amount = piercing.getInt(weaponType);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                AttributeMerge.LOGGER.error("Could not read armor piercing from {}", item, e);
                continue;
            }
            if (amount == 0) continue;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) continue;
            out.put(item, new Weapon(weaponType, MergeEngine.deriveId(id.toString(), TARGET_ID)));
        }

        if (out.isEmpty()) {
            AttributeMerge.LOGGER.info("No Epic Knights weapon carries armor piercing");
            return;
        }

        armorShred = target;
        piercingField = piercing;
        weapons = out;

        AttributeMerge.LOGGER.info("Granting {} as {} on {} Epic Knights weapon(s)",
                PIERCING_FIELD, TARGET_ID, out.size());
        for (Item item : out.keySet()) {
            AttributeMerge.LOGGER.debug("  {}", ForgeRegistries.ITEMS.getKey(item));
        }
    }

    /**
     * Add the modifier if this item is one of the ten. Mainhand only: Epic Knights reads the
     * attacker's main hand, and {@code armor_shred} is an attacker-side attribute.
     *
     * <p>Cost for every other item in the pack is one identity-hash lookup that misses.
     */
    public static void apply(ItemAttributeModifierEvent event) {
        Map<Item, Weapon> table = weapons;
        if (table.isEmpty() || event.getSlotType() != EquipmentSlot.MAINHAND) return;

        Weapon weapon = table.get(event.getItemStack().getItem());
        if (weapon == null) return;

        int amount;
        try {
            amount = piercingField.getInt(weapon.weaponType());
        } catch (IllegalAccessException | IllegalArgumentException e) {
            return;
        }
        if (amount == 0) return;

        event.addModifier(armorShred, new AttributeModifier(
                weapon.modifierId(), MODIFIER_NAME, amount * SCALE, AttributeModifier.Operation.ADDITION));
    }
}
