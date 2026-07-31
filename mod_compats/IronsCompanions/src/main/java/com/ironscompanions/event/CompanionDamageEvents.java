package com.ironscompanions.event;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Phase 3, retypes Companions ability damage into the matching Iron's Spells magic school, so it
 * respects IS spell resistances and reads as real magic (e.g. Pontiff = holy, Crystallized Blood
 * sword/axe = blood). We re-deal the hit under the IS damage type {@code irons_spellbooks:<school>_magic};
 * IS's own resist handling then applies natively. A ThreadLocal guards against recursion.
 *
 * <p>VERIFY(build): every entity id below (companions:*) and the damage-type id pattern
 * ({@code <school>_magic}, see {@code io.redspace.ironsspellbooks.damage.ISSDamageTypes}).
 */
public class CompanionDamageEvents {

    private static final ThreadLocal<Boolean> RETYPING = ThreadLocal.withInitial(() -> false);

    /** Companions entity id (source or direct) -> IS school name. */
    private static final Map<ResourceLocation, String> ENTITY_SCHOOL = Map.ofEntries(
            e("sacred_pontiff", "holy"),
            e("sacred_pontiff_invisible", "holy"),
            e("his_holiness", "holy"),
            e("shade_sword", "blood"),
            e("shade_maw", "blood"),
            e("dinamo", "lightning"),
            e("broken_dinamo", "lightning"),
            e("illager_golem", "lightning"),
            e("ember_pole", "fire"),
            e("living_candle", "fire"),
            e("firework_toad", "fire"),
            e("nether_bullfrog", "fire"),
            e("ender_frog", "ender"));

    /** Companions projectile id -> IS school (covers Pontiff fire ring + the Crystallized Blood weapons). */
    private static final Map<ResourceLocation, String> PROJECTILE_SCHOOL = Map.ofEntries(
            e("pontiff_fire_ring", "holy"),
            e("holiness_star", "holy"),
            e("holiness_naginata", "holy"),
            e("blood_slash", "blood"), // Crystallized Blood SWORD ability
            e("blood_tornado", "blood"), // Crystallized Blood AXE ability ("Blood Vortex")
            e("shade_sword_impact", "blood"));

    private static Map.Entry<ResourceLocation, String> e(String path, String school) {
        return Map.entry(new ResourceLocation("companions", path), school);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || RETYPING.get()) {
            return;
        }
        DamageSource src = event.getSource();
        if (isAlreadyMagic(src)) {
            return; // never double-type native IS damage
        }
        String school = schoolFor(src);
        if (school == null) {
            return;
        }
        float amount = event.getAmount();
        event.setCanceled(true);
        RETYPING.set(true);
        try {
            DamageSource magic = buildSchoolDamage(target.level(), school, src.getEntity(), src.getDirectEntity());
            target.hurt(magic, amount);
        } finally {
            RETYPING.set(false);
        }
    }

    private static String schoolFor(DamageSource src) {
        Entity direct = src.getDirectEntity();
        if (direct != null) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(direct.getType());
            String s = PROJECTILE_SCHOOL.get(id);
            if (s == null) {
                s = ENTITY_SCHOOL.get(id);
            }
            if (s != null) {
                return s;
            }
        }
        Entity attacker = src.getEntity();
        if (attacker != null) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType());
            return ENTITY_SCHOOL.get(id);
        }
        return null;
    }

    private static boolean isAlreadyMagic(DamageSource src) {
        return src.typeHolder()
                .unwrapKey()
                .map(k -> k.location().getNamespace().equals("irons_spellbooks"))
                .orElse(false);
    }

    /**
     * Builds a damage source carrying the IS school damage type. Because the type sits in IS's
     * magic-resist damage-type tags, IS applies the target's resistance automatically.
     *
     * <p>VERIFY(build): prefer IS's own {@code SpellDamageSource}/{@code DamageSources} factory if it
     * also wants a "spell" flag for crit/absorb interplay. The plain typed DamageSource below is the
     * minimum that triggers IS resist. TODO: scale {@code amount} by the owner's matching spell-power
     * attribute for owned companions (README 5D).
     */
    private static DamageSource buildSchoolDamage(Level level, String school, Entity attacker, Entity direct) {
        ResourceKey<DamageType> typeKey =
                ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("irons_spellbooks", school + "_magic"));
        Holder<DamageType> holder =
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(typeKey);
        return new DamageSource(holder, direct, attacker);
    }
}
