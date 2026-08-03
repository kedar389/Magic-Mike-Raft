package com.ironscompanions.soulmage;

import dev.xylonity.companions.common.entity.companion.SoulMageEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * Phase 7, makes the Soul Mage cast Iron's Spells based on the scrolls in its 3-slot inventory,
 * instead of the (removed) Companions magic books. Any IS spell inscribed on a scroll placed in the
 * Soul Mage can be cast at its target. The old {@code SoulMage*Goal}s go inert on their own once the
 * books are gone (their {@code hasRequiredBook()} always fails), so we only need to add this goal.
 *
 * <p>VERIFY(build): the IS casting + spell-container API (see the marked calls). This is the piece
 * most likely to need adjustment against the shipped 3.16.2 API.
 */
public class SoulMageIronsSpellGoal extends Goal {

    private final SoulMageEntity mage;
    private final int minCooldown;
    private final int maxCooldown;
    private int nextUseTick;
    private int castTicks;
    private SpellData chosen;

    public SoulMageIronsSpellGoal(SoulMageEntity mage, int minCooldown, int maxCooldown) {
        this.mage = mage;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mage.tickCount < nextUseTick) {
            return false;
        }
        LivingEntity target = mage.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        chosen = pickSpell();
        return chosen != null;
    }

    @Override
    public boolean canContinueToUse() {
        return castTicks < 12 && mage.getTarget() != null && mage.getTarget().isAlive();
    }

    @Override
    public void start() {
        castTicks = 0;
        faceTarget();
    }

    @Override
    public void tick() {
        faceTarget();
        castTicks++;
        if (castTicks == 6 && !mage.level().isClientSide && chosen != null) {
            cast(chosen, mage.getTarget());
        }
    }

    @Override
    public void stop() {
        nextUseTick = mage.tickCount + minCooldown + mage.getRandom().nextInt(Math.max(1, maxCooldown - minCooldown + 1));
        chosen = null;
    }

    /** Collect every spell inscribed on a scroll (or book) in the Soul Mage's inventory, pick one. */
    private SpellData pickSpell() {
        List<SpellData> spells = new ArrayList<>();
        for (int i = 0; i < mage.inventory.getContainerSize(); i++) {
            ItemStack stack = mage.inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            // Scrolls and spellbooks both qualify. getActiveSpells() hands back slots, not the
            // SpellData itself, so unwrap each one.
            ISpellContainer container = ISpellContainer.get(stack);
            if (container != null && !container.isEmpty()) {
                for (SpellSlot slot : container.getActiveSpells()) {
                    spells.add(slot.spellData());
                }
            }
        }
        if (spells.isEmpty()) {
            return null;
        }
        return spells.get(mage.getRandom().nextInt(spells.size()));
    }

    private void cast(SpellData data, LivingEntity target) {
        if (target == null) {
            return;
        }
        // VERIFY(build): SpellData accessors + mob-cast entrypoint.
        AbstractSpell spell = data.getSpell();
        int spellLevel = data.getLevel();
        MagicData magicData = new MagicData(false); // non-player caster
        spell.onCast(mage.level(), spellLevel, mage, CastSource.MOB, magicData);
    }

    private void faceTarget() {
        LivingEntity target = mage.getTarget();
        if (target == null) {
            return;
        }
        mage.getLookControl().setLookAt(target, 30f, 30f);
        double dx = target.getX() - mage.getX();
        double dz = target.getZ() - mage.getZ();
        double dy = target.getEyeY() - mage.getEyeY();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * (180d / Math.PI)) - 90f;
        float pitch = (float) (-(Mth.atan2(dy, horiz) * (180d / Math.PI)));
        mage.setYRot(yaw);
        mage.yBodyRot = yaw;
        mage.yHeadRot = yaw;
        mage.setXRot(pitch);
    }
}
