package com.ironscompanions.client;

import com.ironscompanions.IronsCompanions;
import dev.xylonity.companions.client.gui.screen.SoulMageScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Says under the Soul Mage's book screen that summoning spells will not be cast, which is where a player
 * stands when deciding what to hand it. What is skipped is {@code soulmage/SummonSpells}.
 *
 * <p>{@link ScreenEvent.Render.Post} rather than a mixin: the line is drawn after Companions' own screen
 * and below its panel, so it needs nothing from the screen but its position and size.
 */
@Mod.EventBusSubscriber(modid = IronsCompanions.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoulMageScreenNote {

    private static final String NOTE_KEY = "gui.irons_companions.soul_mage.no_summons";

    /** Gap between the bottom edge of Companions' panel and the line. */
    private static final int MARGIN = 5;

    private static final int COLOR = 0xC0C0C0;

    private SoulMageScreenNote() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof SoulMageScreen screen)) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        Component note = Component.translatable(NOTE_KEY);
        GuiGraphics graphics = event.getGuiGraphics();
        int x = screen.getGuiLeft() + (screen.getXSize() - font.width(note)) / 2;
        int y = screen.getGuiTop() + screen.getYSize() + MARGIN;
        graphics.drawString(font, note, x, y, COLOR);
    }
}
