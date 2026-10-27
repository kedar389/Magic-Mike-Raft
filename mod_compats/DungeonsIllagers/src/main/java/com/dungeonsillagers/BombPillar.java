package com.dungeonsillagers;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The Geomancer's bomb pillar (DT_MobPropActorDefinition BombPillar): rises, burns down a 2.6 s fuse (GA_TimedAOE) and
 * explodes for 25 game damage within 450 UE (the game's 300, raised half again), hurting no illager. It has 17
 * health: break it first and it crumbles without exploding.
 */
public class BombPillar extends Prop {

    public static final int FUSE = 52;
    private static final double RADIUS = 450 * MoveMob.UE;
    /** Fuse tick at which the pillar starts heating (AnimNS_GeomancerBombBlock_PreExplosion, 1.38 s). */
    public static final int HEAT_FROM = 28;
    private float health = 17.0F;

    public BombPillar(EntityType<? extends BombPillar> type, Level level) {
        super(type, level, "animation.bomb_pillar.explode", 55);
    }

    /** Montage AM_Geomancer_BombPillar_Explode: appear 0.03 s, ignite 0.1 s, pre-explosion 1.38-2.44 s. */
    @Override
    protected void onAge(int t) {
        if (t == 0) {
            sound(IllagerSound.GEOMANCER_BOMB_SPAWN, 1.5F);
            Vfx.play(this, Vfx.Effect.BOMB_APPEAR, this.position(), this.getYRot(), 0);
        }
        if (t == HEAT_FROM) {
            Vfx.play(this, Vfx.Effect.BOMB_FUSE, this.position(), this.getYRot(), FUSE - HEAT_FROM);
        }
        if (t == FUSE) {
            explode();
        }
    }

    private void explode() {
        sound(IllagerSound.GEOMANCER_BOMB_EXPLODE, 3.0F);
        Vfx.play(this, Vfx.Effect.BOMB_EXPLODE, this.position(), this.getYRot(), (float) RADIUS);
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
        } else {
            sound(IllagerSound.GEOMANCER_PILLAR_DISAPPEAR, 1.5F);
            Vfx.play(this, Vfx.Effect.WALL_CRUMBLE, this.position(), this.getYRot(), 0);
            this.discard();
        }
        return true;
    }
}
