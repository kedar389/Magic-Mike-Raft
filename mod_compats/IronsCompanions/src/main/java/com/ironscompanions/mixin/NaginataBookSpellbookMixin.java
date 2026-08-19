package com.ironscompanions.mixin;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.IronsCompanionsSpells;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Turns {@code companions:book_naginata} into <b>His Holiness Scripture</b>, an Iron's Spells spellbook.
 * Companions' own recipe (four Relic Gold around a book) still makes it.
 *
 * <p>All of this is item-level, so none of it is reachable from an event:
 *
 * <ul>
 *   <li>{@link IPresetSpellContainer} makes it a spell container. IS's {@code ItemStackMixin} calls
 *       {@link #initializeSpellContainer(ItemStack)} on every stack of an item implementing it, so the
 *       crafted, creative-tab and JEI copies all arrive inscribed. Casting is NBT-driven from there.</li>
 *   <li>{@code m_7626_} ({@code Item#getName}) renames it in inventory, creative tab, JEI and anvil. A
 *       lang override would instead depend on this mod's pack loading after Companions'.</li>
 *   <li>{@link ICurioItem} is what gives the stack a curio capability, which Curios' {@code
 *       RightClickItem} handler needs to equip it. {@code data/curios/tags/items/spellbook.json} keeps
 *       the spellbook slot the only one that accepts it.</li>
 *   <li>{@code use} is cancelled at HEAD: it spawned Companions' naginatas directly, with no mana and no
 *       school. It is only reached when the spellbook slot is full, since the equip cancels first.</li>
 *   <li>{@link UniqueItem} is the first thing {@code Utils.canImbue} checks, so the book stops showing
 *       "Can be Imbued (1/1)", same as IS's own unique spellbooks.</li>
 * </ul>
 *
 * <p>Container flags are a stock spellbook's ({@code spellWheel} and {@code mustEquip} true): spell
 * wheel, spellbook slot, {@code CastSource.SPELLBOOK} mana and cooldown.
 *
 * <p>{@code remap = false} with SRG names, as in {@link SoulMageBookSlotMixin}.
 */
// Injectors are called by injected bytecode with a signature fixed by the target method, so unused
// handlers and parameters are expected here.
@SuppressWarnings({"UnusedMethod", "UnusedVariable"})
@Mixin(targets = "dev.xylonity.companions.common.item.book.books.NaginataBook", remap = false)
public class NaginataBookSpellbookMixin implements ISpellbook, IPresetSpellContainer, UniqueItem, ICurioItem {

    /** Ten slots: the locked Naginata, and nine the player scribes themselves. */
    private static final int SPELL_SLOTS = 10;

    /** The renamed item's own key, in this mod's lang file rather than Companions'. */
    private static final String NAME_KEY = "item.irons_companions.his_holiness_scripture";

    @Override
    public void initializeSpellContainer(ItemStack stack) {
        if (stack == null || ISpellContainer.isSpellContainer(stack)) {
            return;
        }
        // A stack built before our DeferredRegister fires has no spell to write, and a container with an
        // empty locked slot is worse than none.
        if (!IronsCompanionsSpells.NAGINATA.isPresent()) {
            IronsCompanions.LOGGER.debug("[IronsCompanions] Naginata spell not registered yet, "
                    + "leaving book_naginata stack empty");
            return;
        }
        AbstractSpell naginata = IronsCompanionsSpells.NAGINATA.get();
        ISpellContainerMutable container = ISpellContainer.create(SPELL_SLOTS, true, true).mutableCopy();
        // Max level comes from the spell config rather than a constant here. locked = true: the Naginata
        // cannot be scribed over, the other nine slots stay free.
        container.addSpell(naginata, naginata.getMaxLevel(), true);
        ISpellContainer.set(stack, container.toImmutable());
    }

    /** SRG {@code m_7626_} is {@code Item#getName(ItemStack)}. */
    public Component m_7626_(ItemStack stack) {
        return Component.translatable(NAME_KEY);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    /** IS's own spellbook equip sound, so this one sounds the same. */
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundRegistry.EQUIP_SPELL_BOOK.get(), 1.0f, 1.0f);
    }

    @Inject(method = "m_7203_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;"
            + "Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At("HEAD"), cancellable = true)
    private void ironsCompanions$noNativeCast(Level level, Player player, InteractionHand hand,
                                              CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        cir.setReturnValue(InteractionResultHolder.pass(player.getItemInHand(hand)));
    }
}
