package com.dungeonsillagers;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * The Supreme Evoker (DT_MobType3Definition SupremeEvoker, ST_SupremeEvoker). Health is the game's 1000 / 5, the
 * Prime Enchanter's ratio; staggers at 75/50/25 % like him. Summons illagers at full health and soul vexes below 60 %,
 * rings himself with fang traps when crowded, teleports away, raises a bubble that soaks 50 game damage, and throws
 * volleys of enchanted orbs from both hands.
 */
public class SupremeEvoker extends MoveMob {

    private static final String NAME = "supreme_evoker";
    static final Move ENCHANTED_PROJECTILES = Move.of(NAME, "enchanted_projectiles", 50);
    static final Move SUMMON = Move.of(NAME, "summon", 40);
    /** SummonIllagers.Vex plays the same montage. */
    static final Move SUMMON_VEX = new Move("summon_vex", 40, RawAnimation.begin().thenPlay("animation.supreme_evoker.summon"));
    static final Move SHIELD = Move.of(NAME, "shield", 32);
    static final Move FANG_TRAP = Move.of(NAME, "fang_trap", 62);
    static final Move TELEPORT = Move.of(NAME, "teleport", 40);
    static final Move SPAWN = Move.of(NAME, "spawn", 50);
    static final Move STAGGERED = Move.of(NAME, "staggered", 40);
    private static final List<Move> MOVES =
            List.of(ENCHANTED_PROJECTILES, SUMMON, SUMMON_VEX, SHIELD, FANG_TRAP, TELEPORT, SPAWN, STAGGERED);

