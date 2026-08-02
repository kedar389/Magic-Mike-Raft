package com.attributemerge.mixin;

import com.attributemerge.MergeEngine;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes Cataclysm's curio attributes, which are unreachable through
 * {@code CurioAttributeModifierEvent}.
 *
 * <p>{@code CuriosItem.getAttributeModifiers} does this:
 *
 * <pre>
 *   Multimap map = LinkedHashMultimap.create();
 *   if (slotContext.identifier().equals(this.attributeSlot) &amp;&amp; this.attributes != null) {
 *       map.putAll(this.attributes.apply(slotContext.index()));
 *   }
 *   ... slot modifiers ...
 *   MinecraftForge.EVENT_BUS.post(new CurioAttributeModifierEvent(stack, slotContext, uuid, map));
 *   return map;                       // its own map, NOT event.getModifiers()
 * </pre>
 *
 * <p>Two separate problems, both fixed here:
 *
 * <ol>
 *   <li><b>The slot gate.</b> {@code attributeSlot} is baked in at registration.
 *       {@code ring_of_grudged} declares {@code "rings"}, but this pack sets
 *       {@code id=rings;size=0} in {@code curios-common.toml} and routes the item into the standard
 *       {@code ring} slot so every ring in the pack shares one slot. The identifier is then
 *       {@code "ring"}, the gate fails, and the item grants nothing at all. {@link #attributeMerge$aliasRingSlot}
 *       makes {@code ring} satisfy {@code rings}, and nothing else.</li>
 *   <li><b>The discarded event.</b> {@code CurioAttributeModifierEvent.getModifiableMap()} lazily
 *       copies with {@code LinkedHashMultimap.create(originalModifiers)}, so {@code addModifier} and
 *       {@code removeAttribute} write to a copy, and the method returns the original. No listener
 *       can change a Cataclysm curio's attributes. {@link #attributeMerge$mergeReturnedModifiers}
 *       merges the returned multimap directly instead.</li>
 * </ol>
 *
 * <p>Targeted by name string, so this adds no compile dependency on Cataclysm.
 * {@link AttributeMergeMixinPlugin} skips the whole config when Cataclysm is absent. Both injections
 * use {@code require = 1}: if a Cataclysm update changes the method, the mod fails loudly at load
 * rather than silently reverting the ring to granting nothing.
 */
@Mixin(targets = "com.github.L_Ender.cataclysm.items.CuriosItem.CuriosItem", remap = false)
public class CataclysmCuriosMixin {

    /**
     * The only {@code String.equals} call in the method is the slot gate,
     * {@code slotContext.identifier().equals(this.attributeSlot)}, so this redirect is unambiguous.
     *
     * @param actual   the receiver, {@code slotContext.identifier()}, the slot the curio sits in
     * @param expected the argument, {@code this.attributeSlot}, the slot the item was registered for
     */
    @Redirect(
            method = "getAttributeModifiers",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"),
            require = 1,
            remap = false
    )
    private boolean attributeMerge$aliasRingSlot(String actual, Object expected) {
        if (actual.equals(expected)) return true;
        // The one alias this pack needs. Deliberately not a general table: every other Cataclysm
        // curio sits in the slot it was registered for.
        return "ring".equals(actual) && "rings".equals(expected);
    }

    /**
     * Merge the map Cataclysm is about to return. Runs for every Cataclysm curio; for the ones
     * carrying no duplicate attribute this is one {@code isEmpty} check and a few identity-hash
     * lookups, allocating nothing.
     *
     * <p>Takes no target arguments on purpose: Mixin allows a callback to skip capturing them, which
     * keeps Curios' {@code SlotContext} out of this class and the compile classpath.
     */
    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), require = 1, remap = false)
    private void attributeMerge$mergeReturnedModifiers(
            CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        MergeEngine.mergeInPlace(cir.getReturnValue());
    }
}
