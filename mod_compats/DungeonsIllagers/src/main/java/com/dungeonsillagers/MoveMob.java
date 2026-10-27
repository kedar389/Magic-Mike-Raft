package com.dungeonsillagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A Dungeons II illager whose every attack is a {@link Move}: the server triggers its animation, roots the mob for the
 * animation's length, and fires the move's effects on the ticks of the game's montage notifies (montage seconds x 20).
 * Damage is the game's value scaled so 25 equals {@link #GAME_DAMAGE_UNIT_TO_HP} health; distances are the game's UE
 * units, 100 to the block. Sneak + right-click with an empty hand in creative plays the next move, to preview them all.
 */
public abstract class MoveMob extends Monster implements GeoEntity {

    protected static final String CONTROLLER = "main";
    /** Blocks per UE unit: the models render at scale 1, 6.25 UE to the pixel. */
    protected static final double UE = 0.01;
    /** Health one point of game damage takes: the Prime Enchanter's 25-damage Forward Strike deals 14. */
    protected static final float GAME_DAMAGE_UNIT_TO_HP = 14.0F / 25.0F;
    /** Pause between two moves, so one never starts on the tick the last one ended. */
    private static final int MOVE_GAP = 10;
    private static final float MAX_TURN_PER_TICK = 25.0F;
    private static final ResourceLocation ROYAL_GUARD = new ResourceLocation("takesapillage", "legioner");
    private static final ResourceLocation ENCHANTER = new ResourceLocation("enchantwithmob", "enchanter");

    /** One animation-driven action. Length in ticks matches the animation file. */
    public record Move(String id, int length, RawAnimation animation) {

        public static Move of(String mob, String id, int length) {
            return new Move(id, length, RawAnimation.begin().thenPlay("animation." + mob + "." + id));
        }
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String mobName;
    private final RawAnimation idle;
    @Nullable
    private final RawAnimation walk;

    /** Targets already struck by the current hit window; each window hits a target once. */
    protected final Set<UUID> struck = new HashSet<>();
    @Nullable
    protected Move move;
    protected int moveTick;
    private int gap = MOVE_GAP;
    private final Map<Move, Integer> readyAt = new HashMap<>();
    private int previewIndex;
    private boolean spawnPending;
    /** How many of {@link #staggerAt()}'s health thresholds have already staggered the mob. */
    private int staggersDone;
    /** The last summoned wave; summons wait until every one of them is dead or gone. */
    private final List<UUID> wave = new ArrayList<>();

    protected MoveMob(EntityType<? extends Monster> type, Level level, String mobName, boolean hasWalk) {
        super(type, level);
        this.mobName = mobName;
        this.idle = RawAnimation.begin().thenLoop("animation." + mobName + ".idle");
        this.walk = hasWalk ? RawAnimation.begin().thenLoop("animation." + mobName + ".walk") : null;
    }

    // ---- what each mob defines -------------------------------------------------------------------

    /** Every move, for the animation controller and the creative preview. */
    protected abstract List<Move> moves();

    /** The move to start now against {@code target}, or null to keep moving. */
    @Nullable
    protected abstract Move chooseMove(LivingEntity target);

    /** Effects of {@code current} on its tick {@code t}. */
    protected abstract void onMoveTick(Move current, int t);

    /** Cooldown in ticks after {@code finished} ends; 0 for none. */
    protected abstract int cooldown(Move finished);

    /** The spawn move, played once when the mob is spawned; null for none. */
    @Nullable
    protected abstract Move spawnMove();

    /** The stagger move. */
    protected abstract Move staggerMove();

    /** Health fractions that stagger the mob once each, interrupting whatever it is doing. */
    protected abstract float[] staggerAt();

    /** Movement while free: chase, keep range or retreat. Called on ticks no move is playing. */
    protected void tickMovement(@Nullable LivingEntity target) {
    }

    /** Paths towards {@code target}, re-planning every 10 ticks rather than every tick. */
    protected void chase(LivingEntity target) {
        if (this.getNavigation().isDone() || this.tickCount % 10 == 0) {
            this.getNavigation().moveTo(target, 1.0);
        }
    }

    /** Called when {@code finished} ends; may start a follow-up move. */
    protected void onMoveEnd(Move finished) {
    }

    /** Animation sound keyframes ("step", ...), played on the client. */
    protected void onSoundKeyframe(String sound) {
    }

    /** Loop played while free and not moving; subclasses may swap it (running, fidgets). */
    protected RawAnimation baseAnimation(AnimationState<MoveMob> state) {
        return state.isMoving() && this.walk != null ? this.walk : this.idle;
    }

    // ---- moves -----------------------------------------------------------------------------------

    protected void startMove(Move next) {
        if (this.level().isClientSide()) {
            return;
        }
        this.move = next;
        this.moveTick = 0;
        this.struck.clear();
        this.getNavigation().stop();
        this.triggerAnim(CONTROLLER, next.id());
    }

