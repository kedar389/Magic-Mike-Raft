package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import dev.xylonity.companions.common.entity.BaseProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Central place to reference and spawn the original Companions projectile/effect entities, so the
 * converted Iron's Spells render with pixel-identical visuals and animations.
 *
 * <p><b>VERIFY the entity registration ids below.</b> The projectile CLASSES are
 * {@code dev.xylonity.companions.common.entity.projectile.{TornadoProjectile, StoneSpikeProjectile,
 * HealRingProjectile, HolinessNaginataProjectile}}, but their registered ids are only in the
 * Companions init class. Confirm from source: https://github.com/Xylonity/Companions
 * (look in the entity {@code DeferredRegister}). Update the four constants once confirmed.
 */
public final class CompanionsProjectiles {

    private CompanionsProjectiles() {}

    public static final ResourceLocation TORNADO = new ResourceLocation("companions", "tornado");          // VERIFY
    public static final ResourceLocation STONE_SPIKE = new ResourceLocation("companions", "stone_spike");  // VERIFY
    public static final ResourceLocation HEAL_RING = new ResourceLocation("companions", "heal_ring");      // VERIFY
    public static final ResourceLocation NAGINATA = new ResourceLocation("companions", "holiness_naginata"); // VERIFY

    /**
     * Spawns a Companions projectile entity at {@code pos}, facing the owner's look direction.
     *
     * <p>TODO(build): once the entity ids are confirmed, wire the owner + damage. The Companions
     * projectiles carry their own owner/damage fields (set by the book/Soul Mage that spawns them).
     * Either reference the concrete class and set them directly, or reflect. Until then the damage
     * is left at the entity default and will be retyped/scaled by the school-damage event layer
     * (Phase 3, see README 5D).
     */
    public static Entity spawn(Level level, ResourceLocation id, LivingEntity owner, Vec3 pos) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null) {
            IronsCompanions.LOGGER.error("[IronsCompanions] Companions entity type not found: {} (fix id in CompanionsProjectiles)", id);
            return null;
        }
        Entity entity = type.create(level);
        if (entity == null) {
            return null;
        }
        entity.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), owner.getXRot());
        // All Companions projectiles extend BaseProjectile, which carries the owner (used for
        // damage attribution + friendly-fire). VERIFY(build): setOwner(LivingEntity) signature.
        if (entity instanceof BaseProjectile projectile) {
            projectile.setOwner(owner);
        }
        level.addFreshEntity(entity);
        return entity;
    }
}
