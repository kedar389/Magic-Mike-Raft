# Magic Minecraft Server — Modpack Wiki

A reference of the deliberate **server-side** config in this modpack: what is set, **where** the setting lives, and (for mobs) **why**. This page is the single index for "is X changed, and how?" — progress/tasks are tracked in [`TODO.MD`](../TODO.MD), not here.

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

_None yet._

---

## Mob Drops

_None yet._

---

## Conventions for this wiki

Add each server-side change to the relevant category: for mobs, include **why**; for game rules, just the **rule name + state** (on/off/value). Record current state only — no task/progress notes (that's [`TODO.MD`](../TODO.MD)). Keep the source-of-truth file in the repo. Client-side/visual tweaks do **not** belong here.
