# Pillager Caravans

Replaces the built-in `classic_caravans` pack from Obscuria's Pillager Caravans (`caravans`) with 35 biome-themed caravans across 19 placements.

Namespace `magic_caravans`. Design and rationale: [`docs/caravans.md`](../../docs/caravans.md).

## Contents

| Path | Files | What |
|---|---|---|
| `caravans/placement/` | 19 | One per biome family. Matches a biome set, then picks a caravan pool by weight |
| `caravans/variation/` | 105 | 35 caravans x small / medium / large |
| `tags/worldgen/biome/` | 19 | `has_<placement>_caravan`, one per placement |
| `loot_tables/cargo/` | 20 | One per cargo class, referenced by the llamas |

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

Each llama points at one of the 20 class tables. Every table has three pools:

| Pool | Rolls | Holds |
|---|---|---|
| Staple | 2-4 | Bulk goods, stacked |
| Mid | 1-2 | The worthwhile middle |
| Prize | 1 | One item against a `minecraft:empty` entry at weight 5 |

So a llama yields 3 to 6 stacks, and about one in six also drops the class prize. A large caravan with 6 llamas is worth roughly 20 to 36 stacks.

A caravan with two cargo classes splits its pack between both tables, the first class taking the remainder on an odd count.

Every non-vanilla item id was checked against the deployed jars' lang files before it went in. Note that Epic Knights registers under `magistuarmory`, not `epicknights`.

No Relics, More Relics, Artifacts or Unique Accessories items are placed here. Relics and More Relics self-distribute by regex over loot table ids across all namespaces, and `docs/accessories.md` is still deciding where accessories should land. None of the 20 class names match a Relics family keyword (`desert`, `jungle`, `ocean`, `pillag`, `sculk` and so on), so nothing injects into these tables by accident either.

## Biome sets

Tags where one exists, because Expanded Ecosphere merges its 188 biomes into the vanilla `minecraft:`, `forge:` and `c:` tags with `replace: false`. Explicit biome lists for `salt_flats`, `outback`, `autumnal`, `bamboo`, `volcanic`, and for `jungle` and `swamp`, which need bamboo and mangrove excluded and a biome tag cannot subtract.

Every entry is `required: false`, so a placement still loads if a mod that owns one of its biomes is removed.

No caravans spawn in cave or island biomes: neither has room to walk a column.

## Editing

`docs/caravans.md` holds the full table these files were built from. For a one-off tweak edit the JSON directly. For a change that touches every caravan, change the doc first and rebuild from it.
