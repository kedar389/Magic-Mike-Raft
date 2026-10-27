package com.dungeonsillagers;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * The Geomancer's stone pillar (BP_Geomancer_WallPillar): unhittable, solid while risen (montage collision 0.61-2.91 s),
 * crumbles at 2.6 s and is gone at 3.3 s. Patterns of them wall the target in.
 */
public class WallPillar extends Prop {

    private static final int SOLID_FROM = 12, SOLID_UNTIL = 58;
    private static final EntityDataAccessor<Boolean> SOLID = SynchedEntityData.defineId(WallPillar.class, EntityDataSerializers.BOOLEAN);

    public WallPillar(EntityType<? extends WallPillar> type, Level level) {
        super(type, level, "animation.wall_pillar.rise", 66);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SOLID, false);
    }

    @Override
    protected void onAge(int t) {
        if (t == 2) {
            sound(IllagerSound.GEOMANCER_PILLAR_SPAWNED, 1.2F);
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.position(), 25, 0.4, 0.3);
        }
        if (t == SOLID_FROM) {
            this.entityData.set(SOLID, true);
        }
        if (t == 52) {
            sound(IllagerSound.GEOMANCER_PILLAR_CRUMBLE, 1.2F);
        }
        if (t >= 52 && t % 3 == 0) {
            burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.position().add(0, 1.2, 0), 10, 0.4, 0.8);
        }
        if (t == SOLID_UNTIL) {
            this.entityData.set(SOLID, false);
        }
    }

    /** Players and mobs collide with it like a boat while it stands. */
    @Override
    public boolean canBeCollidedWith() {
        return this.entityData.get(SOLID);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
