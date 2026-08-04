package com.attributemerge.client;

import com.attributemerge.AttributeMerge;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflective access to Apothic Attributes' tooltip formatter.
 *
 * <p>Reached by reflection rather than a compile dependency, for the same reason
 * {@link com.attributemerge.mixin.CataclysmCuriosMixin} targets Cataclysm by name string: the mod
 * builds against Forge alone and no-ops for any family that is not installed.
 *
 * <p>The one method needed is the static
 * {@code IFormattableAttribute.toComponent(Attribute, AttributeModifier, TooltipFlag)}, which
 * renders a whole modifier line the way Apothic renders it on weapons and armour, styling included.
 */
final class ApothicFormatter {

    private static final String IFORMATTABLE_ATTRIBUTE =
            "dev.shadowsoffire.attributeslib.api.IFormattableAttribute";

    /** The {@code IFormattableAttribute} interface, or null when Apothic Attributes is absent. */
    private static final Class<?> INTERFACE;

    /** Handle on the static {@code toComponent}, or null when it could not be resolved. */
    private static final MethodHandle TO_COMPONENT;

    static {
        Class<?> iface = null;
        MethodHandle handle = null;
        try {
            iface = Class.forName(IFORMATTABLE_ATTRIBUTE);
            handle = MethodHandles.lookup().findStatic(iface, "toComponent", MethodType.methodType(
                    MutableComponent.class, Attribute.class, AttributeModifier.class, TooltipFlag.class));
        } catch (ClassNotFoundException e) {
            AttributeMerge.LOGGER.debug("Apothic Attributes is absent, curio tooltips left alone");
        } catch (ReflectiveOperationException e) {
            AttributeMerge.LOGGER.warn("IFormattableAttribute.toComponent has moved, curio tooltips "
                    + "will keep Curios' raw formatting", e);
        }
        INTERFACE = iface;
        TO_COMPONENT = handle;
    }

    /**
     * Description id -> attribute, for attributes Apothic formats. Built on first use: attribute
     * registration is long finished by the time any tooltip renders.
     */
    private static volatile Map<String, Attribute> formattable;

    /** Set if a call ever throws, so a broken formatter costs one log line, not one per frame. */
    private static volatile boolean failed;

    private ApothicFormatter() {
    }

    static boolean isAvailable() {
        return TO_COMPONENT != null && !failed;
    }

    /**
     * The attribute with this description id, if Apothic can format it.
     *
     * <p>In practice that is every registered attribute: Apothic's {@code AttributeMixin} injects
     * {@code IFormattableAttribute} into vanilla's {@code Attribute}. The check is kept because the
     * static {@code toComponent} casts to the interface with no test of its own, so an attribute
     * that somehow lacks it, a failed mixin, a class loaded before Apothic, would be a
     * {@code ClassCastException} on every tooltip frame rather than an unformatted line.
     */
    static Attribute formattableAttribute(String descriptionId) {
        Map<String, Attribute> map = formattable;
        if (map == null) {
            map = new HashMap<>();
            for (Attribute attribute : ForgeRegistries.ATTRIBUTES) {
                if (INTERFACE.isInstance(attribute)) {
                    map.put(attribute.getDescriptionId(), attribute);
                }
            }
            formattable = map;
        }
        return map.get(descriptionId);
    }

    /** The formatted line, or null if the call failed, in which case the caller keeps the original. */
    static Component format(Attribute attribute, AttributeModifier modifier, TooltipFlag flag) {
        try {
            return (Component) TO_COMPONENT.invoke(attribute, modifier, flag);
        } catch (Throwable t) {
            failed = true;
            AttributeMerge.LOGGER.warn("Apothic tooltip formatting threw, curio tooltips will keep "
                    + "Curios' raw formatting for the rest of this session", t);
            return null;
        }
    }
}