    /** Health scale: game 1000 -> 200. The bubble (EvokerSoulShield) holds 50 game health. */
    private static final float HEALTH_SCALE = 200.0F / 1000.0F;
    private static final float SHIELD_HEALTH = 50 * HEALTH_SCALE;
    /** GA_EngageTargetMovement AcceptanceRadiusOverride 600. */
    private static final double KEEP_RANGE = 600 * UE;
    /** Fang Trap: target within 400 UE; seven fangs on a 240 UE circle (SpawnDirectionOffset) around the boss. */
    private static final double FANG_TRIGGER = 400 * UE, FANG_RING = 240 * UE;
    private static final int FANGS = 7;
    /** Enchanted Projectiles: Range 1000, six orbs over an 80 degree fan (AngleSpan), aimed at the target's body. */
    private static final double ORB_RANGE = 1000 * UE;
    private static final float ORB_FAN = 80.0F;
    private static final EntityDataAccessor<Float> SHIELD_LEFT = SynchedEntityData.defineId(SupremeEvoker.class, EntityDataSerializers.FLOAT);

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.dungeons_illagers.supreme_evoker"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    public SupremeEvoker(EntityType<? extends Monster> type, Level level) {
        super(type, level, NAME, true);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000 * HEALTH_SCALE)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractIllager.class, MoveMob.class, PrimeEnchanter.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHIELD_LEFT, 0.0F);
    }

    /** Health left in the protective bubble; 0 = no bubble. */
    public float shieldLeft() {
        return this.entityData.get(SHIELD_LEFT);
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
        return new float[] {0.75F, 0.5F, 0.25F};
    }

    /** CooldownDuration of each ability, in ticks; they start when the move ends. */
    @Override
    protected int cooldown(Move finished) {
        if (finished == ENCHANTED_PROJECTILES) {
            return 60;
        } else if (finished == SUMMON) {
            return 520;
        } else if (finished == SUMMON_VEX) {
            return 640;
        } else if (finished == SHIELD) {
            return 460;
        } else if (finished == FANG_TRAP) {
            return 100;
        }
        return finished == TELEPORT ? 300 : 0;
    }

    /**
     * ST_SupremeEvoker, Aggressive, in order: Summon Illagers (HP >= 95 %), Summon Soul Vex (HP < 60 %), Fang Trap
     * (target within 400 UE), Teleport Retreat (no bubble, HP > 15 %), Activate Shield (no bubble), Enchanted
     * Projectiles, Chase (HP > 25 %). Teleport Construct (HP < 15 %, to the arena's centre) needs the game's arena and
     * is left out. Like the Prime Enchanter, a summon waits until the last wave is dead.
     */
    @Override
    @Nullable
    protected Move chooseMove(LivingEntity target) {
        float health = this.getHealth() / this.getMaxHealth();
        boolean shielded = shieldLeft() > 0;
        if (health >= 0.95F && ready(SUMMON) && !waveAlive()) {
            return SUMMON;
        }
        if (health < 0.6F && ready(SUMMON_VEX) && !waveAlive()) {
            return SUMMON_VEX;
        }
        if (ready(FANG_TRAP) && this.position().subtract(target.position()).horizontalDistance() <= FANG_TRIGGER
                && Math.abs(target.getY() - this.getY()) <= FANG_TRIGGER) {
            return FANG_TRAP;
        }
        if (!shielded && health > 0.15F && ready(TELEPORT)) {
            return TELEPORT;
        }
        if (!shielded && ready(SHIELD)) {
            return SHIELD;
        }
        if (ready(ENCHANTED_PROJECTILES) && this.distanceTo(target) <= ORB_RANGE && this.hasLineOfSight(target)) {
            return ENCHANTED_PROJECTILES;
        }
        return null;
    }

    @Override
    protected void tickMovement(@Nullable LivingEntity target) {
        if (target == null) {
            return;
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.getHealth() > this.getMaxHealth() * 0.25F && this.distanceTo(target) > KEEP_RANGE) {
            chase(target);
        } else {
            this.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && shieldLeft() > 0 && this.tickCount % 40 == 0) {
            sound(IllagerSound.SUPREME_EVOKER_SHIELD_LOOP, 0.8F);
        }
    }

    /** Montage notifies of the AM_SupremeEvoker_* montages. */
    @Override
    protected void onMoveTick(Move current, int t) {
        if (current == ENCHANTED_PROJECTILES) {
            if (t == 9) {
                sound(IllagerSound.SUPREME_EVOKER_PROJECTILE_CAST_1, 2.0F);
                burst(ParticleTypes.WITCH, hand(true), 15, 0.2);
            }
            if (t == 17) {
                sound(IllagerSound.SUPREME_EVOKER_PROJECTILE_CAST_2, 2.0F);
                burst(ParticleTypes.WITCH, hand(false), 15, 0.2);
            }
            if (t == 18) {
                volley(true);
            }
            if (t == 28) {
                volley(false);
            }
        } else if (current == SUMMON || current == SUMMON_VEX) {
            Vec3 above = this.position().add(0, 3.2, 0);
            if (t == 4) {
                burst(ParticleTypes.SOUL_FIRE_FLAME, above, 30, 0.4);
            }
            if (t == 6) {
                sound(IllagerSound.SUPREME_EVOKER_SUMMON, 2.0F);
            }
            if (t == 9) {
                sound(IllagerSound.SUPREME_EVOKER_SUMMON_VOX, 2.0F);
            }
            if (t > 4 && t < 24 && t % 3 == 0) {
                burst(ParticleTypes.WITCH, above, 6, 0.6);
            }
            if (t == 24) {
                sound(IllagerSound.SUPREME_EVOKER_SUMMON_WAVE, 2.0F);
                ring(ParticleTypes.SOUL_FIRE_FLAME, this.position(), 3.0);
                if (this.level() instanceof ServerLevel server) {
                    summonWave(server, current == SUMMON_VEX);
                }
            }
        } else if (current == SHIELD) {
            if (t == 8) {
                sound(IllagerSound.SUPREME_EVOKER_SHIELD_ACTIVATE, 2.0F);
                burst(ParticleTypes.WITCH, hand(true), 25, 0.3);
                burst(ParticleTypes.END_ROD, this.position().add(0, 1.6, 0), 30, 1.6);
                this.entityData.set(SHIELD_LEFT, SHIELD_HEALTH);
            }
        } else if (current == FANG_TRAP) {
            if (t >= 6 && t < 32 && t % 2 == 0) {
                burst(ParticleTypes.WITCH, hand(true), 2, 0.1);
                burst(ParticleTypes.WITCH, hand(false), 2, 0.1);
            }
            if (t == 9) {
                sound(IllagerSound.SUPREME_EVOKER_FANG_TELEGRAPH, 2.0F);
            }
            if (t == 20) {
                ring(ParticleTypes.WITCH, this.position(), FANG_RING);
            }
            if (t == 37) {
                sound(IllagerSound.SUPREME_EVOKER_FANG_CAST, 2.0F);
                fangRing();
            }
            if (t == 38) {
                burst(ParticleTypes.ENCHANTED_HIT, hand(true), 15, 0.2);
                burst(ParticleTypes.ENCHANTED_HIT, hand(false), 15, 0.2);
            }
        } else if (current == TELEPORT) {
            teleportTick(t);
        } else if (current == SPAWN) {
            if (t == 0) {
                sound(IllagerSound.SUPREME_EVOKER_SPAWN, 2.5F);
                burst(ParticleTypes.REVERSE_PORTAL, this.position().add(0, 1.4, 0), 80, 0.8);
            }
        } else if (current == STAGGERED) {
            if (t == 0) {
                sound(IllagerSound.SUPREME_EVOKER_STAGGER_START, 2.0F);
            }
            if (t == 30) {
                sound(IllagerSound.SUPREME_EVOKER_STAGGER_END, 2.0F);
            }
        }
    }

    /**
     * GA_TeleportSequence (Teleport.Flee): vanish at 0.47 s (untargetable and invulnerable until 1.0 s), land 1 s later
     * at a flee point. The game's arena flee points become a random spot 8-14 blocks from the target.
     */
    private void teleportTick(int t) {
        if (t == 0) {
            sound(IllagerSound.SUPREME_EVOKER_TELEPORT_OUT, 2.0F);
            ring(ParticleTypes.REVERSE_PORTAL, this.position(), 1.2);
        }
        if (t == 9) {
            burst(ParticleTypes.PORTAL, this.position().add(0, 1.4, 0), 80, 0.6);
            burst(ParticleTypes.WITCH, this.position().add(0, 1.4, 0), 30, 0.6);
            this.setInvisible(true);
        }
        if (t == 10) {
            sound(IllagerSound.SUPREME_EVOKER_TELEPORT_OUT_VOX, 2.0F);
        }
        if (t == 20) {
            Vec3 to = fleePoint();
            if (to != null) {
                this.teleportTo(to.x, to.y, to.z);
            }
            this.setInvisible(false);
            sound(IllagerSound.SUPREME_EVOKER_TELEPORT_IN, 2.0F);
        }
        if (t == 21) {
            burst(ParticleTypes.REVERSE_PORTAL, this.position().add(0, 3.0, 0), 60, 0.5);
            burst(ParticleTypes.CLOUD, this.position(), 15, 0.6);
            sound(IllagerSound.SUPREME_EVOKER_TELEPORT_IN_VOX, 2.0F);
        }
    }

    @Nullable
    private Vec3 fleePoint() {
        LivingEntity target = this.getTarget();
        Vec3 from = target != null ? target.position() : this.position();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = 8 + this.random.nextDouble() * 6;
            Vec3 at = new Vec3(from.x + Math.cos(angle) * dist, from.y, from.z + Math.sin(angle) * dist);
            at = new Vec3(at.x, groundAt(at), at.z);
            BlockPos feet = BlockPos.containing(at);
            BlockState below = this.level().getBlockState(feet.below());
            if (below.isSolid() && this.level().noCollision(this, this.getBoundingBox().move(at.subtract(this.position())))) {
                return at;
            }
        }
        return null;
    }

    private boolean isTeleporting() {
        return this.move == TELEPORT && this.moveTick >= 9 && this.moveTick < 20;
    }

    /** Where a hand sits while casting: J_L_HandSocket / J_R_HandSocket, held out in front. */
    private Vec3 hand(boolean left) {
        return this.position().add(side(left).scale(0.55)).add(Vec3.directionFromRotation(0, this.getYRot()).scale(0.4)).add(0, 1.5, 0);
    }

    /** Half of the six-orb fan (AngleSpreadType Sequential): left hand first, then right. */
    private void volley(boolean left) {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        Vec3 from = hand(left);
        double dx = target.getX() - from.x, dz = target.getZ() - from.z;
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) -(Mth.atan2(target.getY(0.5) - from.y, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
        for (int i = 0; i < 3; i++) {
            int index = left ? i : i + 3;
            float offset = -ORB_FAN / 2 + ORB_FAN * index / 5;
            EvokerOrb orb = DungeonsIllagersMod.EVOKER_ORB.get().create(this.level());
            if (orb == null) {
                return;
            }
            orb.setOwner(this);
            orb.moveTo(from.x, from.y - 0.25, from.z, yaw + offset, pitch);
            orb.setDeltaMovement(Vec3.directionFromRotation(pitch, yaw + offset).scale(EvokerOrb.SPEED));
            this.level().addFreshEntity(orb);
        }
        burst(ParticleTypes.END_ROD, from, 10, 0.15);
    }

    /** Circular_FacingTarget_StartingTargetDirection: seven fangs around the boss, the first towards the target. */
    private void fangRing() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        LivingEntity target = this.getTarget();
        double start = target != null ? Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) : 0;
        for (int i = 0; i < FANGS; i++) {
            double angle = start + Math.PI * 2 * i / FANGS;
            Vec3 at = this.position().add(Math.cos(angle) * FANG_RING, 0, Math.sin(angle) * FANG_RING);
            FangTrap fang = DungeonsIllagersMod.FANG_TRAP.get().create(server);
            if (fang == null) {
                return;
            }
            fang.moveTo(at.x, groundAt(at), at.z, (float) (angle * Mth.RAD_TO_DEG) - 90.0F, 0);
            fang.setOwner(this);
            server.addFreshEntity(fang);
        }
    }

    /**
     * SummonIllagers: 1-2 Pillagers, 2-6 Vindicators, 0-2 Royal Guards, 0-1 Enchanter, 2-9 blocks out.
     * SummonIllagers.Vex: 4 Vexes, 1-2 Pillagers, 1-2 Royal Guards, 1 Enchanter, 5-8 blocks out.
     */
    private void summonWave(ServerLevel server, boolean vexes) {
        if (vexes) {
            summon(server, EntityType.VEX, 4, 5, 8, ParticleTypes.SOUL);
            summon(server, EntityType.PILLAGER, 1 + this.random.nextInt(2), 5, 8, ParticleTypes.SOUL_FIRE_FLAME);
            summon(server, royalGuard(), 1 + this.random.nextInt(2), 5, 8, ParticleTypes.SOUL_FIRE_FLAME);
            summon(server, enchanter(), 1, 5, 8, ParticleTypes.SOUL_FIRE_FLAME);
        } else {
            summon(server, EntityType.PILLAGER, 1 + this.random.nextInt(2), 2, 9, ParticleTypes.SOUL_FIRE_FLAME);
            summon(server, EntityType.VINDICATOR, 2 + this.random.nextInt(5), 2, 9, ParticleTypes.SOUL_FIRE_FLAME);
            summon(server, royalGuard(), this.random.nextInt(3), 2, 9, ParticleTypes.SOUL_FIRE_FLAME);
            summon(server, enchanter(), this.random.nextInt(2), 2, 9, ParticleTypes.SOUL_FIRE_FLAME);
        }
    }

    /** Vexes serve him and fade like an Evoker's (30-120 s). */
    @Override
    protected void onSummoned(Mob minion) {
        if (minion instanceof Vex vex) {
            vex.setOwner(this);
            vex.setBoundOrigin(this.blockPosition());
            vex.setLimitedLife(20 * (30 + this.random.nextInt(90)));
        }
    }

    // ---- bubble and invulnerability ---------------------------------------------------------------

    /** The bubble takes every hit until broken (GA_ApplyAndHandleGameplayEffects DamageRedirect); teleporting is untouchable. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        if (isTeleporting()) {
            return false;
        }
        float shield = shieldLeft();
        if (shield > 0 && amount > 0) {
            shield -= amount;
            if (shield > 0) {
                this.entityData.set(SHIELD_LEFT, shield);
                sound(IllagerSound.SUPREME_EVOKER_SHIELD_HIT, 1.5F);
                burst(ParticleTypes.ENCHANTED_HIT, this.position().add(0, 1.6, 0), 15, 1.2);
            } else {
                breakShield();
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    private void breakShield() {
        this.entityData.set(SHIELD_LEFT, 0.0F);
        sound(IllagerSound.SUPREME_EVOKER_SHIELD_END, 2.0F);
        burst(ParticleTypes.WITCH, this.position().add(0, 1.6, 0), 60, 1.8);
        burst(ParticleTypes.END_ROD, this.position().add(0, 1.6, 0), 40, 1.8);
    }

    @Override
    protected void onStaggered() {
        this.setInvisible(false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Shield", shieldLeft());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SHIELD_LEFT, tag.getFloat("Shield"));
        this.setInvisible(false);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.setInvisible(false);
        if (!this.level().isClientSide()) {
            this.entityData.set(SHIELD_LEFT, 0.0F);
            burst(ParticleTypes.SOUL, this.position().add(0, 1.4, 0), 60, 0.8);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ---- boss bar --------------------------------------------------------------------------------

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

    // ---- sounds ----------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return IllagerSound.SUPREME_EVOKER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return IllagerSound.SUPREME_EVOKER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return IllagerSound.SUPREME_EVOKER_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    /** Footsteps come from the walk animation's foot plants. */
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void onSoundKeyframe(String sound) {
        if ("step".equals(sound)) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), IllagerSound.SUPREME_EVOKER_STEP.get(),
                    this.getSoundSource(), 1.2F, 1.0F, false);
        }
    }
}
