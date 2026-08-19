package com.ironscompanions.spell;

import dev.xylonity.companions.registry.CompanionsEntities;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

/**
 * Spawns Companions' own projectile entities for the converted spells, so the entity classes, models,
 * animations and hit behaviour stay Companions'. Each spawn does what Companions does inline at its own
 * spawn sites — create, setOwner, addFreshEntity — plus the {@link SpellTag} that makes damage follow
 * the casting spell instead of {@code CompanionsConfig}. There is no API to call for this.
 *
 * <p>Entity types come from {@link CompanionsEntities} on the compile classpath, so an upstream rename
 * is a build error. Resolving them by id would not be: {@code ForgeRegistries.ENTITY_TYPES.getValue()}
 * answers an unknown id with the registry default, a pig.
 */
public final class CompanionsProjectiles {

    /**
     * The projectiles this mod spawns, and so the only types that can carry a {@link SpellTag}. Resolved
     * on first use, because {@code RegistryObject#get} throws until registration is done.
     */
    private static final List<Supplier<? extends EntityType<?>>> SPELL_CARRIERS = List.of(
            CompanionsEntities.TORNADO_PROJECTILE,
            CompanionsEntities.STONE_SPIKE_PROJECTILE,
            CompanionsEntities.HEAL_RING_PROJECTILE,
            CompanionsEntities.HOLINESS_NAGINATA);

    private static final Lazy<Set<EntityType<?>>> CARRIER_TYPES = Lazy.concurrentOf(() ->
            SPELL_CARRIERS.stream().map(Supplier::get).collect(Collectors.toUnmodifiableSet()));

    /** How far below the requested height {@link #groundBelow} will look for a floor. */
    private static final int GROUND_SEARCH_DEPTH = 12;

    private CompanionsProjectiles() {
    }

    /** Whether {@code type} may carry a {@link SpellTag}. One identity-hash probe, for the damage hot path. */
    public static boolean canCarrySpell(EntityType<?> type) {
        return CARRIER_TYPES.get().contains(type);
    }

    /** Spawns at {@code pos} with the owner's rotation and no velocity, carrying no spell. */
    public static Entity spawn(Level level, Supplier<? extends EntityType<?>> type, LivingEntity owner, Vec3 pos) {
        return spawn(level, type, owner, pos, null, 0);
    }

    /** Spawns at {@code pos}, attributing its damage to {@code spell} at {@code spellLevel}. */
    public static Entity spawn(Level level, Supplier<? extends EntityType<?>> type, LivingEntity owner, Vec3 pos,
                               AbstractSpell spell, int spellLevel) {
        Entity entity = create(level, type, owner, pos, spell, spellLevel);
        if (entity == null) {
            return null;
        }
        level.addFreshEntity(entity);
        return entity;
    }

    /**
     * Spawns at {@code from} and launches it toward {@code target}, for Naginata: its projectile extends
     * {@code ThrownTrident} and needs a real velocity to fall on its mark.
     */
    public static Entity spawnAimed(Level level, Supplier<? extends EntityType<?>> type, LivingEntity owner,
                                    Vec3 from, Vec3 target, float velocity, float inaccuracy, double baseDamage,
                                    AbstractSpell spell, int spellLevel) {
        Entity entity = create(level, type, owner, from, spell, spellLevel);
        if (entity == null) {
            return null;
        }
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

    /** Builds, places, owns and tags a projectile, without adding it to the level yet. */
    private static Entity create(Level level, Supplier<? extends EntityType<?>> type, LivingEntity owner, Vec3 pos,
                                 AbstractSpell spell, int spellLevel) {
        Entity entity = type.get().create(level);
        if (entity == null) {
            return null;
        }
        entity.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), owner.getXRot());
        if (entity instanceof Projectile projectile) {
            projectile.setOwner(owner);
        }
        if (spell != null) {
            SpellTag.write(entity, spell, spellLevel);
        }
        return entity;
    }

    /**
     * Highest solid surface at or below {@code startY}, so ground effects erupt from the floor rather
     * than hanging in the air. Gives up after {@link #GROUND_SEARCH_DEPTH} blocks and falls back to
     * {@code startY}, which bounds the cost when a spell is cast over a ravine.
     */
    public static double groundBelow(Level level, double x, double startY, double z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(Mth.floor(x), 0, Mth.floor(z));
        int top = Mth.floor(startY) + 2;
        int bottom = Math.max(level.getMinBuildHeight(), top - GROUND_SEARCH_DEPTH);
        for (int y = top; y > bottom; y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            // isAir first: it is a cached flag on the state, and skips building a shape for the common case.
            if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) {
                return y + 1;
            }
        }
        return startY;
    }
}
