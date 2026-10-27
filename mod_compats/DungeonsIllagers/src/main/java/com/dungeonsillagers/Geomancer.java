package com.dungeonsillagers;

import java.util.ArrayList;
import com.dungeonsillagers.client.VfxLibrary;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * The Geomancer (DT_MobType1Definition Geomancer, ST_Geomancer): keeps 6 blocks from its target, pokes and backs off when
 * crowded, and alternates bomb pillars (70 %) and stone-pillar walls laid out in the game's GI_PillarPattern images
 * around the target. Health and damage follow the game 1:1 for illagers (Vindicator 25 -> 24).
 */
public class Geomancer extends MoveMob {

    private static final String NAME = "geomancer";
    static final Move ATTACK = Move.of(NAME, "attack", 33);
    static final Move BOMB_PILLARS = Move.of(NAME, "bomb_pillars", 33);
    static final Move WALL_PILLARS = Move.of(NAME, "wall_pillars", 33);
    static final Move SPAWN = Move.of(NAME, "spawn", 3);
    static final Move STAGGERED = Move.of(NAME, "staggered", 38);
    private static final List<Move> MOVES = List.of(ATTACK, BOMB_PILLARS, WALL_PILLARS, SPAWN, STAGGERED);

    /** GA_EngageTargetMovement AcceptanceRadiusOverride 600; GA_Retreat range 300, distance 600, cooldown 3 s. */
    private static final double KEEP_RANGE = 600 * UE, CROWDED = 300 * UE;
    private static final int RETREAT_COOLDOWN = 60;
    /** How long a retreat runs before the Geomancer may stand still again. */
    private static final int RETREAT_TICKS = 30;
    /** Pillar abilities: TargetDistanceMin/Max 200/1500. */
    private static final double PILLAR_MIN = 200 * UE, PILLAR_MAX = 1500 * UE;
    /** After a wall the state tree runs Chase (CloseTimeOutDuration 3 s) before casting again. */
    private static final int CHASE_AFTER_WALL = 60;

    // GI_PillarPattern_* (white/coloured pixel = one pillar, one block apart, centred on the target)
    private static final String[][] BOMB_PATTERNS = {
        {".....", ".....", "#....", ".....", "..#.."}, // DUO
        {"#...#", ".....", ".....", ".....", "#...#"}, // Quad_Corners
        {"..#..", ".....", "#...#", ".....", "..#.."}, // QUAD_M
    };
    private static final String[][] WALL_PATTERNS = {
        {"......#", "......#", "......#", "......#", "......#", "......#", "......#"}, // I
        {"....#", "....#", "....#", "....#", "#####"}, // L
        {"..#.....", "......#.", "...#....", "#.....#.", "...#....", ".#...#..", ".......#", "..#....."}, // Scattered
        {".####", "....#", "....#", "....#", ".####"}, // U_SMALL
        {"#####", "#...#", "....#", "....#", "#..##"}, // CAGE
        {"..#..", "..#..", "##.##", "..#..", "..#.."}, // CROSS
        {"#####", ".....", ".....", ".....", "#####"}, // LINES
    };

    /** AnimNS_Geomancer_GlowingBeads: what the beads shine with right now. */
    public enum Beads { MATTE, BOMB, WALL }
    private static final EntityDataAccessor<Byte> BEADS = SynchedEntityData.defineId(Geomancer.class, EntityDataSerializers.BYTE);

    private int retreatReadyAt;
    private int retreatUntil;
    private int castReadyAt;
    /** Pillar positions still to raise this move, one every 2 ticks (SpawnInterval 0.1 s). */
    private final List<Vec3> pending = new ArrayList<>();

