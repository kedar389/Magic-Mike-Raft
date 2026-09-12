package com.ironscompanions.mixin;

import com.ironscompanions.attribute.MageStaffBonuses;
import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Makes {@code companions:mage_staff} cast, the way Iron's Spells' own six staves do: right-click casts
 * the spell selected on the equipped spellbook, holding it channels, releasing ends the cast.
 * {@link MageStaffBonuses} supplies what the staff carries while held.
 *
 * <p>The five methods are the whole of {@link CastingItem}'s behaviour, forwarded to an instance of it.
 * Forwarding rather than copying keeps the cast on Iron's Spells' own code: mana, cooldowns,
 * {@code CastSource}, the spell wheel and the cast animation all follow from
 * {@code AbstractSpell#attemptInitiateCast}, which is what the class calls. {@code CastingItem} holds no
 * state, so one unregistered instance serves every call.
 *
 * <p>The target is Companions' generic GeckoLib item class, which the mage staff is the only item
 * registered under, so this reaches nothing else. Right-clicking the Golden Allay to tame it is an
 * entity interaction and resolves before {@code Item#use}, so taming is untouched.
 *
 * <p>{@code remap = false} with SRG names, as in {@link NaginataBookSpellbookMixin}: these are methods
 * the target inherits without overriding, so they are merged in under the names the game runs with.
 */
@Mixin(targets = "dev.xylonity.companions.common.item.generic.GenericGeckoItem", remap = false)
public class MageStaffCastingMixin {

    @Unique
    private final CastingItem ironsCompanions$casting = new CastingItem(new Item.Properties());

    /** SRG {@code m_7203_} is {@code Item#use(Level, Player, InteractionHand)}. */
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        return ironsCompanions$casting.use(level, player, hand);
    }

    /** SRG {@code m_8105_} is {@code Item#getUseDuration(ItemStack)}. */
    public int m_8105_(ItemStack stack) {
        return ironsCompanions$casting.getUseDuration(stack);
    }

    /** SRG {@code m_6164_} is {@code Item#getUseAnimation(ItemStack)}. */
    public UseAnim m_6164_(ItemStack stack) {
        return ironsCompanions$casting.getUseAnimation(stack);
    }

    /** SRG {@code m_5551_} is {@code Item#releaseUsing(ItemStack, Level, LivingEntity, int)}. */
    public void m_5551_(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        ironsCompanions$casting.releaseUsing(stack, level, entity, timeLeft);
    }

    /** Forge's own name, so no SRG. Keeps the staff from re-equipping every time the spell changes. */
    public boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, boolean slotChanged) {
        return ironsCompanions$casting.shouldCauseReequipAnimation(from, to, slotChanged);
    }
}
