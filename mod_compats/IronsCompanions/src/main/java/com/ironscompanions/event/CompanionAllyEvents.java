package com.ironscompanions.event;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Phase 5, ally/faction rules between the player, their Iron's Spells summons, and their Companions.
 *
 * <p>Implemented: <b>no friendly fire</b>, an attack is cancelled when attacker and victim share an
 * owner, or when one owns the other. Owner is read via {@link OwnableEntity} (Companions' tamables and
 * IS summons both expose an owner UUID).
 *
 * <p>TODO(build): make IS heal/buff spells treat companions as valid ally <i>targets</i>. IS resolves
 * ally targeting through its own predicate; the cleanest hook is likely a Mixin into IS's ally check
 * (or an IS ally event if one is exposed). Left for the same pass as the Soul Mage mixin (Phase 7).
 * VERIFY: confirm IS summons implement {@link OwnableEntity} in 3.16.2; if not, add an instanceof for
 * {@code io.redspace.ironsspellbooks.api.entity.IMagicSummon} and read its summoner there.
 */
public class CompanionAllyEvents {

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == victim) {
            return;
        }
        if (areAllies(attacker, victim)) {
            event.setCanceled(true);
        }
    }

    private static boolean areAllies(Entity a, Entity b) {
        UUID ownerA = ownerOf(a);
        UUID ownerB = ownerOf(b);
        UUID idA = a.getUUID();
        UUID idB = b.getUUID();

        // one owns the other
        if (idA.equals(ownerB) || idB.equals(ownerA)) {
            return true;
        }
        // both owned by the same player
        return ownerA != null && ownerA.equals(ownerB);
    }

    private static UUID ownerOf(Entity entity) {
        if (entity instanceof Player) {
            return entity.getUUID();
        }
        if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        return null;
    }
}
