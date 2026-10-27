package com.primeenchanter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The Prime Enchanter. Every attack is a {@link Move}: the server triggers its animation, roots the boss for the
 * animation's length, and fires the move's effects on the ticks of the Dungeons II montage notifies
 * (montage seconds / rate scale x 20). Damage is scaled so the game's 25 equals our attack damage; radii are the
 * game's UE units at the model's 1.4 render scale. Sneak + right-click with an empty hand in creative plays the next
 * move, to preview them all.
 */
public class PrimeEnchanter extends Monster implements GeoEntity {

    private static final String CONTROLLER = "main";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.prime_enchanter.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.prime_enchanter.walk");
    private static final int DEATH_TICKS = 113;
    /** Pause between two moves, so one never starts on the tick the last one ended. */
    private static final int MOVE_GAP = 10;
    private static final float GAME_DAMAGE_UNIT = 25.0F;
    private static final double UE_TO_BLOCKS = 1.4 / 100.0;
    /** IceMine GA_TimedAOE Time = 2 s. */
    private static final int MINE_FUSE = 40;
    /** Forward Strike trail (GA_ActorTrail): 3 s, at most 15 mines, one per 8 UE units moved. */
    private static final int TRAIL_TICKS = 60;
    /** Overcharge lightning floor: 5 s, radius in blocks, and how far above the floor is safe. */
    private static final int FLOOR_TICKS = 100;
    private static final double FLOOR_RADIUS = 10.0;
    private static final double FLOOR_SAFE_HEIGHT = 2.0;
    private static final int TRAIL_MAX_MINES = 15;
    private static final double TRAIL_STEP = 8 * 1.4 / 100.0;
    private static final float MAX_TURN_PER_TICK = 25.0F;
    private static final ResourceLocation ROYAL_GUARD = new ResourceLocation("takesapillage", "legioner");
    /** Ticks without a target before the gauntlets go dark. */
    private static final int GAUNTLETS_IDLE_OFF = 100;
    private static final EntityDataAccessor<Boolean> GAUNTLETS_LIT =
            SynchedEntityData.defineId(PrimeEnchanter.class, EntityDataSerializers.BOOLEAN);

    /** One animation-driven action. Lengths match the animation file, in ticks. */
    enum Move {
        FURY_FIST("fury_fist", 138),
        FORWARD_STRIKE("forward_strike", 50),
        LIGHTNING_SLAM("lightning_slam", 88),
        LAUNCH("launch", 79),
        OVERCHARGE("overcharge", 72),
        SUMMON("summon", 45),
        ICEMINES("icemines", 45),
        SPAWN("spawn", 47),
        STAGGERED("staggered", 45),
        DEATH("death", DEATH_TICKS);

        final String id;
        final int length;
        final RawAnimation animation;

        Move(String id, int length) {
            this.id = id;
            this.length = length;
            String name = "animation.prime_enchanter." + id;
            this.animation = id.equals("death")
                    ? RawAnimation.begin().thenPlayAndHold(name)
                    : RawAnimation.begin().thenPlay(name);
        }
    }

    private enum Element { NONE, ICE, LIGHTNING }

