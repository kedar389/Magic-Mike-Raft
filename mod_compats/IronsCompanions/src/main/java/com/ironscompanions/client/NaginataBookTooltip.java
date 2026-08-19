package com.ironscompanions.client;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.ItemIds;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Writes His Holiness Scripture's tooltip: unique heading, slot count, cast prompt, and the inscribed
 * spells by name and level. No damage, mana or cooldown lines, same as a stock spellbook.
 *
 * <p>The block is the one {@code SpellBook#appendHoverText} writes, rebuilt here because a Companions
 * item cannot inherit it, and inserted after the item name where a spellbook's sits.
 *
 * <p>Two blocks come off the same tooltip: Companions' two lines about the old right-click ability,
 * dropped here by translation key, and IS's imbued-weapon block, cancelled by
 * {@code mixin/ImbuedSpellTooltipMixin}.
 *
 * <p>{@link EventPriority#LOWEST}, so it runs after both.
 */
@Mod.EventBusSubscriber(modid = IronsCompanions.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NaginataBookTooltip {

    private static final Set<String> COMPANIONS_LINES = Set.of(
            "tooltip.item.companions.naginata_book",
            "tooltip.item.companions.naginata_book_2");

    /** Where the spellbook block goes: straight after the item name. */
    private static final int BLOCK_START = 1;

    /** The indent and colour Iron's Spells lists a spellbook's spells in. */
    private static final String SPELL_INDENT = "  ";
    private static final int SPELL_COLOR = 0x8888FE;

    private static final Lazy<Item> BOOK = ItemIds.lazy(ItemIds.NAGINATA_BOOK);

    private NaginataBookTooltip() {
    }

    /** Whether {@code stack} is the Scripture. Also the check {@code ImbuedSpellTooltipMixin} makes. */
    public static boolean isScripture(ItemStack stack) {
        Item book = BOOK.get();
        return book != null && stack.getItem() == book;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isScripture(stack)) {
            return;
        }
        List<Component> lines = event.getToolTip();
        lines.removeIf(line -> COMPANIONS_LINES.contains(keyOf(line)));

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ISpellContainer.isSpellContainer(stack)) {
            return;
        }
        lines.addAll(BLOCK_START, spellbookBlock(ISpellContainer.get(stack), player));
    }

    /** Header, cast prompt and spell list, in the order a stock spellbook prints them. */
    private static List<Component> spellbookBlock(ISpellContainer container, LocalPlayer player) {
        List<Component> block = new ArrayList<>();
        block.add(Component.translatable("tooltip.irons_spellbooks.spellbook_rarity",
                        Component.translatable("tooltip.irons_spellbooks.spellbook_unique")
                                .withStyle(TooltipsUtils.UNIQUE_STYLE))
                .withStyle(ChatFormatting.GRAY));
        block.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count",
                        container.getMaxSpellCount())
                .withStyle(ChatFormatting.GRAY));

        List<SpellSlot> spells = container.getActiveSpells();
        if (spells.isEmpty()) {
            return block;
        }
        block.add(Component.empty());
        block.add(Component.translatable("tooltip.irons_spellbooks.press_to_cast",
                        Component.keybind("key.irons_spellbooks.spellbook_cast"))
                .withStyle(ChatFormatting.GOLD));
        block.add(Component.empty());
        block.add(Component.translatable("tooltip.irons_spellbooks.spellbook_tooltip")
                .withStyle(ChatFormatting.GRAY));
        for (SpellSlot slot : spells) {
            // getTitleComponent is the name and level alone, which is all a spellbook lists.
            Component title = TooltipsUtils.getTitleComponent(slot.spellData(), player)
                    .setStyle(Style.EMPTY.withColor(SPELL_COLOR));
            block.add(Component.literal(SPELL_INDENT).append(title));
        }
        return block;
    }

    /** @return the line's translation key, or "" for a literal line, so callers need no null check */
    private static String keyOf(Component line) {
        return line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
    }
}
