package com.ironscompanions.mixin;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Iron's Spells scrolls go into the Soul Mage's three book slots.
 *
 * <p>Without this the whole Soul Mage feature is unreachable: its goal reads spells from scrolls in
 * that inventory, but {@code SoulMageContainerMenu$BookSlot.mayPlace} is
 * {@code stack.getItem() instanceof AbstractMagicBook}, so only Companions' own magic books are
 * accepted, and those are exactly what this mod removes. A slot predicate baked into another mod's
 * class cannot be widened by any Forge event, hence a mixin.
 *
 * <p>Accepts any IS spell container, so a spellbook works as well as a scroll.
 *
 * <p>{@code remap = false} with the SRG method name: the target is a mod class, and the Companions jar
 * as shipped already overrides {@code mayPlace} under its SRG name {@code m_5857_}, which is also the
 * name at runtime (Forge 1.20.1 keeps official class names and SRG member names in production). Class
 * names in the descriptor are therefore the official ones.
 */
// Mixin injectors are called by injected bytecode, never from Java, and their signature is fixed by
// the target method, so unused handlers and unread parameters are expected here rather than dead code.
@SuppressWarnings({"UnusedMethod", "UnusedVariable"})
@Mixin(targets = "dev.xylonity.companions.common.container.SoulMageContainerMenu$BookSlot", remap = false)
public class SoulMageBookSlotMixin {

    @Inject(method = "m_5857_(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void ironsCompanions$allowSpellScrolls(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && ISpellContainer.isSpellContainer(stack)) {
            cir.setReturnValue(true);
        }
    }
}
