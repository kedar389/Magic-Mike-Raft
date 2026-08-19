package com.ironscompanions.mixin;

import com.ironscompanions.client.NaginataBookTooltip;
import io.redspace.ironsspellbooks.player.ClientPlayerEvents;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Iron's Spells' imbued-weapon tooltip off His Holiness Scripture, leaving
 * {@link NaginataBookTooltip} to write a spellbook's instead.
 *
 * <p>IS chooses between the two by item class: {@code imbuedWeaponTooltips} takes the imbued path for
 * any spell container that is not one of its own {@code SpellBook}s, printing per-spell damage, mana and
 * cooldown under an "Imbued Spell" heading. There is no API to opt out, so the private handler that
 * writes the block is cancelled for this one item. The rest of IS's tooltip is untouched.
 *
 * <p>Client only, so it is listed under {@code client} in {@code irons_companions.mixins.json}.
 */
// Injectors are called by injected bytecode with a signature fixed by the target method, so unused
// handlers and parameters are expected here.
@SuppressWarnings({"UnusedMethod", "UnusedVariable"})
@Mixin(value = ClientPlayerEvents.class, remap = false)
public class ImbuedSpellTooltipMixin {

    @Inject(method = "handleImbuedSpellTooltip(Lnet/minecraft/world/item/ItemStack;"
            + "Lnet/minecraft/client/player/LocalPlayer;Ljava/util/List;Z)V",
            at = @At("HEAD"), cancellable = true)
    private static void ironsCompanions$skipScripture(ItemStack stack, LocalPlayer player,
                                                      List<Component> lines, boolean advanced,
                                                      CallbackInfo ci) {
        if (NaginataBookTooltip.isScripture(stack)) {
            ci.cancel();
        }
    }
}
