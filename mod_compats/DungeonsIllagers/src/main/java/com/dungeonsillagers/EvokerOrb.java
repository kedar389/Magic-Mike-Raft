package com.dungeonsillagers;

import com.dungeonsillagers.client.VfxLibrary;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * The Supreme Evoker's enchanted orb (DT_ProjectileDefinition SupremeEvokerOrb): flies straight at 450 UE/s, no
 * gravity, bursts on the first creature or block it touches. Damage is the ability's 8.
 */
public class EvokerOrb extends Projectile {

    /** 450 UE/s in blocks per tick. */
    public static final double SPEED = 450 * MoveMob.UE / 20.0;
    private static final int LIFETIME = 100;

    public EvokerOrb(EntityType<? extends EvokerOrb> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        if (this.level().isClientSide()) {
            Vec3 at = this.position().add(0, 0.25, 0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VfxLibrary.orbTrail(at, motion, this.tickCount));
        } else {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                this.onHit(hit);
                if (this.isRemoved()) {
                    return;
                }
            }
            if (this.tickCount == 1 || this.tickCount % 20 == 0) {
                this.playSound(IllagerSound.SUPREME_EVOKER_PROJECTILE_WHOOSH.get(), 0.6F, 1.0F);
            }
            if (this.tickCount >= LIFETIME) {
                this.discard();
            }
        }
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(this.getOwner() != null && this.getOwner().isAlliedTo(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = this.getOwner();
        if (result.getEntity() instanceof LivingEntity victim
                && victim.hurt(this.damageSources().indirectMagic(this, owner), MoveMob.damageOf(8))) {
            Vec3 push = this.getDeltaMovement().normalize().scale(0.3);
            victim.push(push.x, 0.15, push.z);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        this.playSound(IllagerSound.SUPREME_EVOKER_PROJECTILE_HIT.get(), 1.0F, 1.0F);
        Vfx.play(this, Vfx.Effect.ORB_HIT, this.position().add(0, 0.25, 0), this.getYRot(), 0);
        this.discard();
    }
}