    /** Ability cooldowns from DT_MobType3Definition, in ticks; they start when the move ends. */
    private static final Map<Move, Integer> COOLDOWNS = Map.of(
            Move.FORWARD_STRIKE, 26, Move.FURY_FIST, 70, Move.LIGHTNING_SLAM, 190,
            Move.OVERCHARGE, 360, Move.SUMMON, 800, Move.LAUNCH, 120);
    private static final Move[] PREVIEW = {
        Move.FURY_FIST, Move.FORWARD_STRIKE, Move.LIGHTNING_SLAM, Move.LAUNCH, Move.OVERCHARGE,
        Move.SUMMON, Move.ICEMINES, Move.SPAWN, Move.STAGGERED,
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar =
            new ServerBossEvent(Component.translatable("entity.prime_enchanter.prime_enchanter"),
                    BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    /** Targets already struck by the current hit window; each window hits a target once. */
    private final Set<UUID> struck = new HashSet<>();
    /** Ice mines waiting to explode: position and remaining fuse ticks. */
    private final List<Mine> mines = new ArrayList<>();

    @Nullable
    private Move move;
    private int moveTick;
    private int gap = MOVE_GAP;
    /** Tick count at which each move may be chosen again. */
    private final Map<Move, Integer> readyAt = new EnumMap<>(Move.class);
    /** Move chained after the current one: Overcharge after Lightning Slam, Launch after Overcharge. */
    @Nullable
    private Move chained;
    private int previewIndex;
    private boolean spawnPending;
    /** How many of the {@link #STAGGER_AT} health thresholds have already staggered the boss. */
    private int staggersDone;
    /** The last summoned wave; Summon waits until every one of them is dead or gone. */
    private final List<UUID> wave = new ArrayList<>();
    private int ticksWithoutTarget;
    @Nullable
    private Vec3 trailFrom;
    private int trailMines;
    @Nullable
    private Vec3 floorCenter;
    private int floorTicks;

    /** Health fractions that stagger the boss once each, interrupting whatever it is doing. */
    private static final float[] STAGGER_AT = {0.75F, 0.5F, 0.25F};

    private static final class Mine {
        final Vec3 pos;
        int fuse = MINE_FUSE;

        Mine(Vec3 pos) {
            this.pos = pos;
        }
    }

    public PrimeEnchanter(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractIllager.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    // ---- moves ---------------------------------------------------------------------------------

    private void startMove(Move next) {
        if (this.level().isClientSide()) {
            return;
        }
        this.move = next;
        this.moveTick = 0;
        this.chained = null;
        this.struck.clear();
        this.getNavigation().stop();
        this.triggerAnim(CONTROLLER, next.id);
    }

    private boolean isBusy() {
        return this.move != null;
    }

    /** Launch (mid-air) and Overcharge carry the game's Invulnerable tag for part of the move. */
    private boolean isInvulnerableNow() {
        return (this.move == Move.LAUNCH && this.moveTick >= 22 && this.moveTick <= 44)
                || (this.move == Move.OVERCHARGE && this.moveTick >= 2);
    }

    /** No plain melee hits: MeleeAttackGoal only chases, every attack is a move picked by {@link #chooseMove}. */
    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    private boolean ready(Move candidate) {
        return this.tickCount >= this.readyAt.getOrDefault(candidate, 0);
    }

    /**
     * ST_FrostbiteDesertBoss, Aggressive branch, tried in order: Lightning Slam (HP <= 67 %), Overcharge (HP <= 50 %),
     * Forward Strike, Fury Fist, Summon Illagers (HP <= 98 %), Launch; otherwise keep chasing. Ranges are the abilities'
     * TargetDistanceMax/Min in UE units at the 1.4 render scale, measured from the boss's edge.
     */
    @Nullable
    private Move chooseMove(LivingEntity target) {
        float health = this.getHealth() / this.getMaxHealth();
        double reach = this.distanceTo(target) - this.getBbWidth() / 2;
        if (health <= 0.67F && ready(Move.LIGHTNING_SLAM) && reach <= 400 * UE_TO_BLOCKS) {
            return Move.LIGHTNING_SLAM;
        }
        if (health <= 0.5F && ready(Move.OVERCHARGE)) {
            return Move.OVERCHARGE;
        }
        if (ready(Move.FORWARD_STRIKE) && reach <= 250 * UE_TO_BLOCKS) {
            return Move.FORWARD_STRIKE;
        }
        if (ready(Move.FURY_FIST) && reach <= 150 * UE_TO_BLOCKS) {
            return Move.FURY_FIST;
        }
        if (health <= 0.98F && ready(Move.SUMMON) && !waveAlive()) {
            return Move.SUMMON;
        }
        if (ready(Move.LAUNCH) && reach >= 500 * UE_TO_BLOCKS && reach <= 3000 * UE_TO_BLOCKS && this.hasLineOfSight(target)) {
            return Move.LAUNCH;
        }
        return null;
    }

    private boolean waveAlive() {
        if (this.level() instanceof ServerLevel server) {
            this.wave.removeIf(id -> !(server.getEntity(id) instanceof LivingEntity minion) || !minion.isAlive());
        }
        return !this.wave.isEmpty();
    }

    private void endMove(Move finished) {
        Integer cooldown = COOLDOWNS.get(finished);
        if (cooldown != null) {
            this.readyAt.put(finished, this.tickCount + cooldown);
        }
        this.move = null;
        this.gap = MOVE_GAP;
        Move next = this.chained;
        this.chained = null;
        if (finished == Move.LIGHTNING_SLAM && this.getHealth() <= this.getMaxHealth() * 0.5F && ready(Move.OVERCHARGE)) {
            next = Move.OVERCHARGE;
        } else if (finished == Move.OVERCHARGE) {
            next = Move.LAUNCH; // EmergencyLaunch
        }
        if (next != null && this.getTarget() != null && this.isAlive()) {
            startMove(next);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        tickMines();
        tickFloor();
        tickGauntlets();
        if (this.spawnPending && this.tickCount >= 2) {
            this.spawnPending = false;
            startMove(Move.SPAWN);
        }
        if (this.move != null && this.move != Move.DEATH) {
            this.getNavigation().stop();
            faceTarget();
            onMoveTick(this.move, this.moveTick);
            if (++this.moveTick >= this.move.length) {
                endMove(this.move);
            }
            return;
        }
        if (this.gap > 0) {
            this.gap--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null) {
            Move next = chooseMove(target);
            if (next != null) {
                startMove(next);
            }
        }
    }

    /** Effects keyed to the montage notifies (ticks = montage seconds / rate x 20). */
    private void onMoveTick(Move current, int t) {
        switch (current) {
            case FURY_FIST -> {
                // rate 1.3: two jabs, power-up, ice and lightning hooks, dash, double-fist ground slam
                if (t == 3) {
                    sound(BossSound.FURY_FIST_TELEGRAPH);
                }
                if (t == 14 || t == 25 || t == 73 || t == 88) {
                    lunge(0.6);
                }
                if (t == 13) {
                    sound(BossSound.FURY_FIST_HIT_1);
                }
                if (t == 25) {
                    sound(BossSound.FURY_FIST_HIT_2);
                }
                if (t == 72) {
                    sound(BossSound.FURY_FIST_HIT_3);
                    sound(BossSound.FURY_FIST_HIT_3_VOCAL);
                }
                if (t == 80) {
                    sound(BossSound.FURY_FIST_HIT_4);
                    sound(BossSound.FURY_FIST_HIT_4_VOCAL);
                }
                if (t == 94) {
                    sound(BossSound.FURY_FIST_HIT_5);
                    sound(BossSound.FURY_FIST_HIT_5_VOCAL);
                }
                if (t >= 13 && t <= 16) {
                    hitWindow(armStrike(false), 1.6, 12, 1500, gauntletsLit() ? Element.ICE : Element.NONE);
                }
                if (t >= 25 && t <= 28) {
                    hitWindow(armStrike(true), 1.6, 12, 1500, gauntletsLit() ? Element.LIGHTNING : Element.NONE);
                }
                if (t == 26) {
                    this.struck.clear();
                }
                if (t == 15) {
                    burst(ParticleTypes.SNOWFLAKE, armStrike(false), 12, 0.3);
                }
                if (t == 27) {
                    burst(ParticleTypes.ELECTRIC_SPARK, armStrike(true), 12, 0.3);
                }
                if (t == 31) {
                    sound(BossSound.FURY_FIST_POWER_UP);
                    sound(BossSound.FURY_FIST_POWER_UP_VOCAL);
                }
                if (t == 41) {
                    burst(ParticleTypes.SNOWFLAKE, handPosition(false), 30, 0.4);
                    burst(ParticleTypes.ELECTRIC_SPARK, handPosition(true), 30, 0.4);
                }
                if (t == 44) {
                    burst(ParticleTypes.ELECTRIC_SPARK, this.position().add(0, 1.5, 0), 60, 1.2);
                }
                if (t == 72) {
                    this.struck.clear();
                }
                if (t >= 73 && t <= 78) {
                    hitWindow(armStrike(false), 1.6, 15, 2000, Element.ICE);
                }
                if (t == 74) {
                    burst(ParticleTypes.SNOWFLAKE, armStrike(false), 40, 0.8);
                }
                if (t == 88) {
                    this.struck.clear();
                }
                if (t >= 89 && t <= 93) {
                    hitWindow(armStrike(true), 1.6, 15, 2000, Element.LIGHTNING);
                }
                if (t == 89) {
                    burst(ParticleTypes.ELECTRIC_SPARK, armStrike(true), 40, 0.8);
                }
                if (t == 104) {
                    lunge(1.2);
                }
                if (t == 108) {
                    this.struck.clear();
                }
                if (t == 109) {
                    burst(ParticleTypes.SNOWFLAKE, inFront(1.3), 80, 2.0);
                    burst(ParticleTypes.ELECTRIC_SPARK, inFront(1.3), 60, 2.0);
                }
                if (t >= 109 && t <= 115) {
                    hitWindow(inFront(1.3), 200 * UE_TO_BLOCKS, 30, 2000, Element.ICE);
                }
            }
            case FORWARD_STRIKE -> {
                if (t == 0) {
                    this.trailFrom = this.position();
                    this.trailMines = 0;
                }
                if (t < TRAIL_TICKS) {
                    tickIceTrail();
                }
                if (t == 3) {
                    sound(BossSound.FORWARD_STRIKE_TELEGRAPH);
                }
                if (t == 9) {
                    burst(ParticleTypes.SNOWFLAKE, handPosition(false), 25, 0.3);
                }
                if (t == 27) {
                    sound(BossSound.FORWARD_STRIKE_HIT);
                }
                if (t == 28) {
                    lunge(1.6);
                }
                if (t == 29) {
                    burst(ParticleTypes.SNOWFLAKE, inFront(1.0), 50, 1.0);
                }
                if (t >= 29 && t <= 40) {
                    hitWindow(armStrike(false), 1.8, 25, 4000, Element.ICE);
                }
            }
            case LIGHTNING_SLAM -> {
                if (t == 0) {
                    sound(BossSound.LIGHTNING_SLAM_START);
                    sound(BossSound.LIGHTNING_SLAM_START_VOCAL);
                }
                if (t >= 9 && t < 53) {
                    burst(ParticleTypes.ELECTRIC_SPARK, handPosition(true), 3, 0.3);
                }
                if (t == 26) {
                    lunge(1.0);
                    sound(BossSound.LIGHTNING_SLAM_JUMP);
                }
                if (t == 47) {
                    sound(BossSound.LIGHTNING_SLAM_END);
                    sound(BossSound.LIGHTNING_SLAM_END_VOCAL);
                }
                if (t == 53) {
                    Vec3 at = inFront(1.5);
                    bolt(at);
                    burst(ParticleTypes.ELECTRIC_SPARK, at, 120, 3.5);
                    sound(BossSound.LIGHTNING_SLAM_RIPPLE);
                }
                if (t >= 53 && t <= 65) {
                    hitWindow(inFront(1.5), 550 * UE_TO_BLOCKS, 35, 3500, Element.LIGHTNING);
                }
            }
            case LAUNCH -> {
                if (t == 0) {
                    sound(BossSound.LAUNCH_TELEPORT_OUT);
                    sound(BossSound.LAUNCH_TELEPORT_OUT_VOCAL);
                }
                if (t == 30 && this.getTarget() != null) {
                    Vec3 at = this.getTarget().position();
                    this.level().playSound(null, at.x, at.y, at.z, BossSound.LAUNCH_LAND_TELEGRAPH.get(),
                            this.getSoundSource(), 2.0F, 1.0F);
                }
                if (t == 8) {
                    burst(ParticleTypes.SNOWFLAKE, this.position(), 40, 1.0);
                    burst(ParticleTypes.ELECTRIC_SPARK, this.position(), 40, 1.0);
                }
                if (t == 40) {
                    if (this.getTarget() != null) {
                        Vec3 to = this.getTarget().position();
                        this.teleportTo(to.x, to.y, to.z);
                    }
                    sound(BossSound.LAUNCH_TELEPORT_IN);
                    sound(BossSound.LAUNCH_TELEPORT_IN_VOCAL);
                }
                if (t == 43) {
                    bolt(this.position());
                }
                if (t == 45) {
                    burst(ParticleTypes.SNOWFLAKE, this.position(), 120, 3.0);
                    burst(ParticleTypes.ELECTRIC_SPARK, this.position(), 80, 3.0);
                }
                if (t >= 43 && t <= 52) {
                    hitWindow(this.position(), 250 * UE_TO_BLOCKS, 25, 1000, Element.LIGHTNING);
                }
            }
            case OVERCHARGE -> {
                if (t == 0) {
                    sound(BossSound.OVERCHARGE_START);
                }
                if (t == 44) {
                    sound(BossSound.OVERCHARGE_LOOP);
                }
                if (t == 54) {
                    sound(BossSound.OVERCHARGE_END);
                }
                if (t > 3 && t < 54) {
                    burst(ParticleTypes.ELECTRIC_SPARK, handPosition(true), 4, 0.6);
                }
                if (t == 3) {
                    this.floorCenter = this.position();
                }
                if (t > 3 && t < 44 && t % 4 == 0) {
                    ring(ParticleTypes.ELECTRIC_SPARK, this.floorCenter, FLOOR_RADIUS);
                }
                if (t == 44) {
                    this.floorTicks = FLOOR_TICKS;
                }
            }
            case SUMMON -> {
                if (t == 1) {
                    sound(BossSound.SUMMON);
                }
                if (t == 11 && this.level() instanceof ServerLevel server) {
                    // SummonIllagers waves: 1-2 Pillagers, 2-3 Vindicators, 1-3 Royal Guards
                    summon(server, EntityType.PILLAGER, 1 + this.random.nextInt(2));
                    summon(server, EntityType.VINDICATOR, 2 + this.random.nextInt(2));
                    summon(server, royalGuard(), 1 + this.random.nextInt(3));
                }
            }
            case ICEMINES -> {
                // BP_IceMines: telegraph on spawn, explode 1.15 s later
                if (t == 1) {
                    sound(BossSound.SUMMON);
                }
                if (t == 11 && this.getTarget() != null) {
                    Vec3 center = this.getTarget().position();
                    placeMine(center);
                    for (int i = 0; i < 4; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2;
                        double dist = 2.0 + this.random.nextDouble() * 3.0;
                        placeMine(center.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist));
                    }
                }
            }
            case SPAWN -> {
                if (t == 0) {
                    sound(BossSound.LIGHTNING_SLAM_END);
                }
                if (t == 5) {
                    burst(ParticleTypes.SNOWFLAKE, this.position(), 80, 2.0);
                }
                if (t == 35) {
                    sound(BossSound.FURY_FIST_HIT_2);
                }
                if (t == 37) {
                    burst(ParticleTypes.ELECTRIC_SPARK, handPosition(true), 30, 0.4);
                }
                if (t == 39) {
                    burst(ParticleTypes.SNOWFLAKE, handPosition(false), 30, 0.4);
                }
            }
            case STAGGERED -> {
                if (t == 0) {
                    sound(BossSound.STAGGER_START);
                }
                if (t == 34) {
                    sound(BossSound.STAGGER_END);
                }
            }
            default -> { }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GAUNTLETS_LIT, false);
    }

    /** Both gauntlets enchanted: the model shows them regardless of the playing animation. */
    public boolean gauntletsLit() {
        return this.entityData.get(GAUNTLETS_LIT);
    }

    /**
     * The boss enchants its hands whenever it can: as soon as it has a target and is free to act. A stagger knocks the
     * enchantment out; losing the target for {@link #GAUNTLETS_IDLE_OFF} ticks lets it fade.
     */
    private void tickGauntlets() {
        boolean lit = gauntletsLit();
        this.ticksWithoutTarget = this.getTarget() == null ? this.ticksWithoutTarget + 1 : 0;
        if (lit && (this.move == Move.STAGGERED || this.move == Move.DEATH || this.ticksWithoutTarget > GAUNTLETS_IDLE_OFF)) {
            this.entityData.set(GAUNTLETS_LIT, false);
        } else if (!lit && this.getTarget() != null && this.isAlive() && !isBusy()) {
            this.entityData.set(GAUNTLETS_LIT, true);
            sound(BossSound.FURY_FIST_POWER_UP);
            burst(ParticleTypes.SNOWFLAKE, handPosition(false), 30, 0.4);
            burst(ParticleTypes.ELECTRIC_SPARK, handPosition(true), 30, 0.4);
        }
    }

    /**
     * Rooted moves skip vanilla body turning, so attacks keep aiming at the target the way the game's
     * AN_FocusOnTarget notifies do. Hit boxes and lunges use this facing.
     */
    private void faceTarget() {
        LivingEntity target = this.getTarget();
        if (target == null || this.move == Move.STAGGERED) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float yaw = Mth.approachDegrees(this.getYRot(), wanted, MAX_TURN_PER_TICK);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    /** IceMine (DT_MobPropActorDefinition): 8 damage in range 250, 5 splash in range 350, Freezing. */
    private void tickMines() {
        for (var it = this.mines.iterator(); it.hasNext(); ) {
            Mine mine = it.next();
            if (mine.fuse-- > 0) {
                if (mine.fuse % 4 == 0) {
                    ring(ParticleTypes.SNOWFLAKE, mine.pos, 1.0);
                }
                continue;
            }
            it.remove();
            this.level().playSound(null, mine.pos.x, mine.pos.y, mine.pos.z, BossSound.ICE_MINE_EXPLODE.get(),
                    this.getSoundSource(), 1.5F, 1.0F);
            burst(ParticleTypes.SNOWFLAKE, mine.pos, 50, 1.0);
            Set<UUID> hit = new HashSet<>();
            strike(mine.pos, 250 * UE_TO_BLOCKS, 8, 0, Element.ICE, hit);
            strike(mine.pos, 350 * UE_TO_BLOCKS, 5, 0, Element.ICE, hit);
        }
    }

    private void placeMine(Vec3 at) {
        this.mines.add(new Mine(at));
        this.level().playSound(null, at.x, at.y, at.z, BossSound.ICE_MINE_SPAWN.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    /**
     * Forward Strike's GA_ActorTrail: while the boss moves during the strike it drops an ice mine every tick, scattered
     * across a 300-unit-wide strip and snapped to the ground, at most {@link #TRAIL_MAX_MINES}.
     */
    private void tickIceTrail() {
        if (this.trailMines >= TRAIL_MAX_MINES || this.trailFrom == null
                || this.position().distanceToSqr(this.trailFrom) < TRAIL_STEP * TRAIL_STEP) {
            return;
        }
        this.trailFrom = this.position();
        double offset = (this.random.nextDouble() - 0.5) * 300 * UE_TO_BLOCKS;
        Vec3 at = this.position().add(side(false).scale(offset));
        BlockPos column = BlockPos.containing(at);
        double ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column).getY();
        placeMine(new Vec3(at.x, Math.abs(ground - this.getY()) < 3 ? ground : this.getY(), at.z));
        this.trailMines++;
    }

    /** Boss sounds carry far: volume 2.5 is audible to about 40 blocks. */
    private void sound(BossSound sound) {
        this.playSound(sound.get(), 2.5F, 1.0F);
    }

    private Vec3 inFront(double distance) {
        return this.position().add(Vec3.directionFromRotation(0, this.getYRot()).scale(distance));
    }

    private Vec3 side(boolean left) {
        return Vec3.directionFromRotation(0, this.getYRot() + (left ? -90 : 90));
    }

    /** Where a hand sits in the idle stance: lightning gauntlet on the left, ice gauntlet on the right. */
    private Vec3 handPosition(boolean left) {
        return this.position().add(side(left).scale(0.6)).add(0, 1.3, 0);
    }

    /** The arm-to-hand capsule of a punch, centred ahead of the boss on that arm's side. */
    private Vec3 armStrike(boolean left) {
        return inFront(2.0).add(side(left).scale(0.5)).add(0, 1.2, 0);
    }

    private void lunge(double speed) {
        Vec3 dir = Vec3.directionFromRotation(0, this.getYRot()).scale(speed);
        this.setDeltaMovement(dir.x, 0.2, dir.z);
        this.hurtMarked = true;
    }

    /**
     * One tick of a damage window: hits every target in range not struck yet this window.
     * {@code gameDamage} and {@code knockback} are the montage's DamageValue and Knockback.Strength.
     */
    private void hitWindow(Vec3 center, double radius, float gameDamage, float knockback, Element element) {
        strike(center, radius, gameDamage, knockback, element, this.struck);
    }

    /** Hits every target in range not yet in {@code struck}; no push when {@code knockback} is 0. */
    private void strike(Vec3 center, double radius, float gameDamage, float knockback, Element element, Set<UUID> struck) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * gameDamage / GAME_DAMAGE_UNIT;
        double push = knockback > 0 ? 0.4 + knockback / 4000.0 * 0.8 : 0;
        AABB box = new AABB(center, center).inflate(radius, 2.0, radius);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !this.isAlliedTo(e) && e.isAlive() && !struck.contains(e.getUUID()))) {
            struck.add(victim.getUUID());
            if (victim.hurt(this.damageSources().mobAttack(this), damage)) {
                BossSound hitSound = this.move == Move.LIGHTNING_SLAM ? BossSound.LIGHTNING_SLAM_RIPPLE_HIT
                        : BossSound.HITMARKER;
                this.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), hitSound.get(),
                        this.getSoundSource(), 1.0F, 1.0F);
                if (push > 0) {
                    Vec3 dir = victim.position().subtract(this.position()).normalize().scale(push);
                    victim.push(dir.x, 0.35, dir.z);
                }
                if (element == Element.ICE) {
                    victim.setTicksFrozen(Math.max(victim.getTicksFrozen(), victim.getTicksRequiredToFreeze() + 100));
                }
            }
        }
    }

