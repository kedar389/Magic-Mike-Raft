package com.dungeonsillagers;

import com.dungeonsillagers.client.VfxLibrary;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * The Excavator (DT_MobType1Definition DeepDarkIllager): a heavy melee miner. Pickaxe jabs (LightAttack, 7 damage,
 * 100 UE) and, every 4 s, an overhead smash (HeavyAttack, 12 damage within 200 UE plus 5 splash within 400 UE). Runs
 * at its target, fidgets when idle, and half of them carry a backpack (DeepDarkIllagerBackpack, weight 0.5).
 */
public class Excavator extends MoveMob {

    private static final String NAME = "excavator";
    static final Move LIGHT_ATTACK = Move.of(NAME, "light_attack", 26);
    static final Move HEAVY_ATTACK = Move.of(NAME, "heavy_attack", 39);
    static final Move SPAWN = Move.of(NAME, "spawn", 3);
    static final Move STAGGERED = Move.of(NAME, "staggered", 29);
    static final Move BACK_SCRATCH = Move.of(NAME, "back_scratch", 124);
    static final Move HEAD_SCRATCH = Move.of(NAME, "head_scratch", 103);
    static final Move LOOKING = Move.of(NAME, "looking", 109);
    static final Move SLAP = Move.of(NAME, "slap", 100);
    private static final List<Move> FIDGETS = List.of(BACK_SCRATCH, HEAD_SCRATCH, LOOKING, SLAP);
    private static final List<Move> MOVES =
            List.of(LIGHT_ATTACK, HEAVY_ATTACK, SPAWN, STAGGERED, BACK_SCRATCH, HEAD_SCRATCH, LOOKING, SLAP);
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.excavator.run");
    private static final EntityDataAccessor<Boolean> BACKPACK = SynchedEntityData.defineId(Excavator.class, EntityDataSerializers.BOOLEAN);
    /** Heavy smash: centred 175 UE ahead (AnimNS_GroundSlam). */
    private static final double SLAM_AHEAD = 175 * UE;

    public Excavator(EntityType<? extends Monster> type, Level level) {
        super(type, level, NAME, true);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
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
        this.entityData.define(BACKPACK, false);
    }

