package com.ironscompanions.staff;

import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Iron's Spells' staff behaviour, reachable from an item that cannot extend {@link CastingItem}.
 *
 * <p>These five methods are the whole of what {@link CastingItem} overrides, so an item that forwards
 * all five casts exactly as the mod's own six staves do: {@code use} builds a {@code
 * SpellSelectionManager} for the player and calls {@code AbstractSpell#attemptInitiateCast}, which is
 * what carries mana, cooldowns, {@code CastSource}, the spell wheel and the cast animation.
 * {@code mixin/MageStaffCastingMixin} is the only caller.
 *
 * <p>Forwarding rather than reimplementing keeps the cast on Iron's Spells' own code, so an update to
 * how a staff casts arrives here for free.
 *
 * <p>{@link CastingItem} holds no state and none of the five methods reads {@code this}, so one
 * unregistered instance serves every call. It is built here rather than in the mixin because a field
 * initialiser in a mixin has to be transplanted into the target's constructor, and this mod's mixins
 * cannot be run in a dev environment to prove that worked (Iron's Spells ships SRG-named mixins, which
 * the Mojang-mapped dev runtime rejects).
 */
public final class MageStaffCasting {

    private static final CastingItem STAFF = new CastingItem(new Item.Properties());

    private MageStaffCasting() {
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return STAFF.use(level, player, hand);
    }

    public static int getUseDuration(ItemStack stack) {
        return STAFF.getUseDuration(stack);
    }

    public static UseAnim getUseAnimation(ItemStack stack) {
        return STAFF.getUseAnimation(stack);
    }

    public static void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        STAFF.releaseUsing(stack, level, entity, timeLeft);
    }

    public static boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, boolean slotChanged) {
        return STAFF.shouldCauseReequipAnimation(from, to, slotChanged);
    }
}
