package com.dungeonsillagers;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * The Geomancer's bomb pillar (DT_MobPropActorDefinition BombPillar): rises, burns down a 2.6 s fuse (GA_TimedAOE) and
 * explodes for 25 game damage within 300 UE, hurting no illager. It has 17 health: break it first and it crumbles
 * without exploding.
 */
public class BombPillar extends Prop {

    private static final int FUSE = 52;
    private static final double RADIUS = 300 * MoveMob.UE;
    private float health = 17.0F;

    public BombPillar(EntityType<? extends BombPillar> type, Level level) {
        super(type, level, "animation.bomb_pillar.explode", 55);
    }

    /** Montage AM_Geomancer_BombPillar_Explode: appear 0.03 s, ignite 0.1 s, pre-explosion 1.38-2.44 s. */
    @Override
    protected void onAge(int t) {
        if (t == 0) {
            sound(IllagerSound.GEOMANCER_BOMB_SPAWN, 1.5F);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()), this.position(), 40, 0.6, 0.2);
        }
        if (t >= 2 && t < 10 && t % 2 == 0) {
            burst(ParticleTypes.SMALL_FLAME, this.position().add(0, 2.6, 0), 3, 0.2, 0.1);
        }
        if (t >= 28 && t < FUSE && t % 2 == 0) {
            int heat = (t - 28) / 6 + 1;
            burst(ParticleTypes.FLAME, this.position().add(0, 1.6, 0), heat, 0.45, 1.0);
            burst(ParticleTypes.SMOKE, this.position().add(0, 2.8, 0), heat, 0.3, 0.2);
        }
        if (t == FUSE) {
            explode();
        }
    }

    private void explode() {
        sound(IllagerSound.GEOMANCER_BOMB_EXPLODE, 3.0F);
        burst(ParticleTypes.EXPLOSION, this.position().add(0, 1.0, 0), 6, RADIUS * 0.5, 0.6);
        burst(ParticleTypes.LARGE_SMOKE, this.position().add(0, 0.5, 0), 30, RADIUS * 0.5, 0.4);
        burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.position().add(0, 1.0, 0), 60, 0.8, 1.0);
        strike(this.position(), RADIUS, MoveMob.damageOf(25), this.damageSources().explosion(this, owner()), fresh(),
                victim -> victim.push(0, 0.3, 0));
        this.discard();
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved() || this.isInvulnerableTo(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker != null && owner() != null && (attacker == owner() || owner().isAlliedTo(attacker))) {
            return false;
        }
        this.health -= amount;
        if (this.health > 0) {
            sound(IllagerSound.GEOMANCER_PILLAR_HIT, 1.0F);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.position().add(0, 1.5, 0), 12, 0.4, 0.6);
        } else {
            sound(IllagerSound.GEOMANCER_PILLAR_DISAPPEAR, 1.5F);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.position().add(0, 1.5, 0), 50, 0.5, 1.2);
            this.discard();
        }
        return true;
    }
}