    /** Stops the current move without its cooldown or follow-up. */
    protected void cancelMove() {
        if (this.move != null) {
            this.stopTriggeredAnimation(CONTROLLER, this.move.id());
            this.move = null;
        }
    }

    protected boolean isBusy() {
        return this.move != null;
    }

    protected boolean ready(Move candidate) {
        return this.tickCount >= this.readyAt.getOrDefault(candidate, 0);
    }

    /** No plain melee hits: every attack is a move picked by {@link #chooseMove}. */
    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    private void endMove(Move finished) {
        int cooldown = cooldown(finished);
        if (cooldown > 0) {
            this.readyAt.put(finished, this.tickCount + cooldown);
        }
        this.move = null;
        this.gap = MOVE_GAP;
        if (this.isAlive()) {
            onMoveEnd(finished);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.spawnPending && this.tickCount >= 2) {
            this.spawnPending = false;
            if (spawnMove() != null) {
                startMove(spawnMove());
            }
        }
        if (this.move != null) {
            this.getNavigation().stop();
            faceTarget();
            onMoveTick(this.move, this.moveTick);
            if (this.move != null && ++this.moveTick >= this.move.length()) {
                endMove(this.move);
            }
            return;
        }
        LivingEntity target = this.getTarget();
        tickMovement(target);
        if (this.gap > 0) {
            this.gap--;
            return;
        }
        if (target != null && target.isAlive()) {
            Move next = chooseMove(target);
            if (next != null) {
                startMove(next);
            }
        }
    }

    /**
     * Rooted moves skip vanilla body turning, so attacks keep aiming at the target the way the game's
     * AN_FocusOnTarget notifies do. Hit boxes use this facing.
     */
    private void faceTarget() {
        LivingEntity target = this.getTarget();
        if (target == null || this.move == staggerMove()) {
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

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isBusy();
    }

    // ---- damage ----------------------------------------------------------------------------------

    /** Health a hit of {@code gameDamage} takes. */
    protected static float damageOf(float gameDamage) {
        return gameDamage * GAME_DAMAGE_UNIT_TO_HP;
    }

    /**
     * One tick of a damage window: hits every target within {@code radius} of {@code center} not struck yet this window.
     * {@code knockback} is the game's Knockback.Strength (0 = none); {@code onHit} runs for each target hurt.
     */
    protected void hitWindow(Vec3 center, double radius, float gameDamage, float knockback, @Nullable Consumer<LivingEntity> onHit) {
        strike(center, radius, gameDamage, knockback, onHit, this.struck);
    }

    /** Hits every target in range not yet in {@code struck}. */
    protected void strike(Vec3 center, double radius, float gameDamage, float knockback,
            @Nullable Consumer<LivingEntity> onHit, Set<UUID> struck) {
        AABB box = new AABB(center, center).inflate(radius, 2.0, radius);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !this.isAlliedTo(e) && e.isAlive() && !struck.contains(e.getUUID())
                        && e.position().subtract(center).horizontalDistance() <= radius + e.getBbWidth() / 2)) {
            struck.add(victim.getUUID());
            if (victim.hurt(this.damageSources().mobAttack(this), damageOf(gameDamage))) {
                if (knockback > 0) {
                    // Game strength 4000 (Prime Enchanter Forward Strike) -> 0.6; a vanilla mob hit is about 0.4
                    double push = 0.25 + knockback / 4000.0 * 0.35;
                    Vec3 dir = victim.position().subtract(this.position()).normalize().scale(push);
                    victim.push(dir.x, 0.35, dir.z);
                }
                if (onHit != null) {
                    onHit.accept(victim);
                }
            }
        }
    }

