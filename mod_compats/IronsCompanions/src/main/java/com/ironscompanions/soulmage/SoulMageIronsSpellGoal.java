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
 * Makes the Soul Mage cast whatever Iron's Spells scrolls sit in its three slots, at its current
 * target. The stock {@code SoulMage*Goal}s go inert by themselves once the books are gone, since their
 * {@code hasRequiredBook()} can no longer pass, so this goal is simply added alongside them.
 *
 * <p>Scrolls reach those slots via {@code mixin/SoulMageBookSlotMixin}.
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
