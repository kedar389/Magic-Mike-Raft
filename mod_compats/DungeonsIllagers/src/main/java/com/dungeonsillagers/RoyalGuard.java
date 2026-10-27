package com.dungeonsillagers;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * The Royal Guard (DT_MobType1Definition RoyalGuard, ST_RoyalGuard). His shield (GA_MobShield, ForwardProtection,
 * Shield 30) takes every hit from the front until broken; then he staggers (AM_RoyalGuard_ShieldLoss) and fights
 * shieldless with a jab followed by a two-hit combo. Health 90, 1:1 with the game like the other illagers.
 */
public class RoyalGuard extends MoveMob {

    private static final String NAME = "royal_guard";
    static final Move ATTACK = Move.of(NAME, "attack", 37);
    static final Move SHIELD_LOSS = Move.of(NAME, "shield_loss", 23);
    static final Move SHIELDLESS_ATTACK = Move.of(NAME, "shieldless_attack", 37);
    static final Move COMBO = Move.of(NAME, "combo", 49);
    private static final List<Move> MOVES = List.of(ATTACK, SHIELD_LOSS, SHIELDLESS_ATTACK, COMBO);
    private static final RawAnimation SHIELDLESS_IDLE = RawAnimation.begin().thenLoop("animation.royal_guard.shieldless_idle");
    private static final RawAnimation SHIELDLESS_WALK = RawAnimation.begin().thenLoop("animation.royal_guard.shieldless_walk");
    private static final EntityDataAccessor<Boolean> HAS_SHIELD = SynchedEntityData.defineId(RoyalGuard.class, EntityDataSerializers.BOOLEAN);
    /** Shield attribute: 30 health. Attack range 200 UE in a 30 degree cone. */
    private static final float SHIELD_HEALTH = 30.0F;
    private static final double REACH = 200 * UE;

    private float shield = SHIELD_HEALTH;