    // ---- stagger ---------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        float[] thresholds = staggerAt();
        if (hurt && !this.level().isClientSide() && this.isAlive() && this.staggersDone < thresholds.length
                && this.getHealth() <= this.getMaxHealth() * thresholds[this.staggersDone]) {
            while (this.staggersDone < thresholds.length
                    && this.getHealth() <= this.getMaxHealth() * thresholds[this.staggersDone]) {
                this.staggersDone++;
            }
            onStaggered();
            startMove(staggerMove());
        }
        return hurt;
    }

    /** Called when a stagger interrupts the current move. */
    protected void onStaggered() {
    }

    // ---- summons ---------------------------------------------------------------------------------

    protected boolean waveAlive() {
        if (this.level() instanceof ServerLevel server) {
            this.wave.removeIf(id -> !(server.getEntity(id) instanceof LivingEntity minion) || !minion.isAlive());
        }
        return !this.wave.isEmpty();
    }

    /** Dungeons II's shielded Royal Guard: It Takes a Pillage's legioner when installed, else a vindicator. */
    protected static EntityType<?> royalGuard() {
        return ForgeRegistries.ENTITY_TYPES.containsKey(ROYAL_GUARD)
                ? ForgeRegistries.ENTITY_TYPES.getValue(ROYAL_GUARD) : EntityType.VINDICATOR;
    }

    /** Enchant with Mob's Enchanter when installed, else none. */
    @Nullable
    protected static EntityType<?> enchanter() {
        return ForgeRegistries.ENTITY_TYPES.containsKey(ENCHANTER) ? ForgeRegistries.ENTITY_TYPES.getValue(ENCHANTER) : null;
    }

    /** Spawns {@code count} of {@code type} on the ground {@code minRadius}..{@code maxRadius} blocks away. */
    protected void summon(ServerLevel server, @Nullable EntityType<?> type, int count, double minRadius, double maxRadius,
            ParticleOptions particle) {
        for (int i = 0; type != null && i < count; i++) {
            if (!(type.create(server) instanceof Mob minion)) {
                return;
            }
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = minRadius + this.random.nextDouble() * (maxRadius - minRadius);
            double x = this.getX() + Math.cos(angle) * dist;
            double z = this.getZ() + Math.sin(angle) * dist;
            BlockPos pos = BlockPos.containing(x, this.getY(), z);
            double ground = server.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY();
            minion.moveTo(x, Math.abs(ground - this.getY()) < 4 ? ground : this.getY(), z, this.random.nextFloat() * 360.0F, 0);
            minion.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            minion.setTarget(this.getTarget());
            onSummoned(minion);
            server.addFreshEntity(minion);
            this.wave.add(minion.getUUID());
            burst(particle, minion.position(), 30, 0.5);
        }
    }

    /** Last touch on a summoned minion before it enters the world. */
    protected void onSummoned(Mob minion) {
    }

    // ---- positions and effects -------------------------------------------------------------------

    protected Vec3 inFront(double distance) {
        return this.position().add(Vec3.directionFromRotation(0, this.getYRot()).scale(distance));
    }

    protected Vec3 side(boolean left) {
        return Vec3.directionFromRotation(0, this.getYRot() + (left ? -90 : 90));
    }

    /** The ground height under {@code at} when within 3 blocks of {@code at.y}, else {@code at.y}. */
    protected double groundAt(Vec3 at) {
        double ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(at)).getY();
        return Math.abs(ground - at.y) < 3 ? ground : at.y;
    }

    protected void sound(IllagerSound sound, float volume) {
        this.playSound(sound.get(), volume, 1.0F);
    }

    protected void soundAt(IllagerSound sound, Vec3 at, float volume) {
        this.level().playSound(null, at.x, at.y, at.z, sound.get(), this.getSoundSource(), volume, 1.0F);
    }

    protected void burst(ParticleOptions particle, Vec3 at, int count, double spread) {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(particle, at.x, at.y + 0.2, at.z, count, spread, 0.4, spread, 0.05);
        }
    }

    /** Telegraph: a ring of particles on the ground, about one per block of circumference (at least 16). */
    protected void ring(ParticleOptions particle, Vec3 at, double radius) {
        if (this.level() instanceof ServerLevel server) {
            int points = Math.max(16, (int) (Math.PI * 2 * radius));
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                server.sendParticles(particle, at.x + Math.cos(angle) * radius, at.y + 0.1,
                        at.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
            }
        }
    }

    /** The vanilla step sound of the block underfoot, played locally (for animation footstep keyframes). */
    protected void blockStep() {
        BlockPos below = this.blockPosition().below();
        BlockState state = this.level().getBlockState(below);
        if (state.isAir()) {
            return;
        }
        SoundType type = state.getSoundType(this.level(), below, this);
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), type.getStepSound(), this.getSoundSource(),
                type.getVolume() * 0.15F, type.getPitch(), false);
    }

    // ---- life cycle ------------------------------------------------------------------------------

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.spawnPending = true;
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isCreative() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!this.level().isClientSide()) {
                List<Move> all = moves();
                Move next = all.get(this.previewIndex++ % all.size());
                startMove(next);
                player.displayClientMessage(Component.literal(this.getName().getString() + ": " + next.id()), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public MobType getMobType() {
        return MobType.ILLAGER;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other)
                || ((other instanceof AbstractIllager || other instanceof MoveMob) && other.getTeam() == null);
    }


    // ---- GeckoLib --------------------------------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<MoveMob> controller = new AnimationController<>(this, CONTROLLER, 5,
                state -> {
                    state.setAndContinue(baseAnimation(state));
                    return PlayState.CONTINUE;
                });
        controller.setSoundKeyframeHandler(event -> onSoundKeyframe(event.getKeyframeData().getSound()));
        for (Move each : moves()) {
            controller.triggerableAnim(each.id(), each.animation());
        }
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public String mobName() {
        return this.mobName;
    }
}
