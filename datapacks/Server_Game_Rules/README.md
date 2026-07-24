# Server_Game_Rules

The **single** datapack for all server game rules. Minecraft has no standalone
gamerule config file — gamerules live in `level.dat` — so a datapack load
function is the file-based way to set them.

Rules are organized **per mod**: one `.mcfunction` file per mod, named after the
mod, under `data/server_gamerules/functions/`. Do not create separate datapacks
for each mod — add a new function file here instead.

## Layout

```
data/server_gamerules/functions/
├── load.mcfunction      # run-once guard, fired by #minecraft:load
├── apply.mcfunction     # calls each mod's rule function, then marks done
└── born_in_chaos.mcfunction   # Born in Chaos (born_in_chaos_v1) rules
```

## Rules currently applied

### Born in Chaos (`born_in_chaos.mcfunction`)

| Rule | Value | Effect |
|------|-------|--------|
| `disappearanceofSpiritsUndertheSun` | `true`  | Spirit mobs burn/vanish in daylight |
| `maggotsAppearance`                 | `false` | No maggots from corpses on death |
| `corpseFlySpawn`                    | `false` | No naturally-spawning corpse flies * |
| `soulStratificationEffect`          | `true`  | On |
| `theappearanceoftheNightmareStalker`| `true`  | Nightmare Stalker can appear |
| `generationofInfectedDiamonds`      | `true`  | Infected diamonds generate |
| `naughtinessMechanics`              | `true`  | On |

\* Does **not** stop the corpse-fly swarm triggered by eating rotten flesh — that
path is hardcoded (Rotten Smell effect) and not gated by any game rule.

## Behaviour

Runs **once**. First world load applies all mod rules and sets a marker
(`#done server_gr_setup`); later restarts skip it, so manual `/gamerule` changes
you make in-game are preserved.

## Add rules for another mod

1. Create `data/server_gamerules/functions/<modname>.mcfunction` with its
   `gamerule ...` lines.
2. Add `function server_gamerules:<modname>` to `apply.mcfunction`.
3. Re-apply (see below).

## Install (server)

1. Copy this folder into `<world>/datapacks/`
   (e.g. `servers_folder/magic server/world/datapacks/Server_Game_Rules`).
2. Server console / as op: `reload` (or restart).
3. Confirm: `gamerule maggotsAppearance` (prints the current value).

## Re-apply after editing rules

The baseline only runs once, so to push changes:

```
scoreboard players set #done server_gr_setup 0
reload
```
or run a single mod's file directly: `function server_gamerules:born_in_chaos`
