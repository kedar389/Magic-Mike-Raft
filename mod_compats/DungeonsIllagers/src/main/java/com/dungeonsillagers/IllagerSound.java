package com.dungeonsillagers;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/** Dungeons II Wwise events of the three illagers and their props, one per entry in sounds.json. */
public enum IllagerSound {
    GEOMANCER_AMBIENT("geomancer", "ambient"),
    GEOMANCER_HURT("geomancer", "hurt"),
    GEOMANCER_DEATH("geomancer", "death"),
    GEOMANCER_SPAWN("geomancer", "spawn"),
    GEOMANCER_CANE("geomancer", "cane"),
    GEOMANCER_PRE_ATTACK("geomancer", "pre_attack"),
    GEOMANCER_SPELL_BOMB("geomancer", "spell_bomb"),
    GEOMANCER_SPELL_WALL("geomancer", "spell_wall"),
    GEOMANCER_BOMB_SPAWN("geomancer", "bomb_spawn"),
    GEOMANCER_BOMB_EXPLODE("geomancer", "bomb_explode"),
    GEOMANCER_PILLAR_APPEAR("geomancer", "pillar_appear"),
    GEOMANCER_PILLAR_SPAWNED("geomancer", "pillar_spawned"),
    GEOMANCER_WALL_SPAWN("geomancer", "wall_spawn"),
    GEOMANCER_PILLAR_HIT("geomancer", "pillar_hit"),
    GEOMANCER_PILLAR_CRUMBLE("geomancer", "pillar_crumble"),
    GEOMANCER_PILLAR_DISAPPEAR("geomancer", "pillar_disappear"),
    GEOMANCER_STUNNED_START("geomancer", "stunned_start"),
    GEOMANCER_STUNNED_END("geomancer", "stunned_end"),
    EXCAVATOR_AMBIENT("excavator", "ambient"),
    EXCAVATOR_HURT("excavator", "hurt"),
    EXCAVATOR_DEATH("excavator", "death"),
    EXCAVATOR_SPAWN("excavator", "spawn"),
    EXCAVATOR_AGGRO("excavator", "aggro"),
    EXCAVATOR_STEP_FOLEY("excavator", "step_foley"),
    EXCAVATOR_PICK_SWING("excavator", "pick_swing"),
    EXCAVATOR_PICKAXE_IMPACT("excavator", "pickaxe_impact"),
    EXCAVATOR_PICK_BREAK_TELEGRAPH("excavator", "pick_break_telegraph"),
    EXCAVATOR_PICK_BREAK_HIT("excavator", "pick_break_hit"),
    EXCAVATOR_BACK_SCRATCH("excavator", "back_scratch"),
    EXCAVATOR_HEAD_SCRATCH("excavator", "head_scratch"),
    EXCAVATOR_SLAP("excavator", "slap"),
    SUPREME_EVOKER_AMBIENT("supreme_evoker", "ambient"),
    SUPREME_EVOKER_HURT("supreme_evoker", "hurt"),
    SUPREME_EVOKER_DEATH("supreme_evoker", "death"),
    SUPREME_EVOKER_STEP("supreme_evoker", "step"),
    SUPREME_EVOKER_SPAWN("supreme_evoker", "spawn"),
    SUPREME_EVOKER_STAGGER_START("supreme_evoker", "stagger_start"),
    SUPREME_EVOKER_STAGGER_END("supreme_evoker", "stagger_end"),
    SUPREME_EVOKER_PROJECTILE_CAST_1("supreme_evoker", "projectile_cast_1"),
    SUPREME_EVOKER_PROJECTILE_CAST_2("supreme_evoker", "projectile_cast_2"),
    SUPREME_EVOKER_PROJECTILE_WHOOSH("supreme_evoker", "projectile_whoosh"),
    SUPREME_EVOKER_PROJECTILE_LOOP("supreme_evoker", "projectile_loop"),
    SUPREME_EVOKER_PROJECTILE_HIT("supreme_evoker", "projectile_hit"),
    SUPREME_EVOKER_SUMMON("supreme_evoker", "summon"),
    SUPREME_EVOKER_SUMMON_VOX("supreme_evoker", "summon_vox"),
    SUPREME_EVOKER_SUMMON_WAVE("supreme_evoker", "summon_wave"),
    SUPREME_EVOKER_SHIELD_ACTIVATE("supreme_evoker", "shield_activate"),
    SUPREME_EVOKER_SHIELD_LOOP("supreme_evoker", "shield_loop"),
    SUPREME_EVOKER_SHIELD_HIT("supreme_evoker", "shield_hit"),
    SUPREME_EVOKER_SHIELD_END("supreme_evoker", "shield_end"),
    SUPREME_EVOKER_TELEPORT_OUT("supreme_evoker", "teleport_out"),
    SUPREME_EVOKER_TELEPORT_OUT_VOX("supreme_evoker", "teleport_out_vox"),
    SUPREME_EVOKER_TELEPORT_IN("supreme_evoker", "teleport_in"),
    SUPREME_EVOKER_TELEPORT_IN_VOX("supreme_evoker", "teleport_in_vox"),
    SUPREME_EVOKER_FANG_TELEGRAPH("supreme_evoker", "fang_telegraph"),
    SUPREME_EVOKER_FANG_CAST("supreme_evoker", "fang_cast"),
    SUPREME_EVOKER_FANG_RISE("supreme_evoker", "fang_rise"),
    SUPREME_EVOKER_FANG_SNAP("supreme_evoker", "fang_snap");

    private final RegistryObject<SoundEvent> event;

    IllagerSound(String mob, String name) {
        this.event = DungeonsIllagersMod.registerSound(mob + "." + name);
    }

    public SoundEvent get() {
        return this.event.get();
    }
}
