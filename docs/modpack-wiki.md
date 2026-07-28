# Magic Minecraft Server — Modpack Wiki

A reference of the deliberate **server-side** config in this modpack: what is set, **where** the setting lives, and (for mobs) **why**. This page is the single index for "is X changed, and how?" — progress/tasks are tracked in [`TODO.MD`](../TODO.MD), not here. For the full list of biomes that generate on this server, see [`biomes.md`](biomes.md).

## How config works here

- **Source of truth = this repo.** Curated server configs live in [`mod_configs_server/`](../mod_configs_server). Only files we've intentionally customized are tracked — not the full config dump.
- **Live installs** in `servers_folder/` are **untracked**. Changes are made in the repo first, then copied to the live server. See [[mirror-instance-changes-to-repo]].
- Different behaviors are controlled by different systems — a mod's own config, vanilla/Forge **game rules** (datapack), or the **Mob Control** mod. Each section notes which.

## Contents

1. [Banned Mobs](#banned-mobs)
2. [Game Rules](#game-rules)
3. [Mob Biome Spawns](#mob-biome-spawns)
4. [Loot Tables](#loot-tables)
5. [Mob Drops](#mob-drops)
6. [Attributes](#attributes)
7. [Waystones](#waystones)

---

## Banned Mobs

### Alex's Mobs

Disabled via each mob's `<mob>SpawnWeight = 0` in [`mod_configs_server/alexsmobs.toml`](../mod_configs_server/alexsmobs.toml) (`0 = disable spawn`). Leafcutter ants are world-gen anthills, disabled via `leafcutterAnthillSpawnChance = 0.0`.

| Mob | Reason | Setting |
|---|---|---|
| Fly | Swarm lag, no value | `flySpawnWeight = 0` |
| Cockroach | Breeds rapidly → entity lag | `cockroachSpawnWeight = 0` |
| Mungus | Spreads mushrooms; clutter | `mungusSpawnWeight = 0` |
| Farseer | World-border ranged pest | `farseerSpawnWeight = 0` |
| Enderiophage | Multiplies & teleports → End chaos | `enderiophageSpawnWeight = 0` |
| Guster | Sandstorm particles + ranged sand attacks | `gusterSpawnWeight = 0` |
| Leafcutter Ant | Documented TPS killer (AlexsMobs #382) | `leafcutterAnthillSpawnChance = 0.0` |
| Anaconda | Large aggressive water snake | `anacondaSpawnWeight = 0` |
| Anteater | Owner preference | `anteaterSpawnWeight = 0` |
| Triops | Overpopulates water | `triopsSpawnWeight = 0` |
| Banana Slug | Clutter / owner preference | `bananaSlugSpawnWeight = 0` |
| Terrapin | Overpopulates coasts | `terrapinSpawnWeight = 0` |
| Flying Fish | Overpopulates water | `flyingFishSpawnWeight = 0` |
| Lobster | Overpopulates coasts | `lobsterSpawnWeight = 0` |
| Comb Jelly | Overpopulates water | `combJellySpawnWeight = 0` |
| Tiger | Owner preference | `tigerSpawnWeight = 0` |
| Gazelle | Owner preference (very high spawn weight) | `gazelleSpawnWeight = 0` |
| Blobfish | Clutter (high spawn weight) | `blobfishSpawnWeight = 0` |
| Underminer | Tunneling illager, destructive | `underminerSpawnWeight = 0` |

_Sources: [AlexsMobs #382 (Leafcutter TPS lag)](https://github.com/Alex-the-666/AlexsMobs/issues/382), [SteamPunk #83 (fish/lobster/terrapin overpopulation)](https://github.com/LunaPixelStudios/SteamPunk/issues/83)._

### Born in Chaos

Born in Chaos has no config file — spawns are disabled via its custom game rules
(set in the [Game Rules](#game-rules) datapack). Natural spawning is turned off for:

| Mob | Reason | Rule (off) |
|---|---|---|
| Corpse Fly | Swarm clutter / lag | `corpseFlySpawn` |
| Bloody Gadfly | Swarm clutter / lag | `bloodyGadflySpawn` |
| Door Knight | Owner preference | `doorKnightSpawn` |

Also off: **Maggots** from corpses on death (`maggotsAppearance`) — a death effect rather than a natural spawn. See [Game Rules → Born in Chaos](#born-in-chaos-1) for the full rule list.

---

## Game Rules

Datapack: [`datapacks/Server_Game_Rules/`](../datapacks/Server_Game_Rules/) (namespace `server_gamerules`), one function file per mod. See [[server-gamerules-datapack-convention]].

### Born in Chaos

`born_in_chaos.mcfunction`:

| Rule | State |
|---|-------|
| `disappearanceofSpiritsUndertheSun` | on    |
| `maggotsAppearance` | off   |
| `corpseFlySpawn` | off   |
| `bloodyGadflySpawn` | off   |
| `doorKnightSpawn` | off   |
| `soulStratificationEffect` | on    |
| `theappearanceoftheNightmareStalker` | on    |
| `generationofInfectedDiamonds` | on    |
| `naughtinessMechanics` | on    |

---

## Mob Biome Spawns

Spawn-rate and biome tuning that isn't a full ban.

| Mob | Change | Setting |
|---|---|---|
| Catfish | Reduced spawn rate (was 4) | `catfishSpawnWeight = 2` in [`mod_configs_server/alexsmobs.toml`](../mod_configs_server/alexsmobs.toml) |

---

## Loot Tables

### End Remastered eyes — vanilla chests

Datapack [`datapacks/eyes_of_ender_drops/`](../datapacks/eyes_of_ender_drops/) (see its README for the full scheme). Eyes injected into vanilla chest loot:

| Eye | Chest | Chance |
|---|---|---|
| `cursed_eye` | Bastion treasure | 50% |
| `black_eye` | Buried treasure | 30% |
| `black_eye` | Shipwreck treasure | 5% |
| `corrupted_eye` | Pillager outpost | 30% |
| `magical_eye` | Woodland mansion | 10% |

Default End Remastered chest sources (mineshaft, desert pyramid, igloo, jungle temple, nether bridge, simple dungeon) are **disabled** — those eyes now come from bosses (below).

---

## Mob Drops

### End Remastered eyes — mobs & bosses

Datapack [`datapacks/eyes_of_ender_drops/`](../datapacks/eyes_of_ender_drops/).

**Boss drops (guaranteed):**

| Eye | Boss | Mod |
|---|---|---|
| `old_eye` | Ancient Remnant | Cataclysm |
| `lost_eye` | The Harbinger | Cataclysm |
| `nether_eye` | Netherite Monstrosity | Cataclysm |
| `exotic_eye` | Coralssus | Cataclysm (only source; recipe removed) |
| `cold_eye` | Lich | Bosses of Mass Destruction (player kill) |
| `rogue_eye` | Sculptor | Mowzie's Mobs (player kill) |
| `undead_soul` (→ craft `undead_eye`) | Maledictus | Cataclysm |

**Injected into vanilla mob drops:**

| Eye / item | Mob | Chance |
|---|---|---|
| `guardian_eye` | Elder Guardian | 30% |
| `magical_eye` | Evoker | 5% |
| `wither_eye` | Wither | 90% ×2 rolls |
| `witch_pupil` (→ craft `witch_eye`) | Witch | 10% |

Crafted eyes: `undead_eye` (shaped) and `witch_eye` (`ender_eye` + `witch_pupil`, shapeless).

---

## Attributes

### Duplicate attributes merged into Apothic Attributes

Datapack [`datapacks/Attribute_Merge/`](../datapacks/Attribute_Merge/).

**Apothic Attributes (`attributeslib:*`) is the canonical attribute set** — it owns the Attributes GUI, the tooltips and the combat formulas in `config/attributeslib.cfg` (currently stock — not tracked in the repo).

Iron's Lib ships its own fork of 7 of those attributes (same names, same descriptions, same value conventions), so each stat existed twice. Iron's Lib's built-in Apothic compat is gated on modid `apothic_attributes`, but the installed Apothic Attributes 1.20.1-1.3.7 declares modid `attributeslib` — so it never fires. Iron's Lib also reads attribute remaps from datapacks (`irons_lib_attribute_remap`), which is what this datapack uses: item/curio modifiers for the `irons_lib` attribute are rewritten onto the Apothic one, so they **stack on a single attribute** and the Iron's Lib handlers stop contributing (no double-dipping).

| Merged stat | From | To |
|---|---|---|
| Critical damage | `irons_lib:crit_damage` | `attributeslib:crit_damage` |
| Dodge chance | `irons_lib:dodge_chance` | `attributeslib:dodge_chance` |
| Arrow damage | `irons_lib:arrow_damage` | `attributeslib:arrow_damage` |
| Armor pierce | `irons_lib:armor_pierce` | `attributeslib:armor_pierce` |
| Experience gained (mob + block XP) | `irons_lib:experience_gained` | `attributeslib:experience_gained` |
| Healing received | `irons_lib:healing_received` | `attributeslib:healing_received` |
| Mining speed | `irons_lib:mining_speed` | `attributeslib:mining_speed` |

### Cataclysm + Obscure API merged by KubeJS bridge

Script [`kubejs/server_scripts/attribute_merge_bridge.js`](../kubejs/server_scripts/attribute_merge_bridge.js).

These two families **cannot** use the datapack remap above, because the remap transfers the modifier amount as-is and neither shares Apothic's conventions:

- **Cataclysm** counts in percent **points** — The Annihilator grants `ADDITION 75.0` meaning "+75% crit damage", which remapped onto Apothic's 1.5-based multiplier would be a 76.5× crit. Needs a ×0.01 rescale.
- **Obscure API** reads its attributes by **summing** the amounts of ADDITION + MULTIPLY_BASE + MULTIPLY_TOTAL modifiers additively (`ObscureAPIAttributes.getPercent`), and Aquamirae grants them as `MULTIPLY_BASE` (e.g. `0.3`). Under the vanilla math Apothic uses, `MULTIPLY_BASE` on a base-0 attribute (dodge, crit chance) contributes **nothing** — a raw remap would silently delete those bonuses.

So the script reads each source the way its own mod does, re-applies it to the Apothic attribute as a transient `ADDITION` modifier (rescaled), and cancels the source so its owning mod contributes nothing. Re-synced once per second per player; nothing is written to player data.

| Merged stat | From | To | Scale |
|---|---|---|---|
| Critical damage (percent points) | `cataclysm:additional_critical_damage` | `attributeslib:crit_damage` | ×0.01 |
| Critical damage | `obscure_api:critical_damage` | `attributeslib:crit_damage` | ×1 |
| Crit chance | `obscure_api:critical_hit` | `attributeslib:crit_chance` | ×1 |
| Dodge chance | `obscure_api:dodge` | `attributeslib:dodge_chance` | ×1 |
| Armor penetration (% of armor) | `obscure_api:penetration` | `attributeslib:armor_shred` | ×1 |
| Healing received | `obscure_api:healing_power` | `attributeslib:healing_received` | ×1 |

Obscure's Penetration is a *percentage* of armor ignored, so its twin is `armor_shred` (percentage armor reduction), not `armor_pierce` (flat armor points).

Note: an Aquamirae item's own tooltip still lists its Obscure API stat lines (that's what the item grants); the **player's** total now lives on the Apothic stat. The value is applied once, but it is described in both places.

**Deliberately left alone:** **Tinkers' Construct** (`Critical Damage`, `Experience`, `Mining Speed` — owner's call to skip Tinkers integration); `obscure_api` `parry` / `accuracy` / `resilience` / `regeneration` / `magic_damage` / `magic_resistance` and `cataclysm` `nature_heal` / `eat_speed` / `charge_time`, which have no Apothic counterpart to merge into. **Jump height** is not duplicated at all — `tconstruct:generic.jump_boost` is the only jump-height attribute; Artifacts / Unique Accessories / More Relics / Botania change jump height in code. Details in the datapack's [README](../datapacks/Attribute_Merge/README.md).

---

## Waystones

Waystones are a **discovered, XP-gated** travel network — not a cheap early convenience.
Two systems: mod config for world-gen + XP, and KubeJS for the recipes.

**World-gen & teleport cost** — [`mod_configs_server/waystones-common.toml`](../mod_configs_server/waystones-common.toml):

| Setting | Value | Effect |
|---|---|---|
| `worldGen.frequency` | `31` | ~500 blocks between world-gen waystones (was 25 ≈ 400) — sparse |
| `restrictions.generatedWaystonesUnbreakable` | `true` | World-gen waystones cannot be mined |
| `xpCost.minimumBaseXpCost` | `1.0` | Every warp costs ≥ 1 level |
| `xpCost.maximumBaseXpCost` | `5.0` | Long-haul cap of 5 levels (was 3) |
| `xpCost.blocksPerXpLevel` | `1000` | Cost scales 1 level per 1000 blocks between the min/max |
| `xpCost.*XpCostMultiplier` (waystone, warpStone, sharestone, portstone, warpPlate, global, inventoryButton) | `1.0` | All warp methods now pay the base cost (were `0.0` = free) |

Because Minecraft XP levels get progressively more expensive, a flat few-level cost stings early game but is trivial by endgame — **usable at endgame, harder early**. All values are tunable.

**Recipes** — [`kubejs/server_scripts/waystones_balance.js`](../kubejs/server_scripts/waystones_balance.js):

| Item | Change |
|---|---|
| `waystones:warp_stone` | Recipe **removed** — no survival craft. Intended as a future expensive trader trade (see [`TODO.MD`](../TODO.MD)); unobtainable for now. |
| `waystones:waystone` | **Unchanged** — the mod's default recipe already requires a Warp Stone (3× stone bricks + 4× obsidian + 1× Warp Stone). Since Warp Stone is unobtainable, the waystone is uncraftable too, so waystones are currently **only** available from world gen. |

---

## Conventions for this wiki

Add each server-side change to the relevant category: for mobs, include **why**; for game rules, just the **rule name + state** (on/off/value). Record current state only — no task/progress notes (that's [`TODO.MD`](../TODO.MD)). Keep the source-of-truth file in the repo. Client-side/visual tweaks do **not** belong here.
