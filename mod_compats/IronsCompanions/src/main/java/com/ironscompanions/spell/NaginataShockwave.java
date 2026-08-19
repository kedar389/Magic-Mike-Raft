package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Splash damage and sparkles for a landing Naginata blade, driven by {@code mixin/HolinessNaginataMixin}
 * from the projectile's own hit methods.
 *
 * <p>Only blades carrying a {@link SpellTag} are touched, so the Sacred Pontiff's own naginatas are
 * unaffected.
 */
public final class NaginataShockwave {

    /** How far from the landing point the shockwave reaches. */
    public static final double SPLASH_RADIUS = 3.0;

    /** Share of the spell's power dealt to everything caught in that radius. */
    private static final float SPLASH_FACTOR = 0.5f;

    private static final int SPAWN_SPARKS = 8;
    private static final int IMPACT_SPARKS = 24;

    private NaginataShockwave() {
    }

    /** Sparkles where a blade appears, before it starts falling. */
    public static void spawnSparkle(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, SPAWN_SPARKS, 0.2, 0.2, 0.2, 0.02);
    }

    /**
     * Shockwave where a blade lands: sparkles, plus splash damage to everything nearby except the owner
     * and whatever the blade hit directly, which takes the full hit through its own damage path.
     *
     * @param blade     the landing projectile
     * @param directHit the entity it struck, or null when it hit the ground
     */
    public static void onImpact(Entity blade, Entity directHit) {
        if (!(blade.level() instanceof ServerLevel level)) {
            return;
        }
        SpellTag tag = SpellTag.read(blade);
        if (tag == null) {
            return;
        }
        Vec3 at = blade.position();
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, IMPACT_SPARKS, 0.6, 0.1, 0.6, 0.12);
        level.sendParticles(ParticleTypes.FLASH, at.x, at.y + 0.2, at.z, 1, 0, 0, 0, 0);

        Entity owner = blade instanceof Projectile projectile ? projectile.getOwner() : null;
        float damage = tag.power(owner) * SPLASH_FACTOR;

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(at, SPLASH_RADIUS * 2, SPLASH_RADIUS * 2, SPLASH_RADIUS * 2));
        for (LivingEntity victim : nearby) {
            if (victim == owner || victim == directHit || !victim.isAlive()) {
                continue;
            }
            // The single-argument damage source leaves the blade out of the picture, so the damage
            // bridge does not recognise this hit as one of ours and re-deal it at full power.
            DamageSources.applyDamage(victim, damage, tag.spell().getDamageSource(owner));
        }
    }
}