    private void bolt(Vec3 at) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level());
        if (bolt != null) {
            bolt.moveTo(at.x, at.y, at.z);
            bolt.setVisualOnly(true);
            this.level().addFreshEntity(bolt);
        }
    }

    private void burst(ParticleOptions particle, Vec3 at, int count, double spread) {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(particle, at.x, at.y + 0.2, at.z, count, spread, 0.4, spread, 0.05);
        }
    }

    /** Ice-mine telegraph: a ring of snowflakes on the ground. */
    /** Telegraph: a ring of particles on the ground, about one per block of circumference (at least 16). */
    private void ring(ParticleOptions particle, Vec3 at, double radius) {
        if (this.level() instanceof ServerLevel server) {
            int points = Math.max(16, (int) (Math.PI * 2 * radius));
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                server.sendParticles(particle, at.x + Math.cos(angle) * radius, at.y + 0.1,
                        at.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Overcharge's lightning floor (GA_ReactToOverlapOnRelatedActors, FrostbiteDesertBossLightningFloor): 5 lightning
     * damage every 0.2 s for 5 s. The game's arena raises platforms as safe spots; here anyone outside the circle or
     * {@link #FLOOR_SAFE_HEIGHT} blocks above the boss's floor is safe.
     */
    private void tickFloor() {
        if (this.floorTicks <= 0 || this.floorCenter == null) {
            return;
        }
        this.floorTicks--;
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 24; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double dist = Math.sqrt(this.random.nextDouble()) * FLOOR_RADIUS;
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.floorCenter.x + Math.cos(angle) * dist,
                        this.floorCenter.y + 0.1, this.floorCenter.z + Math.sin(angle) * dist, 1, 0, 0.05, 0, 0.02);
            }
        }
        if (this.floorTicks % 20 == 0) {
            this.level().playSound(null, this.floorCenter.x, this.floorCenter.y, this.floorCenter.z,
                    BossSound.OVERCHARGE_FLOOR.get(), this.getSoundSource(), 2.5F, 1.0F);
        }
        if (this.floorTicks % 4 != 0) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 5 / GAME_DAMAGE_UNIT;
        AABB box = new AABB(this.floorCenter, this.floorCenter).inflate(FLOOR_RADIUS, FLOOR_SAFE_HEIGHT, FLOOR_RADIUS);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !this.isAlliedTo(e) && e.isAlive() && e.onGround()
                        && e.getY() < this.floorCenter.y + FLOOR_SAFE_HEIGHT
                        && e.position().subtract(this.floorCenter).horizontalDistance() <= FLOOR_RADIUS)) {
            if (victim.hurt(this.damageSources().mobAttack(this), damage)) {
                this.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                        BossSound.OVERCHARGE_FLOOR_DAMAGE.get(), this.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }

    /** Dungeons II's shielded Royal Guard: It Takes a Pillage's legioner when installed, else a vindicator. */
    private static EntityType<?> royalGuard() {
        EntityType<?> legioner = ForgeRegistries.ENTITY_TYPES.getValue(ROYAL_GUARD);
        return legioner != null && ForgeRegistries.ENTITY_TYPES.containsKey(ROYAL_GUARD) ? legioner : EntityType.VINDICATOR;
    }

    private void summon(ServerLevel server, EntityType<?> type, int count) {
        for (int i = 0; i < count; i++) {
            summon(server, type);
        }
    }

    private void summon(ServerLevel server, EntityType<?> type) {
        if (!(type.create(server) instanceof Mob minion)) {
            return;
        }
        double angle = this.random.nextDouble() * Math.PI * 2;
        double x = this.getX() + Math.cos(angle) * 4.0;
        double z = this.getZ() + Math.sin(angle) * 4.0;
        BlockPos pos = BlockPos.containing(x, this.getY(), z);
        minion.moveTo(x, server.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY(), z,
                this.random.nextFloat() * 360.0F, 0);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
        minion.setTarget(this.getTarget());
        server.addFreshEntity(minion);
        this.wave.add(minion.getUUID());
        burst(ParticleTypes.SOUL_FIRE_FLAME, minion.position(), 30, 0.5);
    }

    // ---- life cycle ----------------------------------------------------------------------------

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.spawnPending = true;
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide() && isInvulnerableNow() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide() && this.isAlive() && this.staggersDone < STAGGER_AT.length
                && this.getHealth() <= this.getMaxHealth() * STAGGER_AT[this.staggersDone]) {
            while (this.staggersDone < STAGGER_AT.length
                    && this.getHealth() <= this.getMaxHealth() * STAGGER_AT[this.staggersDone]) {
                this.staggersDone++;
            }
            startMove(Move.STAGGERED);
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StaggersDone", this.staggersDone);
        ListTag waveTag = new ListTag();
        this.wave.forEach(id -> waveTag.add(NbtUtils.createUUID(id)));
        tag.put("Wave", waveTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.staggersDone = tag.getInt("StaggersDone");
        this.wave.clear();
        tag.getList("Wave", Tag.TAG_INT_ARRAY).forEach(id -> this.wave.add(NbtUtils.loadUUID(id)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            sound(BossSound.DEATH_LONG);
        }
        this.move = Move.DEATH;
        this.mines.clear();
        this.floorTicks = 0;
        this.triggerAnim(CONTROLLER, Move.DEATH.id);
    }

    /** Hold the body for the death animation instead of vanilla's 20-tick topple. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isCreative() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!this.level().isClientSide()) {
                Move next = PREVIEW[this.previewIndex++ % PREVIEW.length];
                startMove(next);
                player.displayClientMessage(Component.literal("Prime Enchanter: " + next.id), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isBusy();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.ILLAGER;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other) || (other instanceof AbstractIllager && other.getTeam() == null);
    }

    // ---- boss bar ------------------------------------------------------------------------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    // ---- sounds --------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return BossSound.AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return BossSound.HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BossSound.DEATH.get();
    }

    /** Footsteps come from the walk animation's foot plants (see {@link #registerControllers}), not walked distance. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    private void playFootstep() {
        Level level = this.level();
        level.playLocalSound(this.getX(), this.getY(), this.getZ(), BossSound.STEP.get(), this.getSoundSource(),
                1.6F, 1.0F, false);
        level.playLocalSound(this.getX(), this.getY(), this.getZ(), BossSound.STEP_FOLEY.get(), this.getSoundSource(),
                1.2F, 1.0F, false);
        if (this.random.nextInt(3) == 0) {
            level.playLocalSound(this.getX(), this.getY(), this.getZ(), BossSound.CAPE.get(), this.getSoundSource(),
                    1.2F, 1.0F, false);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    // ---- GeckoLib ------------------------------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<PrimeEnchanter> controller = new AnimationController<>(this, CONTROLLER, 5, state -> {
            // The server's death trigger can lose to a move animation already playing; the client starts it itself
            if (this.isDeadOrDying() && state.getController().getTriggeredAnimation() != Move.DEATH.animation) {
                state.getController().tryTriggerAnimation(Move.DEATH.id);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        });
        controller.setSoundKeyframeHandler(event -> {
            if ("step".equals(event.getKeyframeData().getSound())) {
                playFootstep();
            }
        });
        for (Move each : Move.values()) {
            controller.triggerableAnim(each.id, each.animation);
        }
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
