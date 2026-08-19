package com.ironscompanions.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * The spell a Companions projectile was fired by, carried on the projectile itself.
 *
 * <p>Tagging is necessary because {@code BaseProjectile} exposes only a lifetime: each projectile
 * hardcodes its damage from {@code CompanionsConfig} and would ignore the scroll it was cast from.
 * Carrying the spell rather than a number is what lets the hit go through
 * {@code DamageSources.applyDamage} and pick up spell power, school resistance and the right death
 * message, and it lets power be read at impact, so buffs gained mid-flight count.
 *
 * <p>Reading is gated on {@link CompanionsProjectiles#canCarrySpell} before the tag itself is touched.
 * That matters on the damage hot path: {@code Entity#getPersistentData} allocates a {@link CompoundTag}
 * the first time it is called on an entity, and that compound is then saved with it, so probing every
 * entity in a fight would hand persisted NBT to every mob, player and arrow in the world.
 *
 * @param spell the spell that fired the projectile
 * @param level the inscribed level of the casting scroll or book, at least 1
 */
public record SpellTag(AbstractSpell spell, int level) {

    private static final String SPELL_KEY = "irons_companions_spell";
    private static final String LEVEL_KEY = "irons_companions_spell_level";

    /** Marks {@code projectile} as this mod's, fired by {@code spell}. */
    static void write(Entity projectile, AbstractSpell spell, int level) {
        CompoundTag data = projectile.getPersistentData();
        data.putString(SPELL_KEY, spell.getSpellResource().toString());
        data.putInt(LEVEL_KEY, Math.max(1, level));
    }

    /**
     * Reads the tag off a projectile, if it is one of ours and still names a live spell.
     *
     * @return the tag, or null when {@code entity} is not a projectile this mod spawned, carries no tag,
     *         or names a spell that is no longer registered
     */
    public static SpellTag read(Entity entity) {
        if (!CompanionsProjectiles.canCarrySpell(entity.getType())) {
            return null;
        }
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(SPELL_KEY, Tag.TAG_STRING)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(data.getString(SPELL_KEY));
        AbstractSpell spell = id == null ? null : SpellRegistry.getSpell(id);
        if (spell == null || spell.equals(SpellRegistry.none())) {
            return null;
        }
        return new SpellTag(spell, Math.max(1, data.getInt(LEVEL_KEY)));
    }

    /** Damage this spell deals for {@code caster}, at the level inscribed on the casting item. */
    public float power(Entity caster) {
        return spell.getSpellPower(level, caster);
    }
}
