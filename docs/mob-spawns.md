# Magic Minecraft Server — Mob Spawn Reference

Every mod on this server that adds a mob, and where that mob actually comes from.
Companion to [`biomes.md`](biomes.md) (which biomes exist), [`structures.md`](structures.md) (where structures generate) and [`modpack-wiki.md`](modpack-wiki.md) (player-facing changes).

## How to read this

| Notation | Meaning |
|---|---|
| `#namespace:tag` | A biome **tag** — matches every biome in it, including Expanded Ecosphere's |
| `minecraft:swamp` | An exact **biome ID** — only that one biome |
| `A + B` | Both conditions must hold |
| `A · or · B` | Either alternative qualifies |
| **w** | Spawn weight relative to other mobs in the same category; `0` = disabled |

**Structure biomes below are the effective ones** — the mod's own tag with Structurify's overrides from `configuration_server/mod_configs_server/structurify.json` applied. [`structures.md`](structures.md) lists every structure's full biome set and spacing.

**Tags vs. IDs matters here.** Expanded Ecosphere replaces the Overworld with 188 of its own biomes but *does* add them to the vanilla `minecraft:`, `forge:` and `c:` biome tags. So tag-based spawns (`#forge:is_snowy`, `#minecraft:is_forest`) work everywhere, while spawns pinned to an exact vanilla biome ID only fire in the 52 vanilla biomes EE keeps.

## Contents

