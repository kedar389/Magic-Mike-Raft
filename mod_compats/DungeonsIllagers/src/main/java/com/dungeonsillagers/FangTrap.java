package com.dungeonsillagers;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * The Supreme Evoker's fang trap (DT_MobPropActorDefinition EvokerFangTrap): rises, snaps at 1.03 s for 15 game damage
 * within 140 UE, applies Slowed and throws the target back at 45 degrees (Knockback.Strength 1600).
 */
public class FangTrap extends Prop {

    private static final int SNAP = 21;
    private static final double RADIUS = 140 * MoveMob.UE;

    public FangTrap(EntityType<? extends FangTrap> type, Level level) {
        super(type, level, "animation.fang_trap.attack", 34);
    }

    /** Montage AM_FangTrap_Attack: rise sound 0.16 s, area attack 1.03 s, snap 1.07 s, debris 1.48 s. */
    @Override
    protected void onAge(int t) {
        if (t == 0) {
            burst(ParticleTypes.WITCH, this.position(), 10, 0.3, 0.1);
        }
        if (t == 3) {
            sound(IllagerSound.SUPREME_EVOKER_FANG_RISE, 1.0F);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.position(), 12, 0.3, 0.1);
        }
        if (t == SNAP) {
            sound(IllagerSound.SUPREME_EVOKER_FANG_SNAP, 1.2F);
            burst(ParticleTypes.CRIT, this.position().add(0, 0.8, 0), 12, 0.4, 0.4);
            strike(this.position(), RADIUS, MoveMob.damageOf(15), this.damageSources().indirectMagic(this, owner()), fresh(), victim -> {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
                Vec3 away = victim.position().subtract(this.position()).multiply(1, 0, 1);
                Vec3 dir = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize().scale(0.45);
                victim.push(dir.x, 0.45, dir.z);
            });
        }
        if (t == 30) {
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.position(), 10, 0.3, 0.2);
        }
    }
}
