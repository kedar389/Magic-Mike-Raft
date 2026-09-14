# Pillager Caravans

Replaces the built-in `classic_caravans` pack from Obscuria's Pillager Caravans (`caravans`) with 35 biome-themed caravans across 19 placements.

Namespace `magic_caravans`. Design and rationale: [`docs/caravans.md`](../../docs/caravans.md).

## Contents

| Path | Files | What |
|---|---|---|
| `caravans/placement/` | 19 | One per biome family. Matches a biome set, then picks a caravan pool by weight |
| `caravans/variation/` | 105 | 35 caravans x small / medium / large |
| `tags/worldgen/biome/` | 19 | `has_<placement>_caravan`, one per placement |
| `loot_tables/cargo/` | 116 | Per caravan: small, medium, large, plus prize for 11 of them |

Display names live in `resourcepacks/Caravan_Names/`. Both are needed: without the resource pack the encounter toast shows a raw translation key.

## Composition

Leader 1 at every tier. Escort size depends on the caravan's column:

| Column | Escort s/m/l | Raiders s/m/l |
|---|---|---|
| Light | 2 / 4 / 6 | 3 / 5 / 9 to 11 |
| Standard | 3 / 6 / 9 | 4 / 7 / 12 to 14 |
| Heavy | 4 / 8 / 12 | 5 / 9 / 15 to 17 |

Large adds one `enchantwithmob:enchanter` and one heavy. Heavy count scales inversely with strength: 1 for boss-grade (golem_of_last_resort, zapper) and heavy (ravager, absorber, crocofang), 2 for mid (tesla_golem, iceologer), 3 for light (witch, illusioner, igniter).

Pack llamas are 2 / 4 / 6, carpet-coloured by cargo class. The two drove caravans (Cattle Drovers, Carnival Column) carry 2 llamas at every tier and a herd of 3 / 6 / 9 animals instead.

## Cargo

Every caravan has its own tables. 116 of them under `loot_tables/cargo/<caravan>/`.

| Table | Pools |
|---|---|
| `small` | staple rolls 2-3 |
| `medium` | staple rolls 2-4, mid rolls 1 |
| `large` | staple rolls 3-5, mid rolls 1-2, class prize rolls 1 against empty at weight 4 |
| `prize` | the special guaranteed 1, class prize rolls 1-2, mid rolls 2-3 |

Bigger caravans pay twice: more llamas (2 / 4 / 6) and a richer table on each.

Each table is its caravan's cargo classes plus one or two signature items of its own, so Dune Miners drops sand and TNT on top of Ore and Sculk Cultists drops sculk and echo shards on top of Magic dust and Runes.

### The special

Eleven caravans carry one Artifacts curio, at large tier only. It rides a single dedicated llama on the `prize` table whose first pool grants exactly one, so a caravan can never drop two.

Marauder Column, Illusionist's Troupe, Grave Consort, Deep Delvers and Geode Cutters take PILLAGE-family items. Tomb Robbers DESERT, Idol Looters and Crystal Coven JUNGLE, Sculk Cultists SCULK, Wreckers AQUATIC, Frostbound Wardens COLD. Families come from `docs/accessories.md`; the other 24 caravans top out at their class prize.

### Item sources

Every non-vanilla id was checked against a dump of all 172 jars' lang files, which caught that Epic Knights registers under `magistuarmory`, not `epicknights`.

Three families are deliberately absent. **Waystones**: warp stone is meant to come only from expensive trader trades and it gates the waystone recipe. **End Remastered**: eyes are boss drops. **Botania**: flower- and ritual-made, not hauled goods.

Relics and More Relics self-distribute by regex over loot table ids across all namespaces. No cargo table name matches one of their family keywords, so nothing injects here by accident.

## Biome sets

Tags where one exists, because Expanded Ecosphere merges its 188 biomes into the vanilla `minecraft:`, `forge:` and `c:` tags with `replace: false`. Explicit biome lists for `salt_flats`, `outback`, `autumnal`, `bamboo`, `volcanic`, and for `jungle` and `swamp`, which need bamboo and mangrove excluded and a biome tag cannot subtract.

Every entry is `required: false`, so a placement still loads if a mod that owns one of its biomes is removed.

No caravans spawn in cave or island biomes: neither has room to walk a column.

## Editing

`docs/caravans.md` holds the full table these files were built from. For a one-off tweak edit the JSON directly. For a change that touches every caravan, change the doc first and rebuild from it.