    public boolean hasBackpack() {
        return this.entityData.get(BACKPACK);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.entityData.set(BACKPACK, this.random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Backpack", hasBackpack());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BACKPACK, tag.getBoolean("Backpack"));
    }

    @Override
    protected List<Move> moves() {
        return MOVES;
    }

    @Override
    @Nullable
    protected Move spawnMove() {
        return SPAWN;
    }

    @Override
    protected Move staggerMove() {
        return STAGGERED;
    }

    @Override
    protected float[] staggerAt() {
        return new float[] {0.5F};
    }

    /** HeavyAttack CooldownDuration 4 s; the light jab only waits out its own animation. */
    @Override
    protected int cooldown(Move finished) {
        return finished == HEAVY_ATTACK ? 80 : 0;
    }

    /** Heavy smash whenever it is ready and the target within 220 UE, else the jab at 100 UE. */
    @Override
    @Nullable
    protected Move chooseMove(LivingEntity target) {
        double reach = this.distanceTo(target) - this.getBbWidth() / 2 - target.getBbWidth() / 2;
        if (ready(HEAVY_ATTACK) && reach <= 220 * UE) {
            return HEAVY_ATTACK;
        }
        return reach <= 100 * UE + 0.3 ? LIGHT_ATTACK : null;
    }

    /** Fidgets: now and then while idle with nothing to fight; a target cuts them short. */
    @Override
    protected void tickMovement(@Nullable LivingEntity target) {
        if (target == null && !this.isBusy() && this.getNavigation().isDone() && this.random.nextInt(400) == 0) {
            startMove(FIDGETS.get(this.random.nextInt(FIDGETS.size())));
        }
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.move != null && FIDGETS.contains(this.move) && this.getTarget() != null) {
            cancelMove();
        }
        super.tick();
        // NS_DeepDarkIllager_CandleFire: the candle on his cap
        if (this.level().isClientSide() && this.isAlive()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VfxLibrary.candle(this, this.tickCount));
        }
    }

    /** Montage notifies: AM_DeepDark_Illager_BasicAttack, _HeavyAttack, _Spawn, _Staggered. */
    @Override
    protected void onMoveTick(Move current, int t) {
        if (current == LIGHT_ATTACK) {
            if (t == 7) {
                sound(IllagerSound.EXCAVATOR_PICK_SWING, 1.0F);
            }
            if (t == 9) {
                Vfx.play(this, Vfx.Effect.EXC_SLASH, this.position().add(0, 1.0, 0), this.getYRot(), 1);
            }
            if (t >= 10 && t <= 11) {
                hitWindow(inFront(0.9), 1.0, 7, 0, victim -> soundAtVictim(victim, IllagerSound.EXCAVATOR_PICKAXE_IMPACT));
            }
        } else if (current == HEAVY_ATTACK) {
            Vec3 slam = inFront(SLAM_AHEAD);
            if (t == 2) {
                sound(IllagerSound.EXCAVATOR_PICK_BREAK_TELEGRAPH, 1.2F);
            }
            if (t == 17) {
                sound(IllagerSound.EXCAVATOR_PICK_BREAK_HIT, 1.5F);
            }
            if (t == 18) {
                Vfx.play(this, Vfx.Effect.EXC_SLASH, this.position().add(0, 0.6, 0), this.getYRot(), -1);
                Vfx.play(this, Vfx.Effect.EXC_SMASH, slam, this.getYRot(), 0);
            }
            if (t == 19) {
                hitWindow(slam, 200 * UE, 12, 0, victim -> soundAtVictim(victim, IllagerSound.EXCAVATOR_PICKAXE_IMPACT));
                hitWindow(slam, 400 * UE, 5, 0, null);
            }
        } else if (current == SPAWN) {
            if (t == 0) {
                sound(IllagerSound.EXCAVATOR_SPAWN, 1.0F);
                Vfx.play(this, Vfx.Effect.MOB_SPAWN, this.position(), this.getYRot(), 0);
            }
        } else if (current == STAGGERED) {
            if (t == 0) {
                sound(IllagerSound.EXCAVATOR_HURT, 1.0F);
            }
        }
    }

    private void soundAtVictim(LivingEntity victim, IllagerSound sound) {
        soundAt(sound, victim.position(), 1.0F);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.getTarget() == null && !this.level().isClientSide()) {
            sound(IllagerSound.EXCAVATOR_AGGRO, 1.0F);
        }
        super.setTarget(target);
    }

    @Override
    protected RawAnimation baseAnimation(AnimationState<MoveMob> state) {
        return state.isMoving() && this.isAggressive() ? RUN : super.baseAnimation(state);
    }

    // ---- sounds ----------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return IllagerSound.EXCAVATOR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return IllagerSound.EXCAVATOR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return IllagerSound.EXCAVATOR_DEATH.get();
    }

    /** Footsteps come from the walk and run animations' foot plants. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void onSoundKeyframe(String sound) {
        IllagerSound local = switch (sound) {
            case "foley" -> IllagerSound.EXCAVATOR_STEP_FOLEY;
            case "back_scratch" -> IllagerSound.EXCAVATOR_BACK_SCRATCH;
            case "head_scratch" -> IllagerSound.EXCAVATOR_HEAD_SCRATCH;
            case "slap" -> IllagerSound.EXCAVATOR_SLAP;
            default -> null;
        };
        if ("step".equals(sound)) {
            blockStep();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VfxLibrary.stepDust(this));
        } else if (local != null) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), local.get(), this.getSoundSource(), 0.8F, 1.0F, false);
        }
    }
}