    public RoyalGuard(EntityType<? extends Monster> type, Level level) {
        super(type, level, NAME, true);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractIllager.class, MoveMob.class, PrimeEnchanter.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_SHIELD, true);
    }

    public boolean hasShield() {
        return this.entityData.get(HAS_SHIELD);
    }

    @Override
    protected List<Move> moves() {
        return MOVES;
    }

    @Override
    @Nullable
    protected Move spawnMove() {
        return null;
    }

    /** GA_BecomeStaggered is blocked once the shield is gone; losing the shield is his only stagger. */
    @Override
    protected Move staggerMove() {
        return SHIELD_LOSS;
    }

    @Override
    protected float[] staggerAt() {
        return new float[0];
    }

    /** LightAttack cooldown 0.4 s, HeavyAttack and ComboMelee 0.5 s. */
    @Override
    protected int cooldown(Move finished) {
        return finished == ATTACK ? 8 : finished == SHIELDLESS_ATTACK || finished == COMBO ? 10 : 0;
    }

    /** ST_RoyalGuard: Melee With Shield, else Melee Basic Shieldless followed by Melee Combo Shieldless. */
    @Override
    @Nullable
    protected Move chooseMove(LivingEntity target) {
        double reach = this.distanceTo(target) - this.getBbWidth() / 2 - target.getBbWidth() / 2;
        if (reach > REACH * 0.8) {
            return null;
        }
        if (hasShield()) {
            return ready(ATTACK) ? ATTACK : null;
        }
        return ready(SHIELDLESS_ATTACK) ? SHIELDLESS_ATTACK : null;
    }

    @Override
    protected void onMoveEnd(Move finished) {
        if (finished == SHIELDLESS_ATTACK && this.getTarget() != null && ready(COMBO)) {
            startMove(COMBO);
        }
    }

    /** Montage notifies: AM_RoyalGuard_BasicAttack, _ShieldLoss, _Shieldless_BasicAttack, _Shieldless_ComboAttack. */
    @Override
    protected void onMoveTick(Move current, int t) {
        if (current == ATTACK || current == SHIELDLESS_ATTACK) {
            if (t == 11) {
                sound(IllagerSound.ROYAL_GUARD_ATTACK, 1.0F);
            }
            if (t == 15) {
                Vfx.play(this, Vfx.Effect.ROYAL_SLASH, this.position().add(0, current == ATTACK ? 1.0 : 0.7, 0), this.getYRot(), 1);
            }
            if (t >= 15 && t <= 16) {
                hitWindow(inFront(1.0), 1.1, current == ATTACK ? 8 : 9, 0, this::maceHit);
            }
        } else if (current == COMBO) {
            if (t == 8) {
                sound(IllagerSound.ROYAL_GUARD_COMBO_1_VOCAL, 1.0F);
            }
            if (t == 10) {
                Vfx.play(this, Vfx.Effect.ROYAL_SLASH, this.position().add(0, 0.7, 0), this.getYRot(), -1);
                sound(IllagerSound.ROYAL_GUARD_COMBO_1, 1.0F);
            }
            if (t >= 10 && t <= 14) {
                hitWindow(inFront(1.0), REACH * 0.6, 9, 0, this::maceHit);
            }
            if (t == 27) {
                sound(IllagerSound.ROYAL_GUARD_COMBO_2_VOCAL, 1.0F);
            }
            if (t == 29) {
                this.struck.clear();
            }
            if (t == 30) {
                Vfx.play(this, Vfx.Effect.ROYAL_SLAM, inFront(1.2), this.getYRot(), 0);
                sound(IllagerSound.ROYAL_GUARD_COMBO_2, 1.2F);
            }
            if (t >= 30 && t <= 33) {
                hitWindow(inFront(1.2), REACH * 0.6, 17, 1500, this::maceHit);
            }
        } else if (current == SHIELD_LOSS) {
            if (t == 0) {
                Vfx.play(this, Vfx.Effect.SHIELD_BREAK, inFront(0.3).add(side(true).scale(0.3)).add(0, 1.0, 0), this.getYRot(), 0);
                sound(IllagerSound.ROYAL_GUARD_HURT, 1.0F);
            }
            if (t == 1) {
                sound(IllagerSound.ROYAL_GUARD_SHIELD_BREAK, 1.2F);
            }
            if (t == 5) {
                this.entityData.set(HAS_SHIELD, false); // ShieldBroken notify, 0.27 s
            }
        }
    }

    private void maceHit(LivingEntity victim) {
        soundAt(IllagerSound.ROYAL_GUARD_MACE_HIT, victim.position(), 1.0F);
    }

    // ---- shield ----------------------------------------------------------------------------------

    /** ForwardProtection: anything hitting him from the front half lands on the shield while it holds. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || !hasShield() || this.move == SHIELD_LOSS || amount <= 0
                || source.is(DamageTypeTags.BYPASSES_SHIELD) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        Vec3 from = source.getSourcePosition();
        if (from == null || Vec3.directionFromRotation(0, this.getYRot()).dot(from.subtract(this.position()).multiply(1, 0, 1)) <= 0) {
            return super.hurt(source, amount);
        }
        this.shield -= amount;
        this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        if (this.shield <= 0) {
            cancelMove();
            startMove(SHIELD_LOSS);
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasShield", hasShield());
        tag.putFloat("Shield", this.shield);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HasShield")) {
            this.entityData.set(HAS_SHIELD, tag.getBoolean("HasShield"));
            this.shield = tag.getFloat("Shield");
        }
    }

    @Override
    protected RawAnimation baseAnimation(AnimationState<MoveMob> state) {
        if (hasShield()) {
            return super.baseAnimation(state);
        }
        return state.isMoving() ? SHIELDLESS_WALK : SHIELDLESS_IDLE;
    }

    // ---- sounds ----------------------------------------------------------------------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return IllagerSound.ROYAL_GUARD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return IllagerSound.ROYAL_GUARD_DEATH.get();
    }

    /** Footsteps come from the walk animations' foot plants: the ground plus his armour. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void onSoundKeyframe(String sound) {
        if ("step".equals(sound)) {
            blockStep();
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), IllagerSound.ROYAL_GUARD_STEP.get(),
                    this.getSoundSource(), 0.6F, 1.0F, false);
        }
    }
}
