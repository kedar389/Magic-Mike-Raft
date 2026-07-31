# Technical Notes

Problems, profiling and pack-wide facts. Player-facing gameplay changes are in [`modpack-wiki.md`](modpack-wiki.md). Per-artifact implementation detail is in that artifact's own README, listed in the [index](#implementation-index).

## Contents

1. [Known Issues](#known-issues)
2. [Performance & Profiling](#performance--profiling)
3. [Attribute audit](#attribute-audit)
4. [Implementation index](#implementation-index)
5. [Resolved](#resolved)

---

# Known Issues

From the 31 Jul 2026 session logs (server 11:29-12:17, client 11:29-13:52). Ordered by impact.

Status: **open**, **testing**, **fixed**, **wontfix** (understood, no action worth taking).

## Server tick debt, open

Five `Can't keep up!` events in a 48-minute session:

| Time | Behind | Ticks |
|---|---|---|
| 11:31:27 | 2,019 ms | 40 |
| 11:38:50 | 2,102 ms | 42 |
| 11:55:14 | 6,674 ms | 133 |
| 12:01:28 | 13,313 ms | 266 |
| 12:02:16 | 32,680 ms | 653 |

This produces the "steady FPS, stuttering image" symptom: the client renders at full framerate while the server sends no entity or position updates.

`Running Nms behind` is cumulative tick debt against wall clock, not a single stall. 6.6 s, 13.3 s, 32.7 s is one continuous degradation from ~11:55 that never recovered. The first event, 16 s after `Done`, is startup.

AllTheLeaks was investigated and ruled out. Its leak scan runs on the Server thread every `logIntervalInMinutes` (10) and its output landed 0.8 s before the 32-second event. Against that:

- Three of four scans (11:41:20, 11:51:20, 12:12:13) produced no lag.
- The scan near the freeze fired 55 s late, while every other interval is 9m58s to 10m00s. The task is scheduled in ticks (`interval × 60 × 20`), so a server behind on ticks fires it late in wall-clock.
- A periodic 10-minute task cannot produce a 7-minute continuous ramp.

`logIntervalInMinutes: 0` is not a safe off switch. The period is `interval × 60 × 20` ticks with no zero-guard on that path, so `0` may schedule the scan every tick. Rename the jar to `.jar.disabled` to disable it.

The 11:55-12:02 window contains: DH Update Propagator threads active at 11:55:23; C2ME workers running `JigsawPlacement`, ~21 warnings inside 0.1 s at 11:58:41; `Kedar420 moved too quickly` at 11:56:27, which is a symptom of the lag.

Current theory is chunk generation: C2ME placing structures plus DH's server-side distant generation, both generating terrain while the player explores. Unconfirmed.

Next step: run `/spark profiler --timeout 300` while the lag is happening.

## Distant Horizons LOD database, open

`world/data/DistantHorizons.sqlite` is 5.3 GiB for the overworld alone, with separate databases for the Nether, End and the Iron's Spellbooks pocket dimension.

It still holds Tinkers' Construct blocks that no longer exist. The client logs `Unable to find BlockState for Block [tconstruct:...]` on DH render-loader threads and takes the fallback path for every affected LOD chunk. Clearing the caches drops the dead references and the bulk, at the cost of regeneration time.

## illageandspillage entities have no attributes, open

690 server lines (345 × `illageandspillage:bone`, 345 × `illageandspillage:axe`) and 1,150 client lines. Both entities are registered without an attribute supplier. Cosmetic, but it dominates both logs.

## medievalorigins missing dependencies, open

11 origins reference content from mods that are not installed, so they load with those powers skipped and are weaker than intended. Affected: `high_elf`, `plague_victim`, `revenant`, `banshee`, `gorgon`, `moon_elf`, `yeti`, `siren`, `incubus`, `ogre`, `troll`.

- Powers: `origins_survive:*`, `thermoo-patches:*`, `origins_toughasnails:*`, `origins_env_deh:*`, `mermod:fabulous_fins`, `survive:cold_blooded`
- Attributes: `ars_nouveau:ars_nouveau.perk.max_mana`, `ars_nouveau:*.perk.mana_regen`, `combatroll:count`, `spell_power:healing`
- Entities/blocks: `iceandfire:siren`, `tradingpost:trading_post`

This is the only issue in this list that changes gameplay.

## generic.attack_damage on mobs that lack it, open

47 client warnings, all the same attribute, across mob types that have none by default: Horse 11, Parrot 9, Chicken 8, Villager 7, Pig 6, SkeletonHorse 3, WanderingTrader 1.

The server sends it in `ClientboundUpdateAttributesPacket` and the client's copy of the entity has no slot for it. A mod present on both sides would register the attribute on both, so the asymmetry means a server-only mod is attaching it.

Suspect: MobControl, which is server-only and whose rule schema has `set.damage`, `set.health`, `set.knockback`. Unconfirmed, because `config/mobcontrol.toml` holds only the shipped documentation example, whose `name="mod:mob"` matches nothing. The client logs the warning and drops the attribute.

## Broken assets and data, open

| Source | Count | Problem |
|---|---|---|
| cluttered | 224 | `Exception loading blockstate definition`, those blocks render as missing models |
| farmersrespite | 180 + 8 | `Unknown recipe category` (kettle_pouring, brewing), missing kettle textures |
| aquamirae_delight | 36 | Missing textures in model |
| netherdepthsupgrade | 32 | Missing textures in model |
| cataclysm | 24 | `Unknown recipe category` (weapon_fusion) |
| oceansdelight | 20 | Missing textures in model |
| companions | 12 | `Unknown recipe category` (soul_furnace) |

Plus 69 loot-table problems server-side: 33 unparseable data files, 29 validation problems, 7 Forge element errors. Three are `Unknown enchantment` (`minecraft:efficiency` ×2, `minecraft:sharpness` ×1) affecting `minecraft:chests/illager_mansion/wool`, `betterarcheology:archeology/sword_loot` and `raidsenhanced:entities/raid_drill`. These also fire on the client in the singleplayer world-list screen, so they are unrelated to the server world or the FML registry wipe. Those tables do not load.

## EMF model-creation cap, wontfix

Entity Model Features stops handling a model after 64 creation attempts, which means something rebuilds it per-render instead of caching. Seven models across four mods hit the cap: `born_in_chaos_v1:modelspiritguidesombrero`, `cataclysm:aptrgangr_head_model`, `cataclysm:draugr_head_model`, `cataclysm:kobolediator_head_model`, `irons_spellbooks:pyrium_staff_head`, `irons_spellbooks:pyrium_staff_orb`, `minecraft:player_head`. Those models lose EMF support for the session. The repeated allocation is minor GC pressure.

## Structurify config sync exceeds packet limit, wontfix

`/structurify config sync fromServer` throws `EncoderException: String too big (was 40849 characters, max 32767)`. Structurify serialises its whole structure config into one string field, and this pack exceeds Forge's `FriendlyByteBuf` string cap by ~25%. Worldgen is server-side so structures still generate, only the client-side config view is lost. No config knob avoids it.

## Stale JVM crash log, wontfix

`hs_err_pid20432.log` and `replay_pid20432.log` in the server folder are an `EXCEPTION_ACCESS_VIOLATION` from 23 Jul 2026, unrelated to anything above. Safe to delete.

---

# Performance & Profiling

The two performance settings that belong to no datapack or compat mod.

## spark, background profiler off

spark enables its background profiler by default on dedicated servers, sampling continuously from boot. On Windows the low-overhead engine is unavailable: the log confirms `The async-profiler engine is not supported for your os/arch (windows10/amd64), so the built-in Java engine will be used instead`, leaving the JVM thread-dump sampler, which costs more.

[`mod_configs_server/spark.json`](../mod_configs_server/spark.json) sets `backgroundProfiler: false`. On-demand `/spark profiler` still works.

The key must be present and explicitly `false`. spark treats a missing key as enabled on servers (`didEnableByDefault`), so deleting the line re-enables it. There is no JVM flag equivalent.

## Distant Horizons, server-side distant generation

The server runs DH with `enableDistantGeneration = true`, generating LOD chunks off-thread. Threading in [`mod_configs_server/DistantHorizons.toml`](../mod_configs_server/DistantHorizons.toml):

| Setting | Value | Effect |
|---|---|---|
| `common.multiThreading.numberOfThreads` | `4` | Was `12`. 12 full-duty world-gen threads starved the server tick thread and C2ME |
| `common.multiThreading.threadRunTimeRatio` | `0.5` | Was `1.0`. Each thread idles half the time, so gen bursts stop causing tick stutter |

Set `enableDistantGeneration = false` to stop the server pre-generating LODs. Clients then build their own as they explore.

---

# Attribute audit

Pack-wide findings. How the merge is implemented is in the three READMEs in the [index](#implementation-index).

Apothic Attributes (`attributeslib:*`) is the canonical set. It owns the Attributes GUI, the tooltips and the combat formulas in `config/attributeslib.cfg` (stock, not tracked).

Every attribute registrar in the pack: Apothic Attributes, Iron's Lib, Iron's Spellbooks, Obscure API, Cataclysm, Geomancy spell lib, Geomancy Plus, Caelus. No other mod registers one.

## Every "critical damage" attribute

Registered base / min / max, read out of the installed jars:

| GUI label | Attribute | Base | Min | Max | Status |
|---|---|---|---|---|---|
| Crit Damage | `attributeslib:crit_damage` | 1.5 | 1.0 | 100 | canonical |
| Critical Damage | `irons_lib:crit_damage` | 1.0 | 0.0 | 1024 | remapped by datapack, hidden |
| Critical Damage | `obscure_api:critical_damage` | 2.0 | 0.0 | 100 | merged by the mod, hidden |
| % Critical Damage | `cataclysm:additional_critical_damage` | 0.0 | −512 | 512 | merged by the mod ×0.01, hidden |
| Spell Critical Damage | `gtbcs_spell_lib:spell_crit_damage` | 1.0 | 1.0 | 10 | spell crits only |

## Which mods grant these stats

| Attribute family | Granted by |
|---|---|
| Apothic `attributeslib:*` | Unique Accessories: armor pierce, arrow damage, arrow velocity, crit chance, crit damage, dodge chance, draw speed, mining speed. Already canonical |
| Obscure API `obscure_api:*` | Aquamirae (accuracy, critical damage, critical hit, penetration), Aquamirae Delight (critical hit) |
| Cataclysm `cataclysm:*` | Cataclysm itself: The Annihilator (`ADDITION 75.0`) and The Immolator (`ADDITION 60.0`) for crit damage, `nature_heal` via armor tiers and a food mixin |
| Iron's Lib `irons_lib:*` | Nothing. No installed mod grants them. Iron's Spellbooks' only `irons_lib:` references are a particle texture and a worldgen processor. Hidden, with the datapack remap kept as a safety net |

## Dodge chance is not one attribute

Player-facing numbers are in [the wiki](modpack-wiki.md#dodge-chance). Medieval Origins' Moon Elf "Nimble" is not an attribute but `origins:modify_damage_taken` (`multiply_total_multiplicative −2.0`) gated on predicate `medievalorigins:dodge_chance` = `random_chance 0.2`, so it is invisible to the GUI and rolls independently of the Apothic stat.

Folding it into `attributeslib:dodge_chance` as an `origins:attribute` power would reduce its coverage: Apothic's dodge only applies to melee damage from entities in range, while the power negates any damage type.

Unique Accessories belt amounts are configurable in `config/uniqueaccessories-common.toml` (stock values).

`irons_lib:dodge_chance` and `obscure_api:dodge` are registered and their handlers work, but nothing in the pack grants them.

---

# Implementation index

Each of these is documented once, in its own README.

| Change | Documented in |
|---|---|
| Iron's Lib attribute remap, 7 stats, datapack | [`datapacks/Attribute_Merge/README.md`](../datapacks/Attribute_Merge/README.md) |
| Cataclysm and Obscure API merge, and why a code hook was needed | [`mod_compats/AttributeMerge/README.md`](../mod_compats/AttributeMerge/README.md) |
| Source-mod labels on attribute names | [`resourcepacks/Attribute_Mod_Labels/README.md`](../resourcepacks/Attribute_Mod_Labels/README.md) |
| RealmRPG jigsaw pools resolving without their namespace | [`datapacks/RealmRPG_Pool_Namespace_Fix/README.md`](../datapacks/RealmRPG_Pool_Namespace_Fix/README.md) |
| Eyes of Ender redistributed to bosses and chests | [`datapacks/Eyes_Of_Ender_Drops/README.md`](../datapacks/Eyes_Of_Ender_Drops/README.md) |
| Per-mod game rules | [`datapacks/Server_Game_Rules/README.md`](../datapacks/Server_Game_Rules/README.md) |
| Curios slot changes | [`datapacks/Curios_Slot_Tweaks/README.md`](../datapacks/Curios_Slot_Tweaks/README.md) |
| KubeJS scripts and deploy | [`kubejs/README.md`](../kubejs/README.md) |
| Iron's Companions content/compat mod | [`mod_compats/IronsCompanions/README.md`](../mod_compats/IronsCompanions/README.md) |
| Adding and updating mods, packwiz workflow | [`COMMANDS.md`](COMMANDS.md) |

Both compat mods ship unbuilt. There is no JDK in the dev environment.

---

# Resolved

## Server crash on every mob death, fixed 30 Jul 2026

`NullPointerException: Registry Object not present: majruszsenchantments:immortality`, thrown from Majrusz's Immortality death listener on every mob death.

Forge RegistryObjects were left unbound by the registry remap that ran on the first world load after Tinkers' Construct was removed (1,304 missing entries deleted, 20 `ascendant_arcana` enchantments logged `Object did not get ID it asked for`). Majrusz Library caches Forge's `RegistryObject::get` as its value supplier, so an unbound handle throws on every `EnchantmentHelper.has()` call.

Fixed by stripping the root-level `fml` compound from `world/level.dat`, forcing Forge to rebuild registry IDs from the current modlist with no remap. Verified after reboot: all 24 majrusz enchantments present, zero NPEs, zero missing-entry warnings, zero ID remaps.

Inspect `level.dat` by walking the NBT, not by string-grepping the decompressed bytes. Registry names live inside `{K:name, V:id}` compounds and anchored greps miss entries at random. That mistake produced a wrong "7 enchantments are missing" diagnosis during this investigation.