- [Overworld — animals & ambient](#overworld--animals--ambient)
- [Overworld — hostiles](#overworld--hostiles)
- [The Nether](#the-nether)
- [The End](#the-end)
- [Structure- and boss-bound](#structure--and-boss-bound)
- [Raid-, patrol- and event-only](#raid--patrol--and-event-only)
- [Never spawns naturally](#never-spawns-naturally)
- [Spawn removals and global controls](#spawn-removals-and-global-controls)
- [How this list was produced](#how-this-list-was-produced)

---

# Overworld — animals & ambient

## Alex's Mobs

Biome rules live in `config/alexsmobs/<mob>_spawns.json`, weights in `config/alexsmobs.toml`.
Rows referencing only absent mods (Biomes O' Plenty, Terralith) are dropped — those alternatives can never match here.

| Mob | Spawns in | w |
|---|---|---|
| `alligator_snapping_turtle` | #minecraft:is_overworld + #forge:is_swamp + not minecraft:mangrove_swamp | 20 |
| `anaconda` | #minecraft:is_overworld + #forge:is_swamp · or · minecraft:mangrove_swamp | 0 |
| `anteater` | #minecraft:is_overworld + #minecraft:is_jungle + not minecraft:bamboo_jungle | 0 |
| `bald_eagle` | #minecraft:is_overworld + #minecraft:is_hill + #forge:is_coniferous · or · minecraft:grove · or · minecraft:windswept_forest | 15 |
| `banana_slug` | minecraft:old_growth_pine_taiga · or · minecraft:old_growth_spruce_taiga · or · #minecraft:is_taiga + #forge:is_dense/overworld + #forge:is_rare | 0 |
| `bison` | #minecraft:is_overworld + #forge:is_plains + not #minecraft:is_savanna + not #forge:is_hot/overworld · or · minecraft:meadow | 9 |
| `blobfish` | #minecraft:is_deep_ocean | 0 |
| `blue_jay` | #minecraft:is_overworld + #minecraft:is_forest + not minecraft:sparse_jungle · or · #minecraft:is_overworld + #minecraft:is_taiga · or · minecraft:cherry_grove | 16 |
| `bunfungus` | #minecraft:is_overworld + #forge:is_mushroom + #forge:is_rare | 3 |
| `cachalot_whale` | #minecraft:is_overworld + #minecraft:is_ocean + #forge:is_cold/overworld · or · minecraft:lukewarm_ocean · or · minecraft:deep_ocean · or · minecraft:deep_lukewarm_ocean | 2 |
| `caiman` | minecraft:mangrove_swamp | 29 |
| `capuchin_monkey` | #minecraft:is_jungle + not minecraft:bamboo_jungle · or · minecraft:mangrove_swamp | 28 |
| `catfish` | #minecraft:is_overworld + #forge:is_swamp + not minecraft:mangrove_swamp · or · #minecraft:is_overworld + #minecraft:is_river + not #forge:is_cold/overworld | 2 |
| `cave_centipede` | #minecraft:is_overworld + not #forge:no_default_monsters + not #minecraft:is_ocean + not #forge:is_mushroom + not minecraft:deep_dark | 8 |
| `cockroach` | #minecraft:is_overworld + not #minecraft:is_ocean + not #forge:is_mushroom + not minecraft:deep_dark | 0 |
| `comb_jelly` | minecraft:frozen_ocean · or · minecraft:deep_frozen_ocean | 0 |
| `crocodile` | #forge:is_swamp · or · minecraft:mangrove_swamp · or · #minecraft:is_river + not #forge:is_cold | 20 |
| `crow` | #minecraft:is_overworld + not #minecraft:is_savanna + #forge:is_plains · or · #minecraft:is_overworld + #minecraft:is_forest · or · #minecraft:is_overworld + #minecraft:is_taiga · or · minecraft:cherry_grove | 10 |
| `devils_hole_pupfish` | #minecraft:is_overworld | 23 |
| `elephant` | #minecraft:is_overworld + #minecraft:is_savanna | 30 |
| `emu` | #minecraft:is_overworld + #minecraft:is_badlands · or · #minecraft:is_overworld + #minecraft:is_savanna | 20 |
| `flutter` | minecraft:lush_caves | 13 |
| `fly` | #minecraft:is_overworld | 0 |
| `flying_fish` | #minecraft:is_overworld + #minecraft:is_ocean + not #forge:is_cold/overworld + not #forge:is_hot/overworld + not minecraft:deep_ocean + not minecraft:deep_lukewarm_ocean | 0 |
| `frilled_shark` | #minecraft:is_deep_ocean | 11 |
| `froststalker` | minecraft:ice_spikes · or · minecraft:frozen_peaks | 20 |
| `gazelle` | #minecraft:is_savanna | 0 |
| `gelada_monkey` | #minecraft:is_overworld + #forge:is_plains + #forge:is_plateau | 5 |
| `giant_squid` | #minecraft:is_deep_ocean | 3 |
| `gorilla` | #minecraft:is_jungle + not minecraft:bamboo_jungle | 25 |
| `grizzly_bear` | #minecraft:is_overworld + #minecraft:is_forest + not minecraft:sparse_jungle · or · #minecraft:is_overworld + #minecraft:is_taiga · or · minecraft:cherry_grove | 8 |
| `guster` | not #forge:no_default_monsters + #forge:is_hot/overworld + #forge:is_dry/overworld + #forge:is_sandy | 0 |
| `hammerhead_shark` | #minecraft:is_ocean + #forge:is_hot/overworld | 8 |
| `hummingbird` | minecraft:flower_forest · or · minecraft:sunflower_plains · or · #minecraft:is_jungle · or · minecraft:meadow · or · minecraft:cherry_grove | 19 |
| `jerboa` | #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy + not #minecraft:is_badlands | 12 |
| `kangaroo` | #minecraft:is_overworld + #minecraft:is_badlands · or · #minecraft:is_overworld + #minecraft:is_savanna | 25 |
| `komodo_dragon` | #minecraft:is_jungle + not #forge:is_dense/overworld | 16 |
| `lobster` | #minecraft:is_beach · or · minecraft:stony_shore | 0 |
| `maned_wolf` | #minecraft:is_overworld + #minecraft:is_savanna | 8 |
| `mantis_shrimp` | #minecraft:is_ocean + #forge:is_hot/overworld · or · minecraft:mangrove_swamp | 15 |
| `mimic_octopus` | #forge:is_hot/overworld + #minecraft:is_ocean + not minecraft:deep_warm_ocean | 9 |
| `moose` | #minecraft:is_overworld + #forge:is_snowy + #forge:is_wasteland · or · #minecraft:is_overworld + #forge:is_snowy + #minecraft:is_taiga | 9 |
| `mudskipper` | minecraft:mangrove_swamp | 28 |
| `mungus` | #minecraft:is_overworld + #forge:is_mushroom + #forge:is_rare | 0 |
| `orca` | #minecraft:is_ocean + #forge:is_cold/overworld | 2 |
| `platypus` | #minecraft:is_overworld + #minecraft:is_river + not #forge:is_cold/overworld | 20 |
| `potoo` | minecraft:dark_forest | 15 |
| `raccoon` | #minecraft:is_overworld + #minecraft:is_forest · or · #minecraft:is_overworld + not #minecraft:is_savanna + #forge:is_plains · or · #minecraft:is_overworld + #minecraft:is_taiga · or · minecraft:cherry_grove | 10 |
| `rain_frog` | #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy + not #minecraft:is_badlands | 10 |
| `rattlesnake` | #minecraft:is_badlands · or · #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy | 12 |
| `rhinoceros` | #minecraft:is_savanna | 24 |
| `roadrunner` | #minecraft:is_badlands · or · #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy | 9 |
| `rocky_roller` | not #forge:no_default_monsters + minecraft:dripstone_caves | 60 |
| `seagull` | #minecraft:is_overworld + #minecraft:is_beach · or · minecraft:stony_shore | 21 |
| `seal` | #minecraft:is_beach · or · #minecraft:is_overworld + #minecraft:is_ocean + #forge:is_cold/overworld · or · minecraft:stony_shore | 20 |
| `shoebill` | #minecraft:is_overworld + #forge:is_swamp + not minecraft:mangrove_swamp | 10 |
| `skelewag` | not #forge:no_default_monsters + #minecraft:is_overworld + #minecraft:is_deep_ocean | 15 |
| `skunk` | #minecraft:is_overworld + #minecraft:is_forest + not #minecraft:is_savanna + not #forge:is_cold/overworld + not minecraft:sparse_jungle · or · minecraft:cherry_grove | 7 |
| `snow_leopard` | #minecraft:is_overworld + #forge:is_snowy · or · minecraft:snowy_slopes · or · minecraft:frozen_peaks · or · minecraft:jagged_peaks | 18 |
| `sugar_glider` | minecraft:birch_forest · or · minecraft:old_growth_birch_forest | 15 |
| `sunbird` | #minecraft:is_overworld + #minecraft:is_mountain · or · minecraft:snowy_slopes · or · minecraft:frozen_peaks · or · minecraft:jagged_peaks | 5 |
| `tarantula_hawk` | #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy + not #minecraft:is_badlands | 6 |
| `tasmanian_devil` | #minecraft:is_overworld + #minecraft:is_forest + not #minecraft:is_savanna + not #forge:is_cold/overworld + not minecraft:sparse_jungle | 10 |
| `terrapin` | #minecraft:is_overworld + #minecraft:is_river + not #forge:is_cold/overworld | 0 |
| `tiger` | minecraft:bamboo_jungle · or · minecraft:cherry_grove | 0 |
| `toucan` | #minecraft:is_overworld + #minecraft:is_jungle + not minecraft:bamboo_jungle | 23 |
| `triops` | #forge:is_dry/overworld + #forge:is_hot/overworld + #forge:is_sandy + not #minecraft:is_badlands | 0 |
| `tusklin` | minecraft:ice_spikes · or · #minecraft:is_overworld + #forge:is_snowy + #forge:is_plains | 18 |

Cave-dwellers configured in their own file rather than a `_spawns.json`:

| Mob | Spawns in | w |
|---|---|---|
| `cave_centipede` | Overworld, not ocean / mushroom / deep dark, monsters allowed | 8 |
| `cockroach` | Overworld, not ocean / mushroom / deep dark | 0 |
| `murmur` | Overworld, not ocean / mushroom / deep dark | 5 |
| `underminer` | Overworld, not ocean / mushroom / deep dark | 0 |
| `skreecher` | `#alexsmobs:skreechers_can_spawn_wardens` | 10 |
| `farseer` | Anywhere monsters spawn except `minecraft:mushroom_fields` | 0 |
| `rocky_roller` | `minecraft:dripstone_caves` | 60 |
| `flutter` | `minecraft:lush_caves` | 13 |

Nether and End members of Alex's Mobs are listed under [The Nether](#the-nether) and [The End](#the-end).

Extra gates from `alexsmobs.toml`:

| Setting | Effect |
|---|---|
| `limitGusterSpawnsToWeather = true` | Gusters only while raining or thundering (moot — weight is 0) |
| `soulVultureSpawnOnFossil = true` | Soul Vultures restricted to Nether Fossils, ignoring the biome file |
| `mimicubeSpawnInEndCity = true` | Mimicubes restricted to End Cities, ignoring the biome file |
| `voidWormSpawnDimensions = ["minecraft:the_end"]` | Mysterious Worm only summons in the End |

Rows at **w0** are switched off — the list and the reasoning live in [`modpack-wiki.md` → Banned Mobs](modpack-wiki.md#banned-mobs); the knob is `<mob>SpawnWeight = 0`.

`cachalot_whale_beached` uses the Cachalot Whale weight. `leafcutter_anthill` is a worldgen feature, not a spawn, and is off via `leafcutterAnthillSpawnChance = 0.0`.

## Friends & Foes

Code-driven biome modifier keyed off `friendsandfoes:` biome tags. Every mob is enabled in `config/friendsandfoes.json`.

| Mob | Spawns in | w | Group |
|---|---|---|---|
| Crab | `minecraft:mangrove_swamp`, `minecraft:beach` | 14 | 2-4 |
| Glare | `minecraft:lush_caves` | 4 | 1 |
| Moobloom | `flower_forest`, `cherry_grove`, `meadow`, `sunflower_plains` | 4 | 2-4 |
| Mauler | `minecraft:desert`, `#forge:is_desert` | 8 | 1 |
| Mauler | `#minecraft:is_badlands` | 16 | 1 |
| Mauler | `#minecraft:is_savanna`, `#forge:is_savanna` | 32 | 1 |
| Rascal | `#minecraft:has_structure/mineshaft`, `.../mineshaft_mesa` | - | 1 |

Copper Golem, Tuff Golem, Iceologer, Illusioner and Wildfire are structure-bound — see [Structure- and boss-bound](#structure--and-boss-bound).

## Ribbits

| Mob | Spawns in |
|---|---|
| Ribbit | Ribbit Village only — `minecraft:swamp`, `#forge:is_swamp` |

## Sea Dwellers

| Mob | Spawns in | w |
|---|---|---|
| Depth Mermorph | any biome | 1 |
| Orange / Yellow Coral Mermorph | `minecraft:warm_ocean` | 1 each |
| Polar Mermorph | `deep_cold_ocean`, `frozen_ocean`, `cold_ocean` | 1 |
| River Mermorph | `minecraft:river` | 1 |

Worker, Farmer, Collector, Architect, Blacksmith and Hunter Mermorphs are village residents. They arrive with the Sea Village, which generates as a worldgen feature (`seadwellers:sea_village_spawn`) in any biome.

---

# Overworld — hostiles

## Born in Chaos

44 mobs, one plain `forge:add_spawns` file each. Three are pinned to specific biomes; three more are switched off by game rule; the remaining **38 use `forge:any`** — any Overworld biome, at night, competing with vanilla monsters for the same spawn budget, for a combined weight of 459.

| Mob | Spawns in | w | Group |
|---|---|---|---|
| Zombie Fisherman | any | 30 | 1 |
| Decaying Zombie | any | 30 | 1 |
| Baby Skeleton | any | 25 | 1-2 |
| Barrel Zombie | any | 25 | 1 |
| Zombie Lumberjack | any | 25 | 1 |
| Decrepit Skeleton | any | 20 | 1 |
| Restless Spirit | any | 20 | 1 |
| Baby Spider | any | 16 | 3-5 |
| Siamese Skeletons | any | 16 | 1 |
| Spirit Guide | any | 16 | 1 |
| Zombie Bruiser | any | 16 | 1 |
| Dread Hound | any | 15 | 3-5 |
| Mother Spider | any | 15 | 1 |
| Mr. Pumpkin | any | 15 | 1-3 |
| Dark Vortex | any | 14 | 1 |
| Mrs. Pumpkin | any | 13 | 1 |
| Skeleton Demoman | any | 12 | 1 |
| Bonescaller | any | 11 | 1 |
| Swarmer | any | 11 | 1 |
| Pumpkin Bruiser | any | 10 | 1 |
| Senor Pumpkin | any | 10 | 1-2 |
| Krampus Henchman | any | 9 | 2-3 |
| Spirit Guide Assistant | any | 9 | 2 |
| Phantom Creeper | any | 8 | 1 |
| Skeleton Thrasher | any | 8 | 1 |
| Spirit of Chaos | any | 8 | 1 |
| Thornshell Crab | any | 8 | 1 |
| Zombie Clown | any | 8 | 1 |
| Corpse Fish | any | 7 | 3-5 |
| Fallen Chaos Knight | any | 5 | 1 |
| Dire Hound Leader | any | 4 | 1 |
| Missioner | any | 4 | 1 |
| Nightmare Stalker | any | 4 | 1 |
| Sir Pumpkinhead | any | 4 | 1 |
| Lifestealer | any | 3 | 1 |
| Glutton Fish | any | 2 | 1 |
| Krampus | any | 2 | 1 |
| Supreme Bonescaller | any | 1 | 1 |
| Seared Spirit | `plains`, `sunflower_plains` | 5 | 1 |
| Bone Imp | `basalt_deltas`, `nether_wastes` | 25 | 1-2 |
| Firelight | `crimson_forest`, `nether_wastes` | 15 | 1-3 |

Switched off by the game-rules datapack, so their `forge:any` entries never fire — see [`modpack-wiki.md` → Banned Mobs](modpack-wiki.md#banned-mobs):

| Mob | w in the jar | Rule |
|---|---|---|
| Bloody Gadfly | 15 | `bloodyGadflySpawn false` |
| Door Knight | 13 | `doorKnightSpawn false` |
| Corpse Fly | 12 | `corpseFlySpawn false` |

Born in Chaos also generates structures: Dark Towers (forest, plains, taiga), Clown Caravans (plains, savanna, taiga), Farm, Observation Towers, Infernal Pumpkin, Mound of Hounds (desert) and Firewell (the four EE volcano biomes, via Structurify). The 19 creator memorial graves are **disabled** in Structurify.

## Creeper Overhaul

One creeper per biome family; vanilla creepers are removed from all of them. All variants enabled. Weight 75, groups of 2, except where noted.

| Creeper | Spawns in |
|---|---|
| Jungle, Bamboo | `#minecraft:is_jungle` |
| Desert | `#minecraft:has_structure/desert_pyramid` |
| Badlands | `#minecraft:has_structure/mineshaft_mesa` |
| Hills | `#minecraft:is_hill`, `minecraft:stony_peaks` |
| Savannah | `#minecraft:is_savanna` |
| Swamp | `#minecraft:has_structure/ruined_portal_swamp` |
| Dark Oak | `minecraft:dark_forest` |
| Spruce | `#minecraft:is_taiga` |
| Snowy | `#forge:is_snowy` |
| Beach | `#minecraft:is_beach` |
| Dripstone | `dripstone_caves`, `lush_caves` |
| Cave | `#creeperoverhaul:where_creepers_spawn` — the union of every row above |
| Mushroom | `minecraft:mushroom_fields` (w10, 1-2) |
| Ocean | `lukewarm_ocean`, `deep_lukewarm_ocean`, `warm_ocean` (w1, 1) |

## Enderman Overhaul

One enderman per biome family; vanilla endermen are removed from all of them. All variants enabled in `config/endermanoverhaul.jsonc`.

| Enderman | Spawns in | w | Group |
|---|---|---|---|
| Badlands | `#minecraft:has_structure/mineshaft_mesa` | 10 | 1-4 |
| Dark Oak | `#minecraft:has_structure/woodland_mansion` | 10 | 1-4 |
| Desert | `.../desert_pyramid`, `.../village_desert`, `#forge:is_desert` | 10 | 1-4 |
| Ice Spikes | `minecraft:ice_spikes` | 10 | 1-4 |
| Savanna | `#minecraft:is_savanna` | 10 | 1-4 |
| Snowy | `.../village_snowy`, `#forge:is_snowy` | 10 | 1-4 |
| Swamp | `.../swamp_hut`, `#forge:is_swamp` | 10 | 1-4 |
| Windswept Hills | `minecraft:windswept_hills` | 10 | 1-4 |
| Flower Fields | `sunflower_plains`, `meadow`, `cherry_grove`, `flower_forest` | 5 | 1-4 |
| Coral | `minecraft:warm_ocean` | 5 | 1-2 |
| Cave | all tags above plus `dripstone_caves`, `lush_caves`, `#forge:is_lush` | 2 | 1-2 |
| Mushroom Fields | `#forge:is_mushroom` | 1 | 1-2 |

Nether and End endermen have their own sections.

## Spider Overhaul

> **In the client instance but not in the live server `mods/`.** `docs/modlist-server.yaml` lists it under Mobs, so this is a deployment gap, not a decision. Until the jar is on the server none of these spiders spawn, and the vanilla spider removals below do not apply.

Six of the mod's eleven spiders have a spawn entry; all are plain `forge:add_spawns`.

| Spider | Spawns in | w | Group |
|---|---|---|---|
| Cavern | `#minecraft:is_overworld` — **every Overworld biome** | 120 | 1-3 |
| Birch | `minecraft:birch_forest`, `minecraft:old_growth_birch_forest` | 100 | 1-2 |
| Desert | `minecraft:desert`, `#minecraft:is_badlands`, `#forge:is_desert` | 100 | 1-3 |
| Ice | `#forge:is_snowy` | 100 | 1-3 |
| Jungle | `#minecraft:is_jungle` | 100 | 1-3 |
| Swamp | `minecraft:swamp`, `minecraft:mangrove_swamp`, `#forge:is_swamp` | 100 | 1-3 |

No spawn entry in 0.0.6 — registered but unreachable without a spawn egg: Mushroom, Ocean, Savanna, Sculk, Taiga Spider.

`minecraft:spider` and `minecraft:cave_spider` are removed **only** in the five themed families (`#spider_overhaul:remove_vanilla_spawn_biomes` = birch, desert, ice, jungle, swamp). Plains, forests, savannas, mountains and oceans keep the vanilla spider *and* gain the Cavern Spider.

Structure: Birch Spidernest, `minecraft:birch_forest` and `minecraft:old_growth_birch_forest`, no spawn overrides.

Config (`SpiderOverhaulConfig`, written as JSON on first run): `spawnWeightMultiplier` scales every weight above, `replaceVanillaSpiders` toggles the removal. Spawn placement itself is entirely the JSON biome modifiers — the mod's `SpawnConditions` class is a stub that says so.

## Variants & Ventures

Biome-appropriate variants of vanilla undead. All four enabled, spawn chance 80 %, minimum Y -64, spawners enabled.

| Mob | Spawns in | w |
|---|---|---|
| Gelid | frozen + ice + snowy collections: `deep_frozen_ocean`, `frozen_peaks`, `frozen_ocean`, `frozen_river`, `ice_spikes`, `snowy_taiga`, `grove`, `snowy_slopes`, `jagged_peaks`, `snowy_plains`, `snowy_beach`, `#forge:is_snowy` | - |
| Murk | `lukewarm_ocean`, `deep_lukewarm_ocean`, `warm_ocean` | 4 |
| Thicket | `#minecraft:is_jungle`, `#forge:is_jungle` | - |
| Verdant | `#minecraft:is_jungle`, `#forge:is_jungle` | - |

`enableBetterStraySpawns` and `enableBetterHuskSpawns` also widen vanilla Stray and Husk spawns into the same snowy and desert collections at 80 % chance.

## Iron's Spells 'n Spellbooks

| Mob | Spawns in | w |
|---|---|---|
| Necromancer | `#minecraft:is_overworld` — any Overworld biome | 18 |
| Ice Spider | Ice Spider Den (`#forge:is_snowy`, minus mountains and oceans); also patrols in `#irons_spellbooks:ice_spider_patrols` (`#forge:is_snowy`) | - |

Archevoker, Apothecarist, Catacombs Zombie, Ancient Knight, Cryomancer, Cultist, The Dead King, Echo of Tyros, Magehunter Vindicator, Priest and Pyromancer are structure-bound — see below.

## Geomancy Plus (Iron's Spells addon)

| Mob | Spawns in | w |
|---|---|---|
| Geo Grandmaster | `#minecraft:is_mountain` | 5 |

## Mowzie's Mobs

Every entry is gated twice: the biome tags below and the `avoid_structures` list in `config/mowziesmobs-common.toml`.

| Mob | Spawns in | Rate | Group | Conditions |
|---|---|---|---|---|
| Foliaath | `#minecraft:is_jungle` | 70 | 1-4 | Dark, on ground/leaves/logs, Y >= 60 |
| Naga | `#is_beach` + `#is_mountain`, or `#is_beach` + `#is_hill`, or `minecraft:stony_shore` | 20 | 1-2 | Dark, must see sky, Y >= 68 |
| Bluff | any biome | 10 | 2-3 | Dark, underground only |
| Lantern | `#minecraft:is_forest` + `#mowziesmobs:is_magical` (dark forest), not snowy | 5 | 2-4 | Dark, Y >= 60 |
| Umvuthana | `#minecraft:is_savanna` | 5 | 1 | On ground or sand, Y >= 60, avoids villages, outposts and groves |
| Elokosa | `#minecraft:is_jungle` | 5 | 1 | Y >= 60, avoids villages and outposts |
| Grottol | any biome except `#forge:is_mushroom` | 2 | 1 | Dark, no sky, Y -65..16, on `#minecraft:base_stone_overworld` |

Frostmaw, Umvuthi, Ferrous Wroughtnaut and Tongbi the Sculptor come with their own structures — see below.

## L_Ender's Cataclysm — natural spawns

| Mob | Spawns in | w | Rolls |
|---|---|---|---|
| Deepling | `deep_ocean`, `deep_lukewarm_ocean`, `deep_cold_ocean`, `deep_frozen_ocean` | 2 | 30 |
| Deepling Angler | same | 2 | 30 |
| Deepling Brute | same | 1 | 50 |
| Deepling Priest | same | 1 | 70 |
| Deepling Warlock | same | 1 | 70 |
| Coral Golem | same | 1 | 70 |
| Amethyst Crab | `minecraft:lush_caves` | 1 | 20 |
| Koboleton | `minecraft:desert`, `#minecraft:is_savanna`, `#minecraft:is_badlands` | 15 | 1 |
| Ignited Berserker | `#cataclysm:berserker_spawn` — ships **empty**, so it never spawns naturally | 7 | 2 |

## Aquamirae

Every Aquamirae spawn is confined to `#aquamirae:ice_maze` — `minecraft:deep_frozen_ocean` and `minecraft:frozen_ocean`.

| Mob | w | Group |
|---|---|---|
| Maw | 200 | 1-4 |
| Spinefish | 40 | 3-9 |
| Abyssal Scyphoid | 30 | 6-16 |
| Anglerfish | 20 | 1-2 |
| Tortured Soul | 10 | 1-4 |
| Pillagers Patrol | 5 | 1 |

Ghost of Captain Cornelia, Mother of the Maze and the Eel are boss encounters inside the Ice Maze structures, not spawn-list entries.

## Artifacts

| Mob | Spawns in |
|---|---|
| Mimic | Replaces chests in dungeons and mineshafts — no biome spawn entry |

---

# The Nether

## Jaden's Nether Expansion

| Mob | Spawns in | w |
|---|---|---|
| Apparition | `minecraft:soul_sand_valley`, `netherexp:black_ice_glaciers` | 30 |
| Vessel | `minecraft:soul_sand_valley`, `netherexp:black_ice_glaciers` | 30 |
| Wisp, Will O' Wisp | Emerge from Ecto Soul Sand blocks by chance, not a spawn list | - |
| Banshee | Sanctum structure only | - |

The three new Nether sub-biomes carry vanilla-only spawn lists:

| Biome | Monsters | Creatures |
|---|---|---|
| `netherexp:exhaust_mire` | zombified piglin w100, ghast w50, piglin w15 | strider w60 |
| `netherexp:black_ice_glaciers` | ghast w50, stray w20, enderman w1 | strider w60 |
| `netherexp:sorrowsquash_pastures` | ghast w50, skeleton w20, enderman w1 | strider w60 |

Ecto Slab, Stampede, Carcass, False Carcass and Warphopper have no biome spawn entry — they are block-, structure- or event-driven.

## Nether Depths Upgrade — lava fauna

| Mob | Spawns in | w | Group |
|---|---|---|---|
| Lava Pufferfish | `crimson_forest`, `warped_forest` | 15 | 1-5 |
| Obsidianfish | `basalt_deltas` | 15 | 1-5 |
| Magma Cube Fish | `basalt_deltas` | 10 | 2-4 |
| Searing Cod | `#minecraft:is_nether` | 10 | 3-6 |
| Soul Sucker | `soul_sand_valley` | 10 | 1-2 |
| Bonefish | `#minecraft:is_nether` | 8 | 1-5 |
| Glowdine | `basalt_deltas`, `soul_sand_valley`, `nether_wastes` | 8 | 4-6 |
| Eyeball Fish | `crimson_forest` | 3 | 2-4 |
| Fortress Grouper | `basalt_deltas`, `#minecraft:has_structure/nether_fortress` | 3 | 1-2 |
| Wither Bonefish | `crimson_forest`, `soul_sand_valley` | 3 | 1-3 |

Blazefish is caught by lava fishing, not spawned.

## Alex's Mobs — Nether

| Mob | Spawns in | w |
|---|---|---|
| Straddler | `minecraft:basalt_deltas` | 70 |
| Warped Toad | `minecraft:warped_forest` | 30 |
| Soul Vulture | `minecraft:soul_sand_valley`, restricted to Nether Fossils by config | 30 |
| Dropbear | `minecraft:nether_wastes` | 19 |
| Crimson Mosquito | `minecraft:crimson_forest` | 15 |
| Laviathan | `#minecraft:is_nether` | 15 |
| Stradpole | `minecraft:basalt_deltas` | 10 |
| Bone Serpent | `#minecraft:is_nether` | 8 |
| Warped Mosco | No biome entry — does not spawn naturally | 1 |

## Enderman Overhaul — Nether

| Enderman | Spawns in | w | Group |
|---|---|---|---|
| Soulsand Valley | `minecraft:soul_sand_valley` | 2 | 1-2 |
| Crimson Forest | `minecraft:crimson_forest` | 2 | 1-2 |
| Nether Wastes | `minecraft:nether_wastes` | 1 | 4 |
| Warped Forest | `minecraft:warped_forest` | 1 | 4 |

---

# The End

## Alex's Mobs — End

| Mob | Spawns in | w |
|---|---|---|
| Mimicube | `#minecraft:is_end` except `the_end`, restricted to End Cities by config | 40 |
| Spectre | `#minecraft:is_end` except `the_end` | 10 |
| Endergrade | `#minecraft:is_end` except `the_end` | 10 |
| Cosmaw | `#minecraft:is_end` except `the_end`, `small_end_islands` | 9 |
| Cosmic Cod | `#minecraft:is_end` | 5 |
| Enderiophage | `#minecraft:is_end` except `the_end`, `end_barrens`, `end_highlands`, `small_end_islands` | 0 |
| Void Worm | No biome entry; summoned with a Mysterious Worm, End only | 0 |

Nullscape removes `end_midlands` and `end_barrens` from End placement — see [`biomes.md`](biomes.md).

## Enderman Overhaul — End

| Enderman | Spawns in | w | Group |
|---|---|---|---|
| End | `#minecraft:is_end`, `#forge:is_end` | 2 | 1-2 |
| End Islands | `#minecraft:has_structure/end_city` | 1 | 1 |

## L_Ender's Cataclysm — End

| Mob | Spawns in |
|---|---|
| Endermaptera | Ruined Citadel (`end_highlands`, `end_midlands`) |

---

# Structure- and boss-bound

These never appear in a biome spawn list. They are placed inside a structure, or summoned.

## Bosses of Mass Destruction

| Boss | Structure | Structure biomes |
|---|---|---|
| Night Lich | Lich Tower | `snowy_taiga`, `snowy_plains`, `snowy_slopes`, `ice_spikes`, `frozen_peaks`, `snowy_beach`, `frozen_ocean` |
| Obsidilith | Obsidilith Arena | `end_highlands`, `end_midlands`, `small_end_islands`, `end_barrens` |
| Nether Gauntlet | Gauntlet Arena | `#minecraft:is_nether`, underground |
| Void Blossom | Void Blossom | Nearly every Overworld biome — oceans, rivers, beaches, mountains, hills, taiga, jungle, forest, plains, swamp, savanna, desert, mushroom fields, ice spikes |

`eternalNighttime` is on for the Lich. Soul Star targets the Lich Tower; Void Lily targets Void Blossom.

## L_Ender's Cataclysm — structures

| Structure | Biomes | Notable occupants |
|---|---|---|
| Cursed Pyramid | `minecraft:desert` | The Harbinger |
| Desert Occupied Village, Desert Site | `minecraft:desert` | Koboleton |
| Sunken City | the four deep oceans | The Leviathan; needs `#minecraft:is_ocean` / `is_river` around it |
| Acropolis | `minecraft:warm_ocean` | Scylla |
| Frosted Prison, Abandoned Spire / Temple / Village | `minecraft:snowy_plains` | Maledictus |
| Burning Arena | `minecraft:nether_wastes` | Ignis |
| Soul Blacksmith | `crimson_forest`, `nether_wastes`, `soul_sand_valley`, `warped_forest` | Netherite Monstrosity |
| Ruined Citadel | `end_highlands`, `end_midlands` | Ender Guardian, Endermaptera |
| Ancient Factory | `#forge:is_underground` | Ancient Remnant |
| Amethyst Nest | `minecraft:lush_caves` | Amethyst Crab |

Ender Guardian, Netherite Monstrosity, The Harbinger, Ancient Remnant and Scylla all have `respawner = true` — their arenas can be re-triggered.

## Mowzie's Mobs — boss structures

All four use `#mowziesmobs:has_structure/has_mowzie_structure` (every Overworld biome), then narrow by the per-boss biome tags in the config:

| Boss | Structure | Config biome filter | Height |
|---|---|---|---|
| Frostmaw | Frostmaw Spawn | `#forge:is_snowy`, not ocean / river / beach / forest / taiga | 50-100 |
| Umvuthi, the Sunbird | Umvuthana Grove | `#minecraft:is_savanna` | 50-100 |
| Ferrous Wroughtnaut | Wrought Chamber | anything but `#minecraft:is_ocean` | 20-50 |
| Tongbi, the Sculptor | Monastery | `#forge:is_peak` | 120-200 |

The Monastery also spawns Bluffs. Frostmaw, Umvuthi and Ferrous Wroughtnaut avoid villages and pillager outposts.

## Iron's Spells 'n Spellbooks — structures

| Structure | Biomes |
|---|---|
| Catacombs | Most Overworld biomes — hills, taiga, jungle, forest, stony shore, ice spikes, desert, savanna, plains, swamp |
| Evoker Fort | `#minecraft:is_hill`, `#minecraft:is_forest`, `#forge:is_desert`, `#forge:is_plains`, savannas |
| Mangrove Hut | `#forge:is_swamp` |
| Mountain Tower | `#minecraft:is_mountain`, `snowy_plains`, `snowy_taiga`, `grove`, `ice_spikes` |
| Pyromancer Tower | `#minecraft:is_hill`, `#minecraft:is_forest`, `#forge:is_plains`, `#forge:is_swamp` |
| Ice Spider Den | `#forge:is_snowy`, minus mountains and oceans |
| Impaled Icebreaker | `deep_frozen_ocean`, `frozen_ocean`, `ice_spikes`, `frozen_peaks` |
| Citadel, Ancient Battleground | `#minecraft:has_structure/bastion_remnant` (Nether) |

## Companions

Every Companions mob is crafted, tamed or summoned. Its five structures spawn vanilla illagers:

| Structure | Biome | Spawns |
|---|---|---|
| Baba Yaga | `#forge:is_swamp` (Structurify) | pillager, witch |
| Factory | `minecraft:plains` | pillager |
| Teddy Tower | `#wythers:is_dark_forest` (Structurify) | pillager, witch, evoker |
| Monkey Temple | all three vanilla jungles + 10 EE jungles (Structurify) | - |
| Tent | `minecraft:nether_wastes` | - |

## Friends & Foes — structures

| Mob | Structure | Biomes |
|---|---|---|
| Iceologer | Iceologer Cabin | `#forge:is_cold/overworld` (Structurify) |
| Illusioner | Illusioner Shack, Illusioner Training Grounds | `#friendsandfoes:collections/taigas` |
| Wildfire | Citadel | `#minecraft:is_nether` |
| Copper Golem | Copper Golem area, Ancient City centre | - |
| Tuff Golem | Stronghold | - |

## Dungeon Now Loading

| Structure | Biomes | Spawns |
|---|---|---|
| Labyrinth | `#dungeonnowloading:without_water` — mushroom fields, stony shore, swamps, snowy and windswept biomes | Hollow, Garhold, Spawner Carrier plus vanilla monsters |
| Temple of Duality | `#minecraft:is_jungle` (Structurify) | Fairkeeper bosses; no ambient monsters |

## The Conjurer

| Mob | Structure | Biome |
|---|---|---|
| Conjurer | Theatre | `minecraft:dark_forest` |

## Hunter's Return

| Mob | Structure | Biomes |
|---|---|---|
| Hunter | Hunter House | `#minecraft:is_taiga`, `#minecraft:is_forest` |

## It Takes a Pillage

| Mob | Source | Biomes |
|---|---|---|
| Archer, Skirmisher, Legioner | Pillager Camp | `plains`, `snowy_plains`, `meadow`, `savanna`, `sunflower_plains` |
| Archer, Skirmisher, Legioner | Bastille | `#forge:is_plains` minus the four vanilla plains — EE plains only (Structurify) |
| Clay Golem | Replaces Iron Golems: 20 % easy, 50 % normal, 100 % hard | Villages |

Pillage Sieges are enabled.

## Grim Kingdoms

15 castle and fort structures, all restricted to cold biomes: `jagged_peaks`, `frozen_peaks`, `grove`, `snowy_beach`, `snowy_plains`, `snowy_slopes`, `snowy_taiga`, `frozen_river`, `ice_spikes`. They add no mobs of their own — they house vanilla and other mods' spawns.

## Realm RPG: Quests and Rewards — NPC camps

| NPC | Structure biomes |
|---|---|
| Angler | `#minecraft:is_river` |
| Cook | `#minecraft:is_plains`, `#minecraft:is_savanna` |
| Lumberjack | `#minecraft:is_taiga`, `dark_forest`, `#forge:is_spooky` |
| Miner | `#minecraft:is_desert`, `#minecraft:is_badlands` |
| Monster Hunter | `#minecraft:is_forest` |
| Piglin Gambler, Headless Skeleton | `#minecraft:is_nether` |

## Summoned bosses

| Boss | Mod | How |
|---|---|---|
| Dark Doppelganger | Dark Doppelganger | Summon Scroll at an altar; `altar_end_only = true`, so the End only |
| Guardian of Gaia | Botania | Beacon ritual |
| Wither | Wither Reincarnated | Vanilla summon, overhauled fight |
| Ender Dragon | True Ending | Vanilla summon, overhauled fight |
| Enchanter | Enchant With Mob | Accompanies enchanted mobs |

---

# Raid-, patrol- and event-only

## Illage and Spillage

Raid-wave counts for waves 1-8, from `config/illageandspillage-common.toml`:

| Mob | Raid waves |
|---|---|
| Igniter | 0, 0, 1, 2, 0, 2, 1, 4 |
| Crocofang | 0, 0, 1, 0, 1, 2, 1, 3 |
| Twittollager | 0, 0, 0, 0, 1, 0, 2, 1 |
| Absorber | 0, 0, 0, 0, 1, 0, 0, 2 |
| Boss Randomizer | 0, 0, 0, 0, 1, 0, 0, 1 |
| Preserver | disabled, all zeroes |
| Engineer | disabled, all zeroes |

Spiritcaller, Freakager and Magispeller have zero direct raid counts — they arrive only through the Boss Randomizer (Spiritcaller or Freakager mid-raid, Magispeller as final boss), one at a time, seasonal bosses on. Chagrin Sentry, Mob Spirit, Imp, Spirit Hand, Ragno and Devastator are boss summons.

Engineer and Preserver are disabled, and the Engineer takes Factory, Hinder, Beeper, Poker and Sniper with it. See [Spawn removals and global controls](#spawn-removals-and-global-controls).

## Raids Enhanced

| Mob | Source |
|---|---|
| Zapper | Raid waves |
| Golem of Last Resort | Raid waves; it is a raider siege golem, not a village defender (`GolemOfLastResort extends FDRaider extends Raider`, targets players, villagers and iron golems) |
| Raider Blimp, Raider Drill | Raid vehicles; the Drill spawns 2-3 raiders and re-burrows up to 3 times |

## Pillager Caravans

Roaming convoys, enabled. First possible after one in-game day (`worldAgeRequired = 24000`); spawn 32 +/- 32 blocks away at light level 4 or below, three chunks clear of a village.

| Placement | Caravans |
|---|---|
| Desert | Miners, Harvesters, Graverobbers |
| Jungle | Harvesters, Exotics, Graverobbers |
| Plains | Harvesters, Dyers, Marauders |
| Snow | Fishers, Hunters, Miners |
| Swamp | Miners, Alchemists, Harvesters |

Weights: small 9 (48 000-tick cooldown), medium 3 (96 000), large 1 (144 000).

## Aquamirae

Pillagers Patrol spawns as a normal Ice Maze monster (w5); the Outpost and Ship structures also force `minecraft:pillager` and `minecraft:vindicator` spawns.

## Illagers Wear Armor

Adds armoured variants of existing illagers — no new spawn entries.

---

# Never spawns naturally

| Mod | Entities | Note |
|---|---|---|
| Realm RPG: Fallen Adventurers | none | Registers **blocks only** — webbed, mossy, burnt, dripstone skeleton props and the like, scattered as worldgen features in every biome |
| Botania | Pixie, Pink Wither, Guardian of Gaia | Ritual- or flower-driven |
| Create | Contraptions, Package, Seat | Machinery, not creatures |
| Supplementaries | Red Merchant, Hat Stand | Red Merchant is a wandering-trader variant |
| Let's Do: Vinery | Wandering Winemaker, Mule | Trader spawn, not biome-based |
| Gravestone | Ghost of Player | Spawns at a player's grave |
| Ascendant Arcana | Blazebolt, Singularity | Spell projectiles |
| Iron's Spells x I&S compat | Summoned Ragno, Mob Spirit, Spirit Hand, Chagrin Sentry | Spell summons |
| Medieval Origins | Summoner's Zombie / Skeleton / Wither Skeleton | Origin power summons |
| Farmer's Delight, Musket Mod, Unique Accessories | projectiles | - |

---

# Spawn removals and global controls

| Source | Effect |
|---|---|
| Creeper Overhaul | Removes `minecraft:creeper` from every biome in `#creeperoverhaul:where_creepers_spawn` |
| Enderman Overhaul | Removes `minecraft:enderman` from every biome in `#endermanoverhaul:all_spawns` |
| Spider Overhaul | Removes `minecraft:spider` and `minecraft:cave_spider` from birch, desert, ice, jungle and swamp biomes only — **not yet on the server** |
| Aquamirae | Removes zombie, zombie villager, drowned, skeleton, creeper, spider, enderman, witch and slime from `#aquamirae:ice_maze` |
| Expanded Ecosphere | Rewrites all 52 surviving vanilla Overworld biomes' spawn lists, and gives its 188 new biomes vanilla-only spawn lists; modded mobs reach them through biome tags |
| Illage and Spillage | **Engineer and Preserver disabled.** Both raid counts zeroed in `configuration_server/mod_configs_server/illageandspillage-common.toml`. The mod adds no biome spawn modifiers (its only worldgen data file is `minecraft/tags/entity_types/raiders.json`) and neither mob is in `bossrandomizer_bosstypes` or `bossrandomizer_finalbosstypes`, so raid waves were their only route in. The Engineer takes 5 more entities with it: Factory and Hinder (`EngineerMachine`), then Beeper, Poker and Sniper (`FactoryMinion`, spawned by the Factory). Illashooter, Dispenser and Crashager are unaffected |
| Mob Control | Config is stock — no custom rules, `[[mob]] name="*"` allows everything |
| Structurify | Disables only the 19 Born in Chaos creator graves and `supplementaries:way_sign`; also rewrites the biome list of 47 structures (see [`structures.md`](structures.md)) |
| Structure Layout Optimizer, Structure Essentials | Performance only, no spawn changes |

To confirm the Illage and Spillage disable after deploying the config: run a raid at each bad omen level and check that no Engineer, Preserver, Factory, Hinder, Beeper, Poker or Sniper appears.

---

## How this list was produced

1. Extracted `data/*/forge/biome_modifier/**` from all 172 jars in the live server's `mods/` and parsed every `forge:add_spawns` and `forge:remove_spawns` entry.
2. Resolved every `#namespace:tag` reference against the biome tags shipped in the same jars.
3. For mods whose modifier is a bare marker with no `biomes` field (Alex's Mobs, Friends & Foes, Cataclysm, Mowzie's, Companions) read the matching server config in `config/` — and for Cataclysm disassembled `CMWorldRegistry` to map each mob to its biome tag.
4. Extracted `data/*/worldgen/structure/*.json` for structure biome bindings and `spawn_overrides`, then applied the Structurify overrides from the repo copy in `configuration_server/mod_configs_server/` (the live `config/structurify.json` is an older auto-regenerated dump).
5. Cross-checked mob rosters against each jar's `assets/<ns>/lang/en_us.json`.

Re-run after adding, removing or reconfiguring any mob mod. Weights and toggles here come from the live server config, not mod defaults.
