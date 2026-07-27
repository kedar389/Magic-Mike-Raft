# Magic Minecraft Server — Biome Reference

Every biome that **actually generates** on this server, grouped by type. Companion to [`modpack-wiki.md`](modpack-wiki.md) (config changes) — this page is pure reference.

## Where biomes come from

| Source | Role | Count |
|---|---|---|
| **Expanded Ecosphere** (`wythers:`) | Adds 188 new Overworld biomes **and rewrites all 52 vanilla Overworld biomes** (features, mobs, colors). Also supplies the Overworld biome placement (`minecraft:dimension/overworld.json`). | 188 new |
| **Vanilla** (`minecraft:`) | 52 Overworld + 5 Nether + 3 End biomes survive, all EE- or Nullscape-rewritten | 60 |
| **Jaden's Nether Expansion** (`netherexp:`) | 3 Nether sub-biomes, injected via a `MultiNoiseBiomeSource` mixin | 3 |
| **Nullscape** (`nullscape:`) | Rewrites the End dimension + its biomes, adds 3 | 3 |
| **Aquamirae** | Does **not** add a biome — it overrides `minecraft:deep_frozen_ocean` (Ice Maze / Sea of Sorrow content) | 0 |
| **Amplified Nether** | Terrain only — overrides `minecraft:worldgen/noise_settings/nether.json`, no new biomes | 0 |

**Totals: 240 Overworld · 8 Nether · 6 End = 254 biomes.**

Relevant settings: `expanded_ecosphere/config.json5` → `mode = DEFAULT`, `forceLargeBiomes = false`; `expanded_ecosphere/replace_biomes.json5` → `enableBiomes = false` (no biome swaps); `netherexp-common.toml` → `enable_sub_biomes = true`, `black_ice_glaciers = true`, `larger_nether_biomes = false`.

---

## Contents

