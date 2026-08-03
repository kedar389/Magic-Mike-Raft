package com.attributemerge.client;

import com.attributemerge.AttributeMerge;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

/**
 * Renders Apothic attributes on curios the way Apothic renders them on weapons and armour.
 *
 * <p>Curios does not use Apothic's formatter. {@code ClientEventHandler.onTooltip} builds its
 * "when worn" lines itself with vanilla's {@code ItemStack.ATTRIBUTE_MODIFIER_FORMAT} and vanilla's
 * {@code attribute.modifier.*} keys, so an {@code attributeslib} attribute on a curio prints its raw
 * value while the same attribute on a sword goes through {@code PercentBasedAttribute} and prints a
 * percentage. Ring of the Grudge showed {@code +0.1 Crit Damage} where The Annihilator showed
 * {@code +75%}. Apothic's own Curios compat only adds the Attributes button to the Curios screen.
 *
 * <p>This runs last on {@link ItemTooltipEvent} and replaces those lines. Matching on the vanilla
 * translation keys selects exactly the lines Apothic did not format: Apothic rewrites the equipment
 * lines during tooltip construction, before any event fires, so what is still vanilla-keyed by the
 * time this runs was added by a listener.
 *
 * <p>This covers every attribute, not only {@code attributeslib} ones. Apothic's {@code AttributeMixin}
 * injects {@code IFormattableAttribute} into vanilla's {@code Attribute} base class, so every
 * attribute in the game implements it and picks up either the interface's flat default or its own
 * override ({@code PercentBasedAttribute}, {@code BooleanAttribute}). A curio granting
 * {@code generic.armor} therefore ends up rendered exactly as armour with the same modifier is,
 * which is the point.
 *
 * <p>The amount is recovered by parsing the number vanilla already formatted, which is how the line
 * can be rebuilt without a compile dependency on Curios or a second pass over the item's slots. The
 * cost is vanilla's {@code #.##} rounding: an amount of 0.075 has already been printed as 0.08 and
 * is reformatted as 8%, not 7.5%. Apothic rounds the same way for display, so the two agree on
 * everything except the hidden digits.
 *
 * <p>Client only, and inert when Apothic Attributes is absent.
 */
@Mod.EventBusSubscriber(modid = AttributeMerge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class CurioTooltipFormatHandler {

    private static final String PLUS = "attribute.modifier.plus.";
    private static final String TAKE = "attribute.modifier.take.";

    /** Formatting never reads the id, and a constant avoids a random UUID per line per frame. */
    private static final UUID DISPLAY_ONLY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private CurioTooltipFormatHandler() {
    }

    /**
     * LOWEST so Curios has already added its lines. Apothic is not a listener here, so there is no
     * ordering to negotiate with it.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ApothicFormatter.isAvailable()) return;

        List<Component> lines = event.getToolTip();
        for (int i = 0; i < lines.size(); i++) {
            Component reformatted = reformat(lines.get(i), event.getFlags());
            if (reformatted != null) lines.set(i, reformatted);
        }
    }

    /** The Apothic-formatted replacement for a vanilla-formatted modifier line, or null to keep it. */
    private static Component reformat(Component line, TooltipFlag flag) {
        if (!(line.getContents() instanceof TranslatableContents contents)) return null;

        String key = contents.getKey();
        boolean negative = key.startsWith(TAKE);
        if (!negative && !key.startsWith(PLUS)) return null;

        AttributeModifier.Operation operation = operationOf(key.substring(PLUS.length()));
        if (operation == null) return null;

        // Vanilla builds these as (formatted amount, attribute name). Anything else is not a line
        // this code wrote the format for, so leave it be.
        Object[] args = contents.getArgs();
        if (args.length != 2
                || !(args[1] instanceof Component name)
                || !(name.getContents() instanceof TranslatableContents nameContents)) {
            return null;
        }

        Attribute attribute = ApothicFormatter.formattableAttribute(nameContents.getKey());
        if (attribute == null) return null;

        Double shown = parseAmount(args[0]);
        if (shown == null) return null;

        // Vanilla prints ADDITION raw and the two multiply operations pre-multiplied by 100, and
        // prints the magnitude with the sign carried by the "take" key.
        double amount = operation == AttributeModifier.Operation.ADDITION ? shown : shown / 100.0D;
        if (negative) amount = -amount;

        return ApothicFormatter.format(attribute,
                new AttributeModifier(DISPLAY_ONLY, "attribute_merge:display", amount, operation), flag);
    }

    /** The operation vanilla encoded in the key suffix, or null if the suffix is not one. */
    private static AttributeModifier.Operation operationOf(String suffix) {
        return switch (suffix) {
            case "0" -> AttributeModifier.Operation.ADDITION;
            case "1" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "2" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> null;
        };
    }

    /** Read back the number vanilla formatted, using vanilla's own formatter so it round-trips. */
    private static Double parseAmount(Object arg) {
        if (arg instanceof Number number) return number.doubleValue();
        if (!(arg instanceof String text)) return null;
        try {
            // DecimalFormat is not thread safe and this instance is vanilla's, shared with the
            // tooltip code that produced the string.
            synchronized (ItemStack.ATTRIBUTE_MODIFIER_FORMAT) {
                return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.parse(text).doubleValue();
            }
        } catch (ParseException e) {
            return null;
        }
    }
}
