# Loot_Tables

The single datapack for loot changes: structure chests, mob drops and gameplay loot. Loot that belongs to one of the pack's own features stays in that feature's datapack (caravan cargo is in `Pillager_Caravans`).

## Two ways to change loot

| Method | Path | Use for |
|---|---|---|
| Replace | `data/<modid>/loot_tables/<same path as the mod>.json` | rewriting a table completely |
| Add | `data/forge/loot_modifiers/global_loot_modifiers.json` + `data/magicserver/loot_modifiers/<name>.json` | adding items to a table and keeping the mod's own entries, so mod updates still apply |
| Shared | `data/magicserver/loot_tables/family/<name>.json`, `.../tier/<name>.json` | tables other tables pull in with a `minecraft:loot_table` entry |

Which table belongs to which loot family: [`docs/loot-tables.md`](../../docs/loot-tables.md). Wearables per family: [`docs/accessories.md`](../../docs/accessories.md).

## Current changes

None.
