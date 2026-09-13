package com.ironscompanions.mixin;

import com.ironscompanions.attribute.MageStaffBonuses;
import com.ironscompanions.staff.MageStaffCasting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes {@code companions:mage_staff} cast, the way Iron's Spells' own six staves do: right-click casts
 * the spell selected on the equipped spellbook, holding it channels, releasing ends the cast.
 * {@link MageStaffCasting} is the behaviour, {@link MageStaffBonuses} is what the staff carries while
 * held.
 *
 * <p>The target is Companions' generic GeckoLib item class, which the mage staff is the only item
 * registered under, so this reaches nothing else. It overrides none of these five, so all five merge in
 * cleanly. Right-clicking the Golden Allay to tame it is an entity interaction and resolves before
 * {@code Item#use}, so taming is untouched.
 *
 * <p>{@code remap = false} with SRG names, as in {@link NaginataBookSpellbookMixin}: these are methods
 * the target inherits without overriding, so they are merged in under the names the game runs with.
 * Reobfuscation cannot rename them for us, because this class does not extend {@code Item} and so it
 * cannot see them as overrides.
 */
@Mixin(targets = "dev.xylonity.companions.common.item.generic.GenericGeckoItem", remap = false)
public class MageStaffCastingMixin {

    /** SRG {@code m_7203_} is {@code Item#use(Level, Player, InteractionHand)}. */
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        return MageStaffCasting.use(level, player, hand);
    }

    /** SRG {@code m_8105_} is {@code Item#getUseDuration(ItemStack)}. */
    public int m_8105_(ItemStack stack) {
        return MageStaffCasting.getUseDuration(stack);
    }

    /** SRG {@code m_6164_} is {@code Item#getUseAnimation(ItemStack)}. */
    public UseAnim m_6164_(ItemStack stack) {
        return MageStaffCasting.getUseAnimation(stack);
    }

    /** SRG {@code m_5551_} is {@code Item#releaseUsing(ItemStack, Level, LivingEntity, int)}. */
    public void m_5551_(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        MageStaffCasting.releaseUsing(stack, level, entity, timeLeft);
    }

    /** Forge's own name, so no SRG. Keeps the staff from re-equipping every time the spell changes. */
    public boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, boolean slotChanged) {
        return MageStaffCasting.shouldCauseReequipAnimation(from, to, slotChanged);
    }
}
