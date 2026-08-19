package com.ironscompanions.attribute;

import com.ironscompanions.ItemIds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * The worn bonus on the Naginata spellbook: <b>+200 max mana</b> and <b>+10% holy spell power</b>.
 *
 * <p>Those are the exact numbers every Iron's Spells themed and unique spellbook carries
 * ({@code SpellBook.withSpellbookAttributes}: {@code MAX_MANA} 200 ADDITION plus the book's own school
 * power at 0.1 MULTIPLY_BASE), so the crafted book is worth equipping for the same reason theirs are,
 * rather than being a bare spell carrier.
 *
 * <p>Iron's Spells attaches those through {@code CurioBaseItem}, which the Companions item is not, so
 * they are added on {@code CurioAttributeModifierEvent} instead. Curios posts it for every stack in a
 * curio slot whether or not the item has an {@code ICurio} capability, and
 * {@code CuriosImplMixinHooks.getAttributeModifiers} returns {@code event.getModifiers()}, so the
 * additions stick. Vanilla applies and removes them with the equip, as with
 * {@link CompanionArmorBonuses}.
 *
 * <p>The slot check keeps the bonus to the spellbook slot, so a cosmetic copy grants nothing.
 *
 * <p>This class may only be loaded when Curios is present: its handler signature names a Curios type.
 * {@code IronsCompanions} gates the registration on that.
 */
public final class NaginataBookBonuses {

    /** Curios slot identifier that Iron's Spells' spellbook slot registers under. */
    private static final String SPELLBOOK_SLOT = "spellbook";

    private static final double MANA_BONUS = 200.0d;

    /**
     * {@code MULTIPLY_BASE} on an Iron's Spells {@code MagicPercentAttribute} whose base is exactly 1.0:
     * it sums like ADDITION and renders as "+10%". Same convention as the Holy Robe set.
     */
    private static final double HOLY_POWER_BONUS = 0.10d;

    private static final Lazy<Item> BOOK = ItemIds.lazy(ItemIds.NAGINATA_BOOK);
    private static final Lazy<List<AttributeBonus>> BONUSES = Lazy.concurrentOf(NaginataBookBonuses::resolve);

    private NaginataBookBonuses() {
    }

    @SubscribeEvent
    public static void onCurioAttributeModifiers(CurioAttributeModifierEvent event) {
        Item book = BOOK.get();
        if (book == null || event.getItemStack().getItem() != book) {
            return;
        }
        if (!SPELLBOOK_SLOT.equals(event.getSlotContext().identifier())) {
            return;
        }
        for (AttributeBonus bonus : BONUSES.get()) {
            event.addModifier(bonus.attribute(), bonus.modifier());
        }
    }

    private static List<AttributeBonus> resolve() {
        List<AttributeBonus> bonuses = new ArrayList<>(2);
        add(bonuses, AttributeBonus.of(IronsSpellsAttributes.MAX_MANA,
                "irons_companions.naginata_book_mana", MANA_BONUS, AttributeModifier.Operation.ADDITION));
        add(bonuses, AttributeBonus.of(IronsSpellsAttributes.HOLY_SPELL_POWER,
                "irons_companions.naginata_book_holy", HOLY_POWER_BONUS,
                AttributeModifier.Operation.MULTIPLY_BASE));
        return List.copyOf(bonuses);
    }

    private static void add(List<AttributeBonus> bonuses, AttributeBonus bonus) {
        if (bonus != null) {
            bonuses.add(bonus);
        }
    }
}