1. [Overworld](#overworld) — [Oceans](#oceans) · [Beaches & Shores](#beaches--shores) · [Rivers](#rivers) · [Wetlands](#wetlands--swamps-bogs--marshes) · [Plains & Grasslands](#plains--grasslands) · [Savannas](#savannas) · [Deserts, Salt Flats & Scrub](#deserts-salt-flats--scrub) · [Badlands, Canyons & Rock Formations](#badlands-canyons--rock-formations) · [Temperate Forests](#temperate-forests--woodlands) · [Autumnal](#autumnal) · [Taiga & Boreal](#taiga--boreal-forests) · [Tropical & Jungle](#tropical--jungle) · [Eucalyptus & Outback](#eucalyptus--outback-australian) · [Cold & Snowy](#cold--snowy-lowland) · [Mountains & Crags](#mountains-highlands--crags) · [Volcanic & Geothermal](#volcanic--geothermal) · [Islands](#islands) · [Caves & Underground](#caves--underground) · [Mystical](#mystical--otherworldly)
2. [The Nether](#the-nether)
3. [The End](#the-end)
4. [Not generated](#not-generated)

---

# Overworld

240 biomes. `minecraft:` entries are vanilla IDs **as rewritten by Expanded Ecosphere**.

## Oceans

11 biomes.

| Vanilla (9) | Expanded Ecosphere (2) |
|---|---|
| `ocean`, `deep_ocean`, `warm_ocean`, `lukewarm_ocean`, `deep_lukewarm_ocean`, `cold_ocean`, `deep_cold_ocean`, `frozen_ocean`, `deep_frozen_ocean` | `icy_ocean`, `deep_icy_ocean` |

`deep_frozen_ocean` is additionally overridden by **Aquamirae** — this is the Sea of Sorrow / Ice Maze biome.

## Beaches & Shores

12 biomes.

| Vanilla (3) | Expanded Ecosphere (9) |
|---|---|
| `beach`, `snowy_beach`, `stony_shore` | `black_beach`, `calcite_coast`, `cold_stony_shore`, `deepslate_shore`, `desert_beach`, `gravelly_beach`, `icy_shore`, `tropical_beach`, `warm_stony_shore` |

## Rivers

12 biomes.

| Vanilla (2) | Expanded Ecosphere (10) |
|---|---|
| `river`, `frozen_river` | `badlands_river`, `black_river`, `deep_desert_river`, `desert_river`, `gravelly_river`, `icy_river`, `jungle_river`, `lantern_river` (bioluminescent), `savanna_river`, `tropical_forest_river` |

## Wetlands — swamps, bogs & marshes

23 biomes.

| Vanilla (2) | Expanded Ecosphere (21) |
|---|---|
| `swamp`, `mangrove_swamp` | `ancient_mossy_swamp`, `ancient_oak_swamp`, `bamboo_jungle_swamp`, `bamboo_swamp`, `bayou`, `berry_bog`, `birch_swamp`, `coastal_mangroves`, `dripleaf_swamp`, `fen`, `flooded_rainforest`, `flooded_savanna`, `flooded_temperate_rainforest`, `flowering_pantanal`, `marsh`, `mud_pools`, `old_growth_taiga_swamp`, `pantanal`, `snowy_bog`, `snowy_fen`, `waterlily_swamp` |

See also `autumnal_swamp` under [Autumnal](#autumnal), `phantasmal_swamp` under [Mystical](#mystical--otherworldly), `billabong` under [Eucalyptus & Outback](#eucalyptus--outback-australian).

## Plains & Grasslands

13 biomes.

| Vanilla (3) | Expanded Ecosphere (10) |
|---|---|
| `plains`, `sunflower_plains`, `meadow` | `cold_plains`, `cool_plains`, `highland_plains`, `harvest_fields`, `lapacho_plains`, `spring_flower_fields`, `tropical_grassland`, `humid_tropical_grassland`, `dry_tropical_grassland`, `subtropical_grassland` |

## Savannas

7 biomes.

| Vanilla (3) | Expanded Ecosphere (4) |
|---|---|
| `savanna`, `savanna_plateau`, `windswept_savanna` | `dry_savanna`, `jacaranda_savanna`, `wooded_savanna`, `savanna_basaltic_incursions` (geothermal) |

## Deserts, Salt Flats & Scrub

17 biomes.

| Vanilla (1) | Expanded Ecosphere (16) |
|---|---|
| `desert` | **Desert:** `badlands_desert`, `cactus_desert`, `danakil_desert`, `deep_desert`, `red_desert`, `sand_dunes`, `wooded_desert`, `desert_pinnacles`, `desert_lakes`, `guelta` (oasis)<br>**Salt flats:** `salt_lakes_white`, `salt_lakes_pink`, `salt_lakes_turquoise`<br>**Semi-arid scrub:** `chaparral`, `scrubland`, `scrub_forest` |

See also `outback_desert` under [Eucalyptus & Outback](#eucalyptus--outback-australian).

## Badlands, Canyons & Rock Formations

12 biomes.

| Vanilla (3) | Expanded Ecosphere (9) |
|---|---|
| `badlands`, `eroded_badlands`, `wooded_badlands` | `badlands_canyon`, `savanna_badlands`, `granite_canyon`, `red_rock_canyon`, `stony_canyon`, `cool_stony_canyons`, `snowy_canyon`, `tepui` (table mountain), `tsingy_forest` (limestone spires) |

Jungle-flavored canyons live under [Tropical & Jungle](#tropical--jungle); `ayers_rock` under [Eucalyptus & Outback](#eucalyptus--outback-australian).

## Temperate Forests & Woodlands

19 biomes.

| Vanilla (5) | Expanded Ecosphere (14) |
|---|---|
| `forest`, `flower_forest`, `birch_forest`, `old_growth_birch_forest`, `dark_forest` | `cool_forest`, `cool_forest_edge`, `forest_edge`, `warm_birch_forest`, `sakura_forest`, `giant_sequoia_forest`, `temperate_rainforest`, `tangled_forest`, `spring_flower_forest`, `wistman_woods`, `ancient_moss_forest`, `ancient_copper_beech_forest`, `ancient_golden_beech_forest`, `ancient_emerald_beech_forest` |

## Autumnal

7 biomes, all Expanded Ecosphere — a seasonal theme cluster spanning several terrain types.

`autumnal_forest`, `autumnal_forest_edge`, `autumnal_birch_forest`, `autumnal_flower_forest`, `autumnal_plains`, `autumnal_swamp`, `autumnal_crags`

## Taiga & Boreal Forests

11 biomes.

| Vanilla (4) | Expanded Ecosphere (7) |
|---|---|
| `taiga`, `snowy_taiga`, `old_growth_pine_taiga`, `old_growth_spruce_taiga` | `ancient_taiga`, `birch_taiga`, `larch_taiga`, `deep_snowy_taiga`, `pine_barrens`, `boreal_forest_red`, `boreal_forest_yellow` |

See also `taiga_crags` / `old_growth_taiga_crags` under [Mountains](#mountains-highlands--crags), `thermal_taiga` variants under [Volcanic & Geothermal](#volcanic--geothermal), `old_growth_taiga_swamp` under [Wetlands](#wetlands--swamps-bogs--marshes).

## Tropical & Jungle

16 biomes.

| Vanilla (3) | Expanded Ecosphere (13) |
|---|---|
| `jungle`, `sparse_jungle`, `bamboo_jungle` | `tropical_forest`, `tropical_rainforest`, `highland_tropical_rainforest`, `dry_tropical_forest`, `subtropical_forest`, `subtropical_forest_edge`, `sandy_jungle`, `windswept_jungle`, `sparse_bamboo_jungle`, `bamboo_jungle_highlands`, `jungle_canyon`, `bamboo_jungle_canyon`, `tropical_forest_canyon` |

## Eucalyptus & Outback (Australian)

12 biomes, all Expanded Ecosphere — a regional theme cluster.

`eucalyptus_woodland`, `sparse_eucalyptus_woodland`, `eucalyptus_salubris_woodland`, `eucalyptus_deanei_forest`, `eucalyptus_jungle`, `sparse_eucalyptus_jungle`, `eucalyptus_jungle_canyon`, `outback`, `outback_desert`, `kwongan_heath`, `ayers_rock`, `billabong`

## Cold & Snowy (lowland)

8 biomes.

| Vanilla (3) | Expanded Ecosphere (5) |
|---|---|
| `snowy_plains`, `grove`, `ice_spikes` | `tundra`, `snowy_tundra`, `crimson_tundra`, `ice_cap`, `glacial_cliffs` |

## Mountains, Highlands & Crags

21 biomes.

| Vanilla (7) | Expanded Ecosphere (14) |
|---|---|
| `windswept_hills`, `windswept_forest`, `windswept_gravelly_hills`, `stony_peaks`, `jagged_peaks`, `frozen_peaks`, `snowy_slopes` | `highlands`, `forested_highlands`, `huangshan_highlands`, `jade_highlands`, `maple_mountains`, `tibesti_mountains`, `cool_stony_peaks`, `snowy_peaks`, `andesite_crags`, `aspen_crags`, `icy_crags`, `taiga_crags`, `old_growth_taiga_crags`, `temperate_rainforest_crags` |

Also crags: `autumnal_crags` ([Autumnal](#autumnal)), `thermal_taiga_crags` ([Volcanic & Geothermal](#volcanic--geothermal)).

## Volcanic & Geothermal

8 biomes, all Expanded Ecosphere.

**Volcanic:** `volcano`, `volcanic_crater`, `volcanic_chamber`, `tropical_volcano`, `icy_volcano`
**Geothermal:** `thermal_taiga`, `snowy_thermal_taiga`, `thermal_taiga_crags`

See also `savanna_basaltic_incursions` ([Savannas](#savannas)) and `mediterranean_island_thermal_springs` ([Islands](#islands)).

## Islands

10 biomes, all Expanded Ecosphere.

`tropical_island`, `jungle_island`, `temperate_island`, `mediterranean_island`, `mediterranean_island_thermal_springs`, `mushroom_island`, `desert_island`, `cold_island`, `frigid_island`, `frozen_island`

## Caves & Underground

15 biomes.

| Vanilla (3) | Expanded Ecosphere (12) |
|---|---|
| `lush_caves`, `dripstone_caves`, `deep_dark` | `underground`, `deep_underground`, `calcite_caverns`, `mossy_caves`, `mossy_dripstone_caves`, `lichenous_caves`, `lichenous_dripstone_caves`, `lush_dripstone_caves`, `fungous_dripstone_caves`, `lush_fungous_dripstone_caves`, `mushroom_caves`, `lush_shroom_caves` |

## Mystical & Otherworldly

6 biomes.

| Vanilla (1) | Expanded Ecosphere (5) |
|---|---|
| `mushroom_fields` | `forbidden_forest`, `phantasmal_forest`, `phantasmal_swamp`, `deep_dark_forest`, `deep_dark_incursion` |

`deep_dark_forest` / `deep_dark_incursion` are surface sculk biomes — Warden territory above ground.

---

# The Nether

8 biomes. Terrain is regenerated by **Amplified Nether** (taller, cave-heavy `nether.json` noise settings); all five vanilla biomes are reworked by **Jaden's Nether Expansion**.

| Category | Biomes |
|---|---|
| **Vanilla (JNE-reworked)** | `minecraft:nether_wastes`, `minecraft:crimson_forest`, `minecraft:warped_forest`, `minecraft:soul_sand_valley`, `minecraft:basalt_deltas` |
| **JNE sub-biomes** | `netherexp:exhaust_mire` (fire-spark vents, ghast-heavy), `netherexp:sorrowsquash_pastures` (Soul Sand Valley variant), `netherexp:black_ice_glaciers` (rare Soul Sand Valley variant — 8.5% replacement chance, size 128) |

# The End

6 biomes. **Nullscape** replaces the End dimension, its noise settings and its biome placement.

| Category | Biomes |
|---|---|
| **Vanilla (Nullscape-rewritten)** | `minecraft:the_end`, `minecraft:small_end_islands`, `minecraft:end_highlands` |
| **Nullscape** | `nullscape:void_barrens`, `nullscape:shadowlands`, `nullscape:crystal_peaks` |

---

# Not generated

Registered somewhere in the pack, but **never placed** by any dimension — you cannot find these in-world (only via `/locatebiome`-style teleports or not at all):

| Biome | Why |
|---|---|
| `minecraft:cherry_grove` | Dropped from Expanded Ecosphere's Overworld placement — the only vanilla Overworld biome EE removes entirely |
| `minecraft:end_midlands`, `minecraft:end_barrens` | Removed from End placement by Nullscape |
| `wythers:flooded_jungle` | Defined by Expanded Ecosphere but absent from its Overworld placement (unused) |
| `netherexp:grieving_grove`, `netherexp:plume_bowels`, `netherexp:quartz_chasm` | Translation strings only — no biome definition in the 2.3.5 jar |

---

## How this list was produced

1. Scanned every jar in the live server's `mods/` for `data/<ns>/worldgen/biome/*.json` (biome definitions).
2. Read the authoritative **placement** files — Expanded Ecosphere's built-in `resources/ee_default/data/minecraft/dimension/overworld.json` and Nullscape's `data/minecraft/dimension/the_end.json` — and extracted every `"biome"` entry (240 unique Overworld, 6 End).
3. Nether biomes: vanilla multi-noise preset (5) plus the 3 injected by `netherexp`'s `MultiNoiseBiomeSourceMixin`, cross-checked against `netherexp-common.toml` toggles.
4. Cross-checked definitions against placements to find the unused biomes listed above.

Re-run after adding or removing any worldgen mod — a mod that overrides `minecraft:dimension/overworld.json` will change this list wholesale.
