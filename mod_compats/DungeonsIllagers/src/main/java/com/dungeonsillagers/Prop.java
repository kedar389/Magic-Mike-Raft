package com.dungeonsillagers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A summoned Dungeons II prop actor: plays its one animation from the moment it appears, acts on fixed ticks of it
 * (the montage notifies) and removes itself when the animation ends. Not saved: props only live a few seconds.
 */
public abstract class Prop extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation animation;
    private final int lifetime;
    @Nullable
    private UUID ownerId;
    @Nullable
    private LivingEntity owner;
    protected int age;

    protected Prop(EntityType<?> type, Level level, String animation, int lifetime) {
        super(type, level);
        this.animation = RawAnimation.begin().thenPlayAndHold(animation);
        this.lifetime = lifetime;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.ownerId = owner.getUUID();
    }

    @Nullable
    protected LivingEntity owner() {
        if (this.owner == null && this.ownerId != null && this.level() instanceof ServerLevel server
                && server.getEntity(this.ownerId) instanceof LivingEntity found) {
            this.owner = found;
        }
        return this.owner;
    }

    /** Server-side action on tick {@code t} of the animation. */
    protected abstract void onAge(int t);

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        onAge(this.age);
        if (++this.age >= this.lifetime) {
            this.discard();
        }
    }

    /** Hurts every living thing within {@code radius} not allied to the owner and not yet in {@code struck}. */
    protected void strike(Vec3 center, double radius, float health, DamageSource source, Set<UUID> struck,
            @Nullable java.util.function.Consumer<LivingEntity> onHit) {
        LivingEntity caster = owner();
        AABB box = new AABB(center, center).inflate(radius, 1.5, radius);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e != caster && (caster == null || !caster.isAlliedTo(e)) && !struck.contains(e.getUUID())
                        && e.position().subtract(center).horizontalDistance() <= radius + e.getBbWidth() / 2)) {
            struck.add(victim.getUUID());
            if (victim.hurt(source, health) && onHit != null) {
                onHit.accept(victim);
            }
        }
    }

    protected Set<UUID> fresh() {
        return new HashSet<>();
    }

    protected void burst(ParticleOptions particle, Vec3 at, int count, double spread, double height) {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(particle, at.x, at.y + 0.2, at.z, count, spread, height, spread, 0.05);
        }
    }

    protected void sound(IllagerSound sound, float volume) {
        this.playSound(sound.get(), volume, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> state.setAndContinue(this.animation)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
