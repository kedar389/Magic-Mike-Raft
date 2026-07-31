# RealmRPG Pool Namespace Fix

Silences the `Empty or non-existent pool: minecraft:<something>_pool` world-gen warnings and makes **RealmRPG: Quests** houses generate their full layout.

## The bug

`realmrpg_quests-0.1.1-forge-1.20.1.jar` defines its jigsaw pools correctly under its own namespace
(`data/realmrpg_quests/worldgen/template_pool/*.json`), but **16 jigsaw blocks inside its structure NBTs
reference those pools without the namespace**. A bare `cook_end_1_pool` parses as `minecraft:cook_end_1_pool`,
which does not exist, so `JigsawPlacement` logs:

```
[DH-World Gen Thread[3]/WARN] [minecraft/JigsawPlacement]: Empty or non-existent pool: minecraft:cook_end_1_pool
```

The root ("spawn") pieces use the correct namespace, so the houses **do** generate — they just silently lose the
add-on wings and end caps that hang off the broken jigsaw slots.

Two of the references are outright authoring slips:

| NBT | Points at | Should be |
|---|---|---|
| `cook_houses/cook_additions_3_2` | `minecraft:angler_additions_3_pool` | `realmrpg_quests:cook_additions_3_pool` |
| `monster_hunter_houses/monster_hunter_spawn_4` | `minecraft:house_basement_pool` (no such pool in any namespace) | `realmrpg_quests:monster_hunter_basement_pool` — the pool its sibling `monster_hunter_spawn_1`/`_2` use in the same slot |

## The fix

Jigsaw references live inside binary NBT, so they can't be patched by a datapack. Instead this pack **provides the
missing pools under `minecraft:`** — each file is a verbatim copy of the mod's own pool (element locations still
point at `realmrpg_quests:` structures), with the cosmetic `name` field rewritten to match:

`data/minecraft/worldgen/template_pool/` — 16 pools:

| Group | Pools |
|---|---|
| Angler | `angler_additions_3_pool`, `angler_end_1_pool` |
| Cook | `cook_additions_1_pool` … `cook_additions_4_pool`, `cook_end_1_pool` |
| Lumberjack | `lumberjack_additions_3_pool`, `lumberjack_end_1_pool` |
| Miner | `miner_main_room_pool`, `miner_additions_1_pool` … `miner_additions_3_pool` |
| Monster Hunter | `monster_hunter_additions_3_pool`, `monster_hunter_end_1_pool` |
| Basement | `house_basement_pool` — copy of `monster_hunter_basement_pool` |

Because `cook_additions_3_2` asks for the *angler* pool, that one house variant will attach an angler wing. Keeping
the mod's literal intent (an alias of the angler pool) is what this pack does; it is a mod bug either way.

## Still logged after this pack

`Empty or non-existent pool: minecraft:` (empty path) — jigsaw blocks in `angler_basement_*`, `cook_basement_*`,
`cook_additions_1_4/_5` and friends have a **blank** pool string where they should say `minecraft:empty`. A blank
path is not a legal file name, so no datapack can define it. These are terminator jigsaws: nothing is meant to
attach there, so generation is already correct and the warning is pure noise. Only a mod update, or a Log4j
filter, removes those lines.

## Deploy

Copy this folder into `<server>/world/datapacks/` and `/reload` (or restart). Structures already generated in
loaded chunks are unaffected — only newly generated chunks pick up the completed layouts.
