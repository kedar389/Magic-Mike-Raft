package com.primeenchanter;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/** The Prime Enchanter's sound events: Dungeons II FBDBoss Wwise events, one per entry in sounds.json. */
public enum BossSound {
    AMBIENT("ambient"),
    HURT("hurt"),
    DEATH("death"),
    DEATH_LONG("death_long"),
    STEP("step"),
    STEP_FOLEY("step_foley"),
    CAPE("cape"),
    HITMARKER("hitmarker"),
    FORWARD_STRIKE_TELEGRAPH("forward_strike_telegraph"),
    FORWARD_STRIKE_HIT("forward_strike_hit"),
    FURY_FIST_TELEGRAPH("fury_fist_telegraph"),
    FURY_FIST_HIT_1("fury_fist_hit_1"),
    FURY_FIST_HIT_2("fury_fist_hit_2"),
    FURY_FIST_HIT_3("fury_fist_hit_3"),
    FURY_FIST_HIT_3_VOCAL("fury_fist_hit_3_vocal"),
    FURY_FIST_HIT_4("fury_fist_hit_4"),
    FURY_FIST_HIT_4_VOCAL("fury_fist_hit_4_vocal"),
    FURY_FIST_HIT_5("fury_fist_hit_5"),
    FURY_FIST_HIT_5_VOCAL("fury_fist_hit_5_vocal"),
    FURY_FIST_POWER_UP("fury_fist_power_up"),
    FURY_FIST_POWER_UP_VOCAL("fury_fist_power_up_vocal"),
    LAUNCH_TELEPORT_OUT("launch_teleport_out"),
    LAUNCH_TELEPORT_OUT_VOCAL("launch_teleport_out_vocal"),
    LAUNCH_LAND_TELEGRAPH("launch_land_telegraph"),
    LAUNCH_TELEPORT_IN("launch_teleport_in"),
    LAUNCH_TELEPORT_IN_VOCAL("launch_teleport_in_vocal"),
    LIGHTNING_SLAM_START("lightning_slam_start"),
    LIGHTNING_SLAM_START_VOCAL("lightning_slam_start_vocal"),
    LIGHTNING_SLAM_JUMP("lightning_slam_jump"),
    LIGHTNING_SLAM_END("lightning_slam_end"),
    LIGHTNING_SLAM_END_VOCAL("lightning_slam_end_vocal"),
    LIGHTNING_SLAM_RIPPLE("lightning_slam_ripple"),
    LIGHTNING_SLAM_RIPPLE_HIT("lightning_slam_ripple_hit"),
    OVERCHARGE_START("overcharge_start"),
    OVERCHARGE_LOOP("overcharge_loop"),
    OVERCHARGE_END("overcharge_end"),
    OVERCHARGE_FLOOR("overcharge_floor"),
    OVERCHARGE_FLOOR_DAMAGE("overcharge_floor_damage"),
    SUMMON("summon"),
    ICE_MINE_SPAWN("ice_mine_spawn"),
    ICE_MINE_EXPLODE("ice_mine_explode"),
    STAGGER_START("stagger_start"),
    STAGGER_END("stagger_end");

    private final RegistryObject<SoundEvent> event;

    BossSound(String name) {
        this.event = PrimeEnchanterMod.registerSound(name);
    }

    public SoundEvent get() {
        return this.event.get();
    }
}