    public Geomancer(EntityType<? extends Monster> type, Level level) {
        super(type, level, NAME, true);
        this.xpReward = 15;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BEADS, (byte) Beads.MATTE.ordinal());
    }

    public Beads beads() {
        return Beads.values()[this.entityData.get(BEADS)];
    }

    private void setBeads(Beads value) {
        this.entityData.set(BEADS, (byte) value.ordinal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.315)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractIllager.class, MoveMob.class, PrimeEnchanter.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
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

    @Override
    protected int cooldown(Move finished) {
        if (finished == ATTACK) {
            return 10;
        }
        return finished == BOMB_PILLARS || finished == WALL_PILLARS ? 60 : 0;
    }

    /**
     * ST_Geomancer, Aggressive, in order: PlayerTooClose (punch, then 25 % ExplosionPillars or Retreat), ExplosionPillars
     * (70 %) then WallPillars, WallPillars, Chase. Ranges from the abilities' TargetDistanceMax/Min.
     */
    @Override
    @Nullable
    protected Move chooseMove(LivingEntity target) {
        double reach = this.distanceTo(target) - this.getBbWidth() / 2;
        if (reach <= 100 * UE + target.getBbWidth() && ready(ATTACK)) {
            return ATTACK;
        }
        if (reach < PILLAR_MIN || reach > PILLAR_MAX || this.tickCount < this.castReadyAt || !this.hasLineOfSight(target)) {
            return null;
        }
        if (ready(BOMB_PILLARS) && this.random.nextFloat() < 0.7F) {
            return BOMB_PILLARS;
        }
        return ready(WALL_PILLARS) ? WALL_PILLARS : null;
    }

    @Override
    protected void onMoveEnd(Move finished) {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        if (finished == ATTACK && this.random.nextFloat() < 0.25F && ready(BOMB_PILLARS)) {
            startMove(BOMB_PILLARS);
        } else if (finished == BOMB_PILLARS && ready(WALL_PILLARS)) {
            startMove(WALL_PILLARS);
        } else if (finished == WALL_PILLARS) {
            this.castReadyAt = this.tickCount + CHASE_AFTER_WALL;
        }
    }

    /** Chase to 6 blocks; GA_Retreat (6 blocks away, 10 % faster) when the target is within 3. */
    @Override
    protected void tickMovement(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = this.distanceTo(target);
        if (dist <= CROWDED && this.tickCount >= this.retreatReadyAt) {
            Vec3 away = DefaultRandomPos.getPosAway(this, 8, 4, target.position());
            if (away != null) {
                this.getNavigation().moveTo(away.x, away.y, away.z, 1.1);
                this.retreatReadyAt = this.tickCount + RETREAT_COOLDOWN;
                this.retreatUntil = this.tickCount + RETREAT_TICKS;
            }
        } else if (dist > KEEP_RANGE) {
            chase(target);
        } else if (this.tickCount >= this.retreatUntil) {
            this.getNavigation().stop();
        }
    }

    /** Montage notifies: AM_Geomancer_BasicAttack, _Summon_BlastPillar, _Summon_WallPillar, _Spawn, _Staggered. */
    @Override
    protected void onMoveTick(Move current, int t) {
        if (current == ATTACK) {
            if (t == 0) {
                sound(IllagerSound.GEOMANCER_PRE_ATTACK, 1.0F);
            }
            if (t >= 6 && t <= 8) {
                hitWindow(inFront(1.25), 1.25, 8, 0, null);
            }
        } else if (current == BOMB_PILLARS) {
            if (t == 0) {
                this.pending.clear();
                LivingEntity target = this.getTarget();
                if (target != null) {
                    this.pending.addAll(layout(BOMB_PATTERNS[this.random.nextInt(BOMB_PATTERNS.length)], target));
                }
            }
            if (t == 1) {
                setBeads(Beads.BOMB);
                Vfx.play(this, Vfx.Effect.GEO_BEADS, Vfx.Anchor.BODY, 0);
            }
            if (t == 11) {
                setBeads(Beads.MATTE);
            }
            if (t == 4) {
                vfx(Vfx.Effect.GEO_CAST_BOMB, this.position(), 0);
            }
            if (t == 6) {
                sound(IllagerSound.GEOMANCER_SPELL_BOMB, 1.5F);
            }
            if (t == 10) {
                vfx(Vfx.Effect.GEO_SUMMON_DUST, inFront(0.8), 0);
            }
            if (t >= 10 && t % 2 == 0) {
                raiseNext(true);
            }
        } else if (current == WALL_PILLARS) {
            if (t == 0) {
                sound(IllagerSound.GEOMANCER_SPELL_WALL, 1.5F);
                this.pending.clear();
                LivingEntity target = this.getTarget();
                if (target != null) {
                    this.pending.addAll(layout(WALL_PATTERNS[this.random.nextInt(WALL_PATTERNS.length)], target));
                }
            }
            if (t == 1) {
                vfx(Vfx.Effect.GEO_CAST_WALL, this.position(), 0);
            }
            if (t == 3) {
                setBeads(Beads.WALL);
                Vfx.play(this, Vfx.Effect.GEO_BEADS, Vfx.Anchor.BODY, 1);
            }
            if (t == 11) {
                setBeads(Beads.MATTE);
            }
            if (t == 3) {
                sound(IllagerSound.GEOMANCER_WALL_SPAWN, 1.5F);
            }
            if (t >= 3 && t % 2 == 1) {
                raiseNext(false);
            }
        } else if (current == SPAWN) {
            if (t == 0) {
                sound(IllagerSound.GEOMANCER_SPAWN, 1.0F);
                sound(IllagerSound.GEOMANCER_CANE, 1.0F);
                vfx(Vfx.Effect.MOB_SPAWN, this.position(), 0);
            }
        } else if (current == STAGGERED) {
            if (t == 1) {
                sound(IllagerSound.GEOMANCER_STUNNED_START, 1.0F);
            }
            if (t == 31) {
                sound(IllagerSound.GEOMANCER_STUNNED_END, 1.0F);
            }
        }
    }

    @Override
    protected void onStaggered() {
        this.pending.clear();
        setBeads(Beads.MATTE);
    }

    private void vfx(Vfx.Effect effect, Vec3 at, float param) {
        Vfx.play(this, effect, at, this.getYRot(), param);
    }

    /**
     * Pattern cells as ground positions, one block apart, centred on the target and turned to face the Geomancer (the
     * images are drawn for the game's fixed camera).
     */
    private List<Vec3> layout(String[] pattern, LivingEntity target) {
        double yaw = Math.round(Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) / (Math.PI / 2)) * (Math.PI / 2);
        double cos = Math.cos(yaw), sin = Math.sin(yaw);
        double cx = (pattern[0].length() - 1) / 2.0, cy = (pattern.length - 1) / 2.0;
        List<Vec3> cells = new ArrayList<>();
        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length(); col++) {
                if (pattern[row].charAt(col) == '.') {
                    continue;
                }
                double fwd = cy - row, right = col - cx;
                double x = target.getX() + fwd * cos - right * sin;
                double z = target.getZ() + fwd * sin + right * cos;
                Vec3 at = new Vec3(Math.floor(x) + 0.5, target.getY(), Math.floor(z) + 0.5);
                cells.add(new Vec3(at.x, groundAt(at), at.z));
            }
        }
        return cells;
    }

    /** Raises the next pending pillar unless a creature stands there (CollisionHandlingMethod DontSpawnIfColliding). */
    private void raiseNext(boolean bomb) {
        if (this.pending.isEmpty() || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 at = this.pending.remove(0);
        EntityType<? extends Prop> type = bomb ? DungeonsIllagersMod.BOMB_PILLAR.get() : DungeonsIllagersMod.WALL_PILLAR.get();
        AABB space = type.getDimensions().makeBoundingBox(at);
        if (!server.getEntitiesOfClass(LivingEntity.class, space, Entity::isAlive).isEmpty()
                || !server.noCollision(space.deflate(0.05))) {
            return;
        }
        Prop pillar = type.create(server);
        if (pillar == null) {
            return;
        }
        pillar.moveTo(at.x, at.y, at.z, this.random.nextInt(4) * 90.0F, 0);
        pillar.setOwner(this);
        server.addFreshEntity(pillar);
        if (!bomb) {
            server.playSound(null, at.x, at.y, at.z, IllagerSound.GEOMANCER_PILLAR_APPEAR.get(), this.getSoundSource(), 0.8F, 1.0F);
        }
    }

    // ---- sounds ----------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return IllagerSound.GEOMANCER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return IllagerSound.GEOMANCER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return IllagerSound.GEOMANCER_DEATH.get();
    }

    /** Footsteps come from the walk animation's foot plants. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void onSoundKeyframe(String sound) {
        if ("step".equals(sound)) {
            blockStep();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VfxLibrary.stepDust(this));
        }
    }
}
