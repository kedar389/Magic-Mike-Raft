package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Spawns the original Companions projectile entities so the converted Iron's Spells render with
 * pixel-identical visuals and animations.
 *
 * <p>Ids are verified against {@code CompanionsEntities} in the 1.2.3 jar. Mind the
 * {@code _projectile} suffix: {@code ForgeRegistries.ENTITY_TYPES.getValue()} answers an unknown id
 * with the registry default, a pig, so a wrong id spawns livestock instead of returning null. That is
 * what {@link #create} guards with {@code containsKey}.
 */
public final class CompanionsProjectiles {

    private CompanionsProjectiles() {}

    public static final ResourceLocation TORNADO = new ResourceLocation("companions", "tornado_projectile");
    public static final ResourceLocation STONE_SPIKE = new ResourceLocation("companions", "stone_spike_projectile");
    public static final ResourceLocation HEAL_RING = new ResourceLocation("companions", "heal_ring_projectile");
    public static final ResourceLocation NAGINATA = new ResourceLocation("companions", "holiness_naginata");

    /**
     * The spell that fired this projectile, so {@link com.ironscompanions.event.CompanionDamageEvents}
     * can re-deal the hit through Iron's Spells.
     *
     * <p>Tagging is necessary because {@code BaseProjectile} exposes only a lifetime: each projectile
     * hardcodes its damage from {@code CompanionsConfig} and would ignore the scroll. Carrying the spell
     * rather than a number is what lets the hit go through {@code DamageSources.applyDamage} and pick up
     * spell power, school resistance and the right death message.
     */
    public static final String TAG_SPELL = "irons_companions_spell";

    /** Inscribed level of the casting scroll, for the per-level part of the power curve. */
    public static final String TAG_SPELL_LEVEL = "irons_companions_spell_level";

    /** Spawns at {@code pos} with the owner's rotation and no velocity, untagged. */
    public static Entity spawn(Level level, ResourceLocation id, LivingEntity owner, Vec3 pos) {
        return spawn(level, id, owner, pos, null, 0);
    }

    /** Spawns at {@code pos}, attributing its damage to {@code spell} at {@code spellLevel}. */
    public static Entity spawn(Level level, ResourceLocation id, LivingEntity owner, Vec3 pos,
                               AbstractSpell spell, int spellLevel) {
        Entity entity = create(level, id);
        if (entity == null) {
            return null;
        }
        entity.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), owner.getXRot());
        setOwner(entity, owner);
        tag(entity, spell, spellLevel);
        level.addFreshEntity(entity);
        return entity;
    }

    private static void tag(Entity entity, AbstractSpell spell, int spellLevel) {
        if (spell != null) {
            entity.getPersistentData().putString(TAG_SPELL, spell.getSpellResource().toString());
            entity.getPersistentData().putInt(TAG_SPELL_LEVEL, Math.max(1, spellLevel));
        }
    }

    /**
     * Spawns at {@code from} and launches it toward {@code target}, for Naginata, whose projectile
     * extends {@code ThrownTrident} and needs a real velocity to fall on its mark.
     */
    public static Entity spawnAimed(Level level, ResourceLocation id, LivingEntity owner, Vec3 from, Vec3 target,
                                    float velocity, float inaccuracy, double baseDamage,
                                    AbstractSpell spell, int spellLevel) {
        Entity entity = create(level, id);
        if (entity == null) {
            return null;
        }
        entity.moveTo(from.x, from.y, from.z, owner.getYRot(), owner.getXRot());
        setOwner(entity, owner);
        tag(entity, spell, spellLevel);
        Vec3 delta = target.subtract(from);
        if (entity instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(baseDamage);
            arrow.shoot(delta.x, delta.y, delta.z, velocity, inaccuracy);
        } else if (entity instanceof Projectile projectile) {
            projectile.shoot(delta.x, delta.y, delta.z, velocity, inaccuracy);
        }
        level.addFreshEntity(entity);
        return entity;
    }

    private static Entity create(Level level, ResourceLocation id) {
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            IronsCompanions.LOGGER.error("[IronsCompanions] Companions entity type not registered: {}", id);
            return null;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        return type == null ? null : type.create(level);
    }

    private static void setOwner(Entity entity, LivingEntity owner) {
        if (entity instanceof Projectile projectile) {
            projectile.setOwner(owner);
        }
    }

    /**
     * Highest solid surface at or below {@code startY} at (x, z), so ground effects erupt from the floor
     * rather than hanging in the air. Falls back to {@code startY} when nothing solid is found.
     */
    public static double groundBelow(Level level, double x, double startY, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        for (int y = Mth.floor(startY) + 2; y > level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(blockX, y, blockZ);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return y + 1;
            }
        }
        return startY;
    }
}
