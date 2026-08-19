package com.ironscompanions.damage;

import dev.xylonity.companions.registry.CompanionsEntities;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.util.Lazy;

/**
 * Which Iron's Spells school each Companions attacker deals its damage in.
 *
 * <p>The table names Companions' own {@link CompanionsEntities} registry objects, so it is checked by
 * the compiler: an entity that upstream renames or drops fails the build instead of quietly falling out
 * of the mapping. It is resolved once into {@link EntityType} keys, because
 * {@link CompanionsDamageBridge} consults it on every hit dealt anywhere in the world, and a resolved
 * lookup is one hash probe on an identity-hashed key.
 *
 * <p>Note there is no {@code his_holiness} or {@code sacred_pontiff_invisible} entity: those are model
 * and phase names inside {@code SacredPontiffEntity}, so {@code SACRED_PONTIFF} covers both phases.
 */
final class CompanionsSchools {

    /** Mobs whose own attacks are retyped. */
    private static final List<Attacker> MOBS = List.of(
            new Attacker(CompanionsEntities.SACRED_PONTIFF, SchoolRegistry.HOLY_RESOURCE),
            new Attacker(CompanionsEntities.SHADE_SWORD, SchoolRegistry.BLOOD_RESOURCE),
            new Attacker(CompanionsEntities.SHADE_MAW, SchoolRegistry.BLOOD_RESOURCE),
            new Attacker(CompanionsEntities.DINAMO, SchoolRegistry.LIGHTNING_RESOURCE),
            new Attacker(CompanionsEntities.BROKEN_DINAMO, SchoolRegistry.LIGHTNING_RESOURCE),
            new Attacker(CompanionsEntities.ILLAGER_GOLEM, SchoolRegistry.LIGHTNING_RESOURCE),
            new Attacker(CompanionsEntities.EMBER_POLE, SchoolRegistry.FIRE_RESOURCE),
            new Attacker(CompanionsEntities.LIVING_CANDLE, SchoolRegistry.FIRE_RESOURCE),
            new Attacker(CompanionsEntities.FIREWORK_TOAD, SchoolRegistry.FIRE_RESOURCE),
            new Attacker(CompanionsEntities.NETHER_BULLFROG, SchoolRegistry.FIRE_RESOURCE),
            new Attacker(CompanionsEntities.ENDER_FROG, SchoolRegistry.ENDER_RESOURCE));

    /** Projectiles: the Sacred Pontiff's attacks, and both Crystallized Blood weapons. */
    private static final List<Attacker> PROJECTILES = List.of(
            new Attacker(CompanionsEntities.PONTIFF_FIRE_RING, SchoolRegistry.FIRE_RESOURCE),
            new Attacker(CompanionsEntities.HOLINESS_STAR, SchoolRegistry.HOLY_RESOURCE),
            // Crystallized Blood SWORD, and the AXE ("Blood Vortex").
            new Attacker(CompanionsEntities.BLOOD_SLASH_PROJECTILE, SchoolRegistry.BLOOD_RESOURCE),
            new Attacker(CompanionsEntities.BLOOD_TORNADO_PROJECTILE, SchoolRegistry.BLOOD_RESOURCE),
            new Attacker(CompanionsEntities.SHADE_SWORD_IMPACT_PROJECTILE, SchoolRegistry.BLOOD_RESOURCE));

    /**
     * Both tables merged and resolved on first use, because {@code RegistryObject#get} throws until
     * registration is done. Merging is safe: no entity appears in both lists.
     */
    private static final Lazy<Map<EntityType<?>, ResourceLocation>> BY_TYPE =
            Lazy.concurrentOf(CompanionsSchools::resolve);

    private CompanionsSchools() {
    }

    /**
     * The school for a hit, preferring what struck the target over who fired it, so a Pontiff fireball
     * is fire even though the Pontiff itself is holy.
     *
     * @return the school id, or null when neither entity is one of ours
     */
    static ResourceLocation schoolFor(Entity direct, Entity attacker) {
        Map<EntityType<?>, ResourceLocation> byType = BY_TYPE.get();
        if (direct != null) {
            ResourceLocation school = byType.get(direct.getType());
            if (school != null) {
                return school;
            }
        }
        return attacker == null ? null : byType.get(attacker.getType());
    }

    private static Map<EntityType<?>, ResourceLocation> resolve() {
        Map<EntityType<?>, ResourceLocation> byType = new HashMap<>(MOBS.size() + PROJECTILES.size());
        MOBS.forEach(attacker -> byType.put(attacker.type().get(), attacker.school()));
        PROJECTILES.forEach(attacker -> byType.put(attacker.type().get(), attacker.school()));
        return Map.copyOf(byType);
    }

    /**
     * @param type   a Companions entity type, as its own registry object
     * @param school the Iron's Spells school its damage is retyped into
     */
    private record Attacker(Supplier<? extends EntityType<?>> type, ResourceLocation school) {
    }
}
