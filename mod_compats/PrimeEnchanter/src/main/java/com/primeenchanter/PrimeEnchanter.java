package com.primeenchanter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
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
    private static final int MOVE_COOLDOWN = 30;
    private static final float GAME_DAMAGE_UNIT = 25.0F;
    private static final double UE_TO_BLOCKS = 1.4 / 100.0;
    private static final int MINE_FUSE = 23;

    /** One animation-driven action. Lengths match the animation file, in ticks. */
    enum Move {
        FURY_FIST("fury_fist", 138, true),
        FORWARD_STRIKE("forward_strike", 50, true),
        LIGHTNING_SLAM("lightning_slam", 88, true),
        LAUNCH("launch", 79, false),
        OVERCHARGE("overcharge", 72, false),
        SUMMON("summon", 45, false),
        ICEMINES("icemines", 45, false),
        SPAWN("spawn", 47, false),
        STAGGERED("staggered", 45, false),
        DEATH("death", DEATH_TICKS, false);

        final String id;
        final int length;
        final boolean melee;
        final RawAnimation animation;

        Move(String id, int length, boolean melee) {
            this.id = id;
            this.length = length;
            this.melee = melee;
            String name = "animation.prime_enchanter." + id;
            this.animation = id.equals("death")
                    ? RawAnimation.begin().thenPlayAndHold(name)
                    : RawAnimation.begin().thenPlay(name);
        }
    }

    private enum Element { NONE, ICE, LIGHTNING }

    private static final Move[] MELEE = {Move.FURY_FIST, Move.FORWARD_STRIKE, Move.LIGHTNING_SLAM};
    private static final Move[] RANGED = {Move.LAUNCH, Move.OVERCHARGE, Move.SUMMON, Move.ICEMINES};
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
    private int cooldown = MOVE_COOLDOWN;
    private int previewIndex;
    private boolean spawnPending;
    /** How many of the {@link #STAGGER_AT} health thresholds have already staggered the boss. */
    private int staggersDone;

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

    /** MeleeAttackGoal calls this when in reach: open a melee move instead of a plain hit. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (isBusy() || this.cooldown > 0) {
            return false;
        }
        startMove(MELEE[this.random.nextInt(MELEE.length)]);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        tickMines();
        if (this.spawnPending && this.tickCount >= 2) {
            this.spawnPending = false;
            startMove(Move.SPAWN);
        }
        if (this.move != null && this.move != Move.DEATH) {
            this.getNavigation().stop();
            onMoveTick(this.move, this.moveTick);
            if (++this.moveTick >= this.move.length) {
                this.move = null;
                this.cooldown = MOVE_COOLDOWN;
            }
            return;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.distanceTo(target) > 6.0F && this.hasLineOfSight(target)
                && this.random.nextInt(20) == 0) {
            startMove(RANGED[this.random.nextInt(RANGED.length)]);
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
                    hitWindow(armStrike(false), 1.6, 12, 1500, Element.NONE);
                }
                if (t >= 25 && t <= 28) {
                    hitWindow(armStrike(true), 1.6, 12, 1500, Element.NONE);
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
                if ((t == 44 || t == 49 || t == 54) && this.getTarget() != null) {
                    for (int i = 0; i < 3; i++) {
                        Vec3 at = this.getTarget().position()
                                .add(this.random.nextGaussian() * 3.0, 0, this.random.nextGaussian() * 3.0);
                        bolt(at);
                        this.level().playSound(null, at.x, at.y, at.z, BossSound.OVERCHARGE_FLOOR.get(),
                                this.getSoundSource(), 1.5F, 1.0F);
                        this.struck.clear();
                        hitWindow(at, 2.0, 14, 1000, Element.LIGHTNING);
                    }
                }
            }
            case SUMMON -> {
                if (t == 1) {
                    sound(BossSound.SUMMON);
                }
                if (t == 11 && this.level() instanceof ServerLevel server) {
                    summon(server, EntityType.VINDICATOR);
                    summon(server, EntityType.VINDICATOR);
                    summon(server, EntityType.PILLAGER);
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

    private void tickMines() {
        for (var it = this.mines.iterator(); it.hasNext(); ) {
            Mine mine = it.next();
            if (mine.fuse-- > 0) {
                if (mine.fuse % 4 == 0) {
                    ring(mine.pos, 1.2);
                }
                continue;
            }
            it.remove();
            this.level().playSound(null, mine.pos.x, mine.pos.y, mine.pos.z, BossSound.ICE_MINE_EXPLODE.get(),
                    this.getSoundSource(), 1.5F, 1.0F);
            burst(ParticleTypes.SNOWFLAKE, mine.pos, 50, 1.0);
            this.struck.clear();
            hitWindow(mine.pos, 1.8, 18, 1500, Element.ICE);
        }
    }

    private void placeMine(Vec3 at) {
        this.mines.add(new Mine(at));
        this.level().playSound(null, at.x, at.y, at.z, BossSound.ICE_MINE_SPAWN.get(), this.getSoundSource(), 1.2F, 1.0F);
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
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * gameDamage / GAME_DAMAGE_UNIT;
        double push = 0.4 + knockback / 4000.0 * 0.8;
        AABB box = new AABB(center, center).inflate(radius, 2.0, radius);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !this.isAlliedTo(e) && e.isAlive() && !this.struck.contains(e.getUUID()))) {
            this.struck.add(victim.getUUID());
            if (victim.hurt(this.damageSources().mobAttack(this), damage)) {
                BossSound hitSound = this.move == Move.LIGHTNING_SLAM ? BossSound.LIGHTNING_SLAM_RIPPLE_HIT
                        : this.move == Move.OVERCHARGE ? BossSound.OVERCHARGE_FLOOR_DAMAGE
                        : BossSound.HITMARKER;
                this.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), hitSound.get(),
                        this.getSoundSource(), 1.0F, 1.0F);
                Vec3 dir = victim.position().subtract(this.position()).normalize().scale(push);
                victim.push(dir.x, 0.35, dir.z);
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
    private void ring(Vec3 at, double radius) {
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 16; i++) {
                double angle = Math.PI * 2 * i / 16;
                server.sendParticles(ParticleTypes.SNOWFLAKE, at.x + Math.cos(angle) * radius, at.y + 0.1,
                        at.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
            }
        }
    }

    private void summon(ServerLevel server, EntityType<? extends AbstractIllager> type) {
        AbstractIllager minion = type.create(server);
        if (minion == null) {
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.staggersDone = tag.getInt("StaggersDone");
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            sound(BossSound.DEATH_LONG);
        }
        this.move = Move.DEATH;
        this.mines.clear();
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
        AnimationController<PrimeEnchanter> controller = new AnimationController<>(this, CONTROLLER, 5,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE));
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
