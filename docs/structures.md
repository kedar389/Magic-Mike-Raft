# Magic Minecraft Server — Structure Reference

Every structure that **actually generates** on this server, grouped by dimension and biome family. Companion to [`biomes.md`](biomes.md) (which biomes exist) and [`modpack-wiki.md`](modpack-wiki.md) (gameplay changes).

**335 structures from 34 mods: 272 Overworld · 57 Nether · 6 End.** 20 are switched off in Structurify and 3 more can never generate; see [Not generating](#not-generating) and [Known placement problems](#known-placement-problems).

Biome lists here are the **effective** ones: the mod's own biome tag, with Structurify's overrides applied, then filtered to biomes that the world actually places. Tag entries for mods that are not installed (BYG, Biomes O' Plenty, Better Nether, Regions Unexplored, …) are dropped, as are `minecraft:cherry_grove`, `minecraft:end_midlands` and `minecraft:end_barrens`, which are registered but never placed on this server.

## Where structures come from

| Source | Structures | Role |
|---|---|---|
| **Dungeons & Taverns** (`nova_structures:`) | 58 | Largest structure pack. Its sub-mods also rewrite vanilla `stronghold`, `ancient_city` and `pillager_outpost`. |
| **Born in Chaos** (`born_in_chaos_v1:`) | 31 | Undead towers, farms, caravans and 19 memorial graves (all graves disabled here). |
| **Vanilla + DnT overhauls** (`minecraft:`) | 33 | Vanilla structure set, several definitions replaced by Dungeons & Taverns. |
| **BetterArcheology** (`betterarcheology:`) | 25 | Dig sites, fossils, catacombs. |
| **Explorify** (`explorify:`) | 23 | Small exploration set — taverns, caches, watchtowers, guide posts. |
| **Formations Nether** (`formationsnether:`) | 21 | Small Nether props and arenas, very dense. |
| **Feur Jungle** (`feur_extension_jungle:`) | 15 | Jungle trees, ruins and a village. |
| **Grim Kingdoms** (`grim_kingdoms:`) | 15 | Snowbound castles and keeps. |
| **L_Ender's Cataclysm** (`cataclysm:`) | 14 | Boss arenas and dungeons. |
| **YUNG's Better Mineshafts** (`bettermineshafts:`) | 13 | 13 biome-specific mineshaft variants replacing the vanilla one. |
| **Integrated Villages** (`integrated_villages:`) | 12 | Extra village types. |
| **Expanded Ecosphere** (`wythers:`) | 10 | Biome-flavoured landmarks and three badlands villages. |
| Everything else | 65 | Aquamirae, BoMD, Companions, Conjurer, Dungeon Now Loading, Ender's Delight, Friends & Foes, Geomancy Plus, Hunter's Return, Iron's Spells, It Takes a Pillage, Jaden's Nether Expansion, Mowzie's Mobs, Nether Depths Upgrade, Nullscape, Realm RPG Quests, Ribbits, Supplementaries, YUNG's Better Desert Temples / Dungeons / Jungle Temples / Nether Fortresses / Witch Huts. |

## How to read this

| Column | Meaning |
|---|---|
| **Structure** | Registry ID. `/locate structure <id>` takes these verbatim. |
| **Mod** | Mod that ships the structure definition. |
| **Biomes** | Biome families the structure can start in. `EE` = Expanded Ecosphere (`wythers:`), `NE` = Jaden's Nether Expansion (`netherexp:`), `NS` = Nullscape (`nullscape:`). Counts are exact biome counts, not families. |
| **Spacing** | `spacing/separation` in chunks — average gap between attempts / guaranteed minimum gap. `x0.8` is the per-attempt spawn chance. Lower spacing = more common. `—` = ring placement, which has no spacing/separation. |
| **Notes** | `underground` = generates below the surface, `late` = placed after terrain, `raw gen` = placed before terrain. **§** = the biome list comes from Structurify, not from the mod. **off** = disabled, will not generate. |

Sections are by **where a structure starts**. Something marked `underground` still picks its spot from the surface biome above it.

## What Structurify changes

`configuration_server/mod_configs_server/structurify.json` is the single place where structure placement is retuned. It is applied on top of every mod's own biome list, and it is the reason most structures here reach Expanded Ecosphere biomes at all — mods ship vanilla-only biome tags, and EE replaced the Overworld.

| Setting | Value | Effect |
|---|---|---|
| `disable_all_structures` | `false` | Structures generate normally. |
| `prevent_structure_overlap` | `true` | Two structures never intersect; the later one is skipped. |
| `global_spacing_and_separation_modifier` | `1.0` | No global density change — spacing numbers in the tables are the real ones. |

On top of that:

- **197 per-structure entries.** 44 replace the biome list outright (marked **§** in the tables), 6 subtract specific biomes, 20 switch the structure off.
- **117 structures get a biome check**, almost all of them `BLACKLIST` on `#minecraft:is_river` / `#minecraft:is_ocean` — this stops land structures spawning half-submerged where an EE river or coast cuts through their biome.
- **149 structures get a flatness check** (`AUTO`, max 21 blocks of height difference), so buildings do not hang off EE's much steeper terrain.
- **6 whole namespaces** — `born_in_chaos_v1`, `grim_kingdoms`, `hunters_return`, `nova_structures`, `realmrpg_quests`, `takesapillage` — carry namespace-wide flatness and river/ocean rules.

Two placement rewrites are worth knowing about because they deliberately split vanilla from EE:

| Structure | Effect |
|---|---|
| `explorify:supply_cache/taiga` | Whitelisted to `#minecraft:is_taiga`, then the four vanilla taigas are subtracted — it only spawns in EE taigas. |
| `takesapillage:bastille` | Whitelisted to `#forge:is_plains`, then the four vanilla plains are subtracted — EE plains only. `takesapillage:pillager_camp` keeps the vanilla plains. |

## Known placement problems

| Structure | Problem |
|---|---|
| `nova_structures:well_birch` | Structurify whitelists `#c:birch_forest` and `#forge:forest/birch`. Neither tag exists on this server, so the whitelist is empty and the Birch Well **never generates**. The other five Dungeons & Taverns wells are fine. Fixable by whitelisting the same biomes `nova_structures:tavern_birch` uses. |
| `nova_structures:firewatch_tower_cherry`, `nova_structures:tavern_cherry` | Keyed to `minecraft:cherry_grove`, the one vanilla Overworld biome Expanded Ecosphere drops from its placement. Both **never generate**. Fixable by pointing them at an EE flower/sakura biome. |
| `cataclysm:ruined_citadel`, `endersdelight:end_house` | Listed for `end_highlands` + `end_midlands`, but Nullscape never places `end_midlands` — they are effectively Highlands-only, so roughly half as common as their config suggests. |
| `minecraft:monument` | The modlist lists YUNG's Better Ocean Monuments and `betteroceanmonuments-forge-1_20.toml` is in the server config, but **the jar is not installed** — plain vanilla monuments generate. |

---

# Overworld

272 structures.

## Anywhere / most biomes

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterdungeons:skeleton_dungeon` | YUNG's Better Dungeons | any land biome except oceans, rivers, deserts, badlands and mushroom fields — 32 vanilla + 79 EE | `46/23` | underground |
| `betterdungeons:small_dungeon` | YUNG's Better Dungeons | any land biome except oceans, rivers, deserts, badlands and mushroom fields — 32 vanilla + 79 EE | `10/6` | underground |
| `betterdungeons:spider_dungeon` | YUNG's Better Dungeons | any land biome except oceans, rivers, deserts, badlands and mushroom fields — 32 vanilla + 79 EE | `44/22` | underground |
| `betterdungeons:zombie_dungeon` | YUNG's Better Dungeons | any land biome except oceans, rivers, deserts, badlands and mushroom fields — 32 vanilla + 79 EE | `48/24` | underground |
| `bosses_of_mass_destruction:void_blossom` | Bosses of Mass Destruction | most Overworld biomes — 48 vanilla + 89 EE | `64/24` |  |
| `dungeonnowloading:labyrinth` | Dungeon Now Loading | all 39 vanilla Overworld land biomes — no EE biomes | `80/20` |  |
| `integrated_villages:airship_village` | Integrated Villages | every Overworld biome (240) | `115/90` |  |
| `irons_spellbooks:catacombs` | Iron's Spells | forests, taigas, jungles, plains, savannas, deserts, windswept hills and caves — 29 vanilla + 61 EE | `72/48` | underground |
| `irons_spellbooks:evoker_fort` | Iron's Spells | forests, plains, savannas, deserts and windswept hills — 17 vanilla + 36 EE | `85/50` |  |
| `minecraft:mineshaft` | Vanilla / DnT | most Overworld biomes — 48 vanilla + 89 EE | `1/0 x0.004` | underground |
| `minecraft:ruined_portal` | Vanilla / DnT | temperate forests, taigas, plains, rivers and beaches — 22 vanilla + 58 EE | `40/15` |  |
| `minecraft:stronghold` | Vanilla / DnT | every Overworld biome (240) | — | underground |
| `mowziesmobs:frostmaw_spawn` | Mowzie's Mobs | every Overworld biome (240) | `25/8` |  |
| `mowziesmobs:monastery` | Mowzie's Mobs | every Overworld biome (240) | `25/8` |  |
| `mowziesmobs:umvuthana_grove` | Mowzie's Mobs | every Overworld biome (240) | `25/8` |  |
| `mowziesmobs:wrought_chamber` | Mowzie's Mobs | every Overworld biome (240) | `15/5` |  |
| `nova_structures:illager_camp` | Dungeons & Taverns | most Overworld biomes — 34 vanilla + 103 EE | `26/12` |  |
| `nova_structures:illager_hideout` | Dungeons & Taverns | most Overworld biomes — 34 vanilla + 103 EE | `200/12` | underground |

## Caves & underground

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `bettermineshafts:mineshaft_dripstone` | YUNG's Better Mineshafts | `dripstone_caves` | `1/0 x0.003` | underground |
| `bettermineshafts:mineshaft_lush` | YUNG's Better Mineshafts | `lush_caves` | `1/0 x0.003` | underground |
| `bettermineshafts:mineshaft_mushroom` | YUNG's Better Mineshafts | `mushroom_fields`, `EE fungous_dripstone_caves`, `EE lush_fungous_dripstone_caves`, `EE lush_shroom_caves`, `EE mushroom_caves`, `EE mushroom_island`, `EE thermal_taiga_crags` | `1/0 x0.003` | underground |
| `cataclysm:amethyst_nest` | L_Ender's Cataclysm | `lush_caves` | `32/8` | underground |
| `cataclysm:ancient_factory` | L_Ender's Cataclysm | caves/underground — 3 vanilla + 14 EE | `112/70` | underground |
| `minecraft:ancient_city` | Vanilla / DnT | `deep_dark` | `24/8` | underground |

## Plains & grasslands

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:light_temple` | BetterArcheology | plains and taigas — 3 vanilla + 18 EE | `32/16` |  |
| `bettermineshafts:mineshaft_oak` | YUNG's Better Mineshafts | plains/grasslands + savannas — 4 vanilla + 10 EE | `1/0 x0.003` | underground |
| `born_in_chaos_v1:clown_caravan_plains` | Born in Chaos | `plains` | `120/8` |  |
| `born_in_chaos_v1:dark_tower_plain` | Born in Chaos | `meadow`, `plains`, `snowy_plains`, `sunflower_plains` | `85/14` |  |
| `born_in_chaos_v1:farm` | Born in Chaos | `meadow`, `plains`, `savanna`, `sunflower_plains` | `65/6` |  |
| `born_in_chaos_v1:grave_darktitan` | Born in Chaos | `meadow`, `plains`, `sunflower_plains` | `105/15` | **off** |
| `born_in_chaos_v1:observation_tower_plains` | Born in Chaos | `plains` | `200/12` |  |
| `companions:companions_factory` | Companions | `plains` | `80/10` |  |
| `explorify:farmstead` | Explorify | `meadow`, `plains`, `EE autumnal_plains`, `EE cold_plains`, `EE cool_plains`, `EE highland_plains`, `EE subtropical_grassland` | `38/28 x0.8` |  |
| `explorify:tavern` | Explorify | all plains — 4 vanilla + 12 EE | `36/18 x0.8` | § |
| `explorify:watchtower/plains` | Explorify | `meadow`, `plains`, `EE autumnal_plains`, `EE cold_plains`, `EE cool_plains`, `EE highland_plains`, `EE subtropical_grassland` | `48/24 x0.75` |  |
| `integrated_villages:marketstead_village` | Integrated Villages | plains — 2 vanilla + 10 EE | `26/19` |  |
| `integrated_villages:mossy_mounds` | Integrated Villages | plains — 2 vanilla + 10 EE | `26/19` |  |
| `minecraft:pillager_outpost` | Vanilla / DnT | plains, savanna, desert, taiga, meadow and peaks — 11 vanilla + 9 EE | `32/8 x0.2` |  |
| `minecraft:village_plains` | Vanilla / DnT | `meadow`, `plains`, `EE autumnal_plains`, `EE cold_plains`, `EE cool_plains`, `EE highland_plains`, `EE subtropical_grassland` | `34/8` |  |
| `takesapillage:pillager_camp` | It Takes a Pillage | `meadow`, `plains`, `savanna`, `snowy_plains`, `sunflower_plains` | `32/12` | underground |

## Forests & woodlands

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:archeologist_camp_grassy` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `32/16` |  |
| `betterarcheology:fossil_chicken` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `24/12` |  |
| `betterarcheology:fossil_chicken_birch` | BetterArcheology | `birch_forest`, `old_growth_birch_forest`, `EE autumnal_birch_forest`, `EE birch_swamp`, `EE birch_taiga`, `EE warm_birch_forest` | `24/12` |  |
| `betterarcheology:fossil_creeper` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `24/12` |  |
| `betterarcheology:fossil_sheep_0` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `24/12` |  |
| `betterarcheology:fossil_wolf` | BetterArcheology | temperate forests and taiga — 7 vanilla + 29 EE | `24/12` |  |
| `betterarcheology:mott` | BetterArcheology | temperate forests and taiga — 7 vanilla + 29 EE | `32/16` |  |
| `betterarcheology:stonehenge_grassy` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `32/16` |  |
| `betterarcheology:tumulus_grassy` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `32/16` |  |
| `betterarcheology:villager_grave` | BetterArcheology | temperate forests, plains and taiga — 10 vanilla + 39 EE | `24/12` |  |
| `bettermineshafts:mineshaft_overgrown` | YUNG's Better Mineshafts | temperate forests — 6 vanilla + 21 EE | `1/0 x0.003` | underground |
| `born_in_chaos_v1:dark_tower_forest` | Born in Chaos | temperate forests — 6 vanilla + 21 EE | `75/14` |  |
| `born_in_chaos_v1:grave_2003wise` | Born in Chaos | `dark_forest` | `95/10` | **off** |
| `born_in_chaos_v1:grave_euthymia` | Born in Chaos | `dark_forest` | `100/10` | **off** |
| `born_in_chaos_v1:grave_mike_and_rory` | Born in Chaos | `flower_forest` | `85/6` | **off** |
| `born_in_chaos_v1:grave_orion` | Born in Chaos | `birch_forest`, `old_growth_birch_forest` | `100/12` | **off** |
| `born_in_chaos_v1:grave_plug` | Born in Chaos | `dark_forest` | `100/10` | **off** |
| `born_in_chaos_v1:grave_somfunambulist` | Born in Chaos | `forest` | `100/12` | **off** |
| `born_in_chaos_v1:infernal_pumpkin` | Born in Chaos | vanilla forests, plains, savanna, swamp and taiga only | `36/6` |  |
| `born_in_chaos_v1:observation_tower_forest` | Born in Chaos | temperate forests — 6 vanilla + 21 EE | `65/12` |  |
| `companions:companions_teddytower` | Companions | dark forest and its EE relatives — 1 vanilla + 10 EE | `80/30` | § |
| `conjurer_illager:theatre` | Conjurer | `dark_forest` | `40/? x0.5` |  |
| `explorify:dark_forest_settlement` | Explorify | dark forest and its EE relatives — 1 vanilla + 10 EE | `32/16 x0.8` | § |
| `explorify:mausoleum` | Explorify | temperate forests — 8 vanilla + 21 EE | `48/16 x0.8` |  |
| `explorify:supply_cache/birch` | Explorify | `birch_forest`, `old_growth_birch_forest` | `32/20 x0.75` |  |
| `explorify:supply_cache/dark` | Explorify | dark forest and its EE relatives — 1 vanilla + 10 EE | `32/20 x0.75` | § |
| `explorify:supply_cache/forest` | Explorify | `flower_forest`, `forest` | `32/20 x0.75` |  |
| `hunters_return:hunter_house` | Hunter's Return | temperate forests and taigas — 10 vanilla + 40 EE | `26/6` |  |
| `integrated_villages:quark/minka_village` | Integrated Villages | `flower_forest`, `EE autumnal_flower_forest`, `EE flowering_pantanal`, `EE spring_flower_forest` | `26/19` |  |
| `integrated_villages:tavern_village` | Integrated Villages | forests, taigas and wetlands — 5 vanilla + 29 EE | `26/19` |  |
| `irons_spellbooks:pyromancer_tower` | Iron's Spells | temperate forests, plains and wetlands — 15 vanilla + 55 EE | `85/50` | underground |
| `minecraft:mansion` | Vanilla / DnT | dark forest and its EE relatives — 1 vanilla + 10 EE | `80/20` |  |
| `minecraft:trail_ruins` | Vanilla / DnT | forests, taigas and jungles — 13 vanilla + 58 EE | `34/8` | underground |
| `nova_structures:bunker` | Dungeons & Taverns | temperate forests, taigas and plains — 13 vanilla + 57 EE | `50/10` |  |
| `nova_structures:firewatch_tower_birch` | Dungeons & Taverns | `birch_forest`, `old_growth_birch_forest`, `EE autumnal_birch_forest`, `EE birch_swamp`, `EE birch_taiga`, `EE warm_birch_forest` | `50/15` |  |
| `nova_structures:firewatch_tower_dark_oak` | Dungeons & Taverns | dark forest, deep dark and EE dark variants — 2 vanilla + 8 EE | `50/15` |  |
| `nova_structures:firewatch_tower_forest` | Dungeons & Taverns | `forest` | `50/15` |  |
| `nova_structures:illager_manor` | Dungeons & Taverns | dark forest, deep dark and EE dark variants — 2 vanilla + 8 EE | `80/20` |  |
| `nova_structures:ruin_town` | Dungeons & Taverns | temperate forests, taigas and plains — 13 vanilla + 57 EE | `60/12` |  |
| `nova_structures:tavern_birch` | Dungeons & Taverns | `birch_forest`, `old_growth_birch_forest`, `EE autumnal_birch_forest`, `EE birch_swamp`, `EE birch_taiga`, `EE warm_birch_forest` | `44/15` |  |
| `nova_structures:tavern_dark_oak` | Dungeons & Taverns | dark forest, deep dark and EE dark variants — 2 vanilla + 8 EE | `44/15` |  |
| `nova_structures:tavern_oak` | Dungeons & Taverns | oak forest and plains — 3 vanilla + 20 EE | `44/15` |  |
| `nova_structures:undead_crypt` | Dungeons & Taverns | temperate forests, taigas and plains — 13 vanilla + 57 EE | `100/20` | underground |
| `nova_structures:underground_house` | Dungeons & Taverns | temperate forests, taigas and plains — 13 vanilla + 57 EE | `20/10` | underground |
| `nova_structures:well_dark_oak` | Dungeons & Taverns | dark forest, deep dark and EE dark variants — 2 vanilla + 8 EE | `40/15` |  |
| `nova_structures:well_oak` | Dungeons & Taverns | oak forest and plains — 3 vanilla + 20 EE | `40/15` |  |
| `nova_structures:wild_ruin` | Dungeons & Taverns | temperate forests, taigas and plains — 13 vanilla + 57 EE | `23/18` |  |
| `realmrpg_quests:monster_hunters` | Realm RPG Quests | temperate forests — 6 vanilla + 21 EE | `22/20` |  |

## Taiga & boreal

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:catacombs` | BetterArcheology | jungles and taigas — 4 vanilla + 26 EE | `32/16` |  |
| `bettermineshafts:mineshaft_spruce` | YUNG's Better Mineshafts | all taigas — 4 vanilla + 19 EE | `1/0 x0.003` | underground |
| `born_in_chaos_v1:clown_caravan_taiga` | Born in Chaos | all taigas — 4 vanilla + 19 EE | `80/8` |  |
| `born_in_chaos_v1:dark_tower_taiga` | Born in Chaos | all taigas — 4 vanilla + 19 EE | `75/10` |  |
| `born_in_chaos_v1:grave_derivas` | Born in Chaos | `old_growth_pine_taiga` | `90/10` | **off** |
| `born_in_chaos_v1:grave_feral` | Born in Chaos | `old_growth_spruce_taiga` | `95/9` | **off** |
| `born_in_chaos_v1:gravecarrionexe` | Born in Chaos | `old_growth_spruce_taiga` | `100/9` | **off** |
| `explorify:campsite` | Explorify | all taigas — 4 vanilla + 19 EE | `32/16 x0.8` | § |
| `explorify:supply_cache/taiga` | Explorify | EE taigas only — the four vanilla taigas are excluded — 19 EE | `32/20 x0.75` | § |
| `explorify:watchtower/taiga` | Explorify | all taigas — 4 vanilla + 19 EE | `48/24 x0.75` | § |
| `friendsandfoes:illusioner_shack` | Friends & Foes | all taigas — 4 vanilla + 19 EE | `40/10` |  |
| `friendsandfoes:illusioner_training_grounds` | Friends & Foes | all taigas — 4 vanilla + 19 EE | `40/10` |  |
| `integrated_villages:cabin_village` | Integrated Villages | taigas and dark forest — 6 vanilla + 22 EE | `26/19` |  |
| `minecraft:village_taiga` | Vanilla / DnT | all taigas except EE temperate rainforest — 4 vanilla + 16 EE | `34/8` | § |
| `nova_structures:creeping_crypt` | Dungeons & Taverns | taiga and its EE relatives — 1 vanilla + 8 EE | `100/10` |  |
| `nova_structures:firewatch_tower_taiga` | Dungeons & Taverns | taigas — 3 vanilla + 16 EE | `50/15` |  |
| `nova_structures:tavern_spruce` | Dungeons & Taverns | taigas — 3 vanilla + 16 EE | `44/15` |  |
| `nova_structures:well_spruce` | Dungeons & Taverns | taigas — 3 vanilla + 16 EE | `40/15` |  |
| `realmrpg_quests:lumberjacks` | Realm RPG Quests | taigas and dark forests — 6 vanilla + 26 EE | `24/20` |  |
| `wythers:giant_fallen_log` | Expanded Ecosphere | `old_growth_pine_taiga` | `4/2` |  |

## Snowy & cold

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `aquamirae:outpost` | Aquamirae | `deep_frozen_ocean`, `frozen_ocean` | `18/10` | late |
| `aquamirae:shelter` | Aquamirae | `deep_frozen_ocean`, `frozen_ocean` | `40/20` | late |
| `aquamirae:ship` | Aquamirae | `deep_frozen_ocean`, `frozen_ocean` | `25/10` | late |
| `aquamirae:surface/arch` | Aquamirae | `deep_frozen_ocean`, `frozen_ocean` | `4/0` | late |
| `aquamirae:surface/spiral` | Aquamirae | `deep_frozen_ocean`, `frozen_ocean` | `4/0` | late |
| `bettermineshafts:mineshaft_ice` | YUNG's Better Mineshafts | `frozen_peaks`, `ice_spikes`, `EE glacial_cliffs`, `EE ice_cap`, `EE snowy_bog` | `1/0 x0.003` | underground |
| `bettermineshafts:mineshaft_spruce_snowy` | YUNG's Better Mineshafts | `jagged_peaks`, `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `1/0 x0.003` | underground |
| `born_in_chaos_v1:grave_memesus` | Born in Chaos | `snowy_plains`, `snowy_slopes` | `101/11` | **off** |
| `born_in_chaos_v1:grave_petasi` | Born in Chaos | `snowy_plains`, `snowy_taiga` | `90/10` | **off** |
| `born_in_chaos_v1:grave_rotborne` | Born in Chaos | `snowy_plains` | `97/7` | **off** |
| `bosses_of_mass_destruction:lich_tower` | Bosses of Mass Destruction | `frozen_ocean`, `frozen_peaks`, `ice_spikes`, `snowy_beach`, `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `100/50` |  |
| `cataclysm:abandoned_spire` | L_Ender's Cataclysm | `snowy_plains` | `30/20` |  |
| `cataclysm:abandoned_temple` | L_Ender's Cataclysm | `snowy_plains` | `30/20` |  |
| `cataclysm:abandoned_village` | L_Ender's Cataclysm | `snowy_plains` | `30/20` |  |
| `cataclysm:frosted_prison` | L_Ender's Cataclysm | `snowy_plains` | `80/50` |  |
| `explorify:guide_post_cold` | Explorify | cold biomes, including cold oceans and taiga — 15 vanilla + 21 EE | `32/10 x0.75` | § |
| `friendsandfoes:iceologer_cabin` | Friends & Foes | cold biomes, including cold oceans and taiga — 15 vanilla + 21 EE | `40/10` | § |
| `grim_kingdoms:blackridge_watch` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:chill_hallmanor` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:decrown_monolith` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:frostbloom_bastion` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:frozen_onion_fort` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:glacierfall_keep` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:glimmervault_fortress` | Grim Kingdoms | `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `50/30` |  |
| `grim_kingdoms:shardspire_castle` | Grim Kingdoms | `frozen_peaks`, `frozen_river`, `grove`, `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `50/30` |  |
| `grim_kingdoms:silentgale_citadel` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:snowgrave_citadel` | Grim Kingdoms | `snowy_plains`, `snowy_taiga` | `50/30` |  |
| `grim_kingdoms:stonyfell_outpost` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:travelers_lodge` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `grim_kingdoms:whitewarden_keep` | Grim Kingdoms | `frozen_river`, `grove`, `ice_spikes`, `snowy_beach`, `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `50/30` |  |
| `grim_kingdoms:winterhold_citadel` | Grim Kingdoms | `grove`, `snowy_plains`, `snowy_taiga` | `50/30` |  |
| `grim_kingdoms:zweiberg` | Grim Kingdoms | vanilla snowy and icy biomes only — no EE biomes | `50/30` |  |
| `irons_spellbooks:ice_spider_den` | Iron's Spells | snowy and icy biomes — 10 vanilla + 15 EE | `85/50` | underground |
| `irons_spellbooks:impaled_icebreaker` | Iron's Spells | `deep_frozen_ocean`, `frozen_ocean`, `frozen_peaks`, `ice_spikes` | `75/50` | underground |
| `minecraft:igloo` | Vanilla / DnT | `snowy_plains`, `snowy_slopes`, `snowy_taiga` | `32/8` |  |
| `minecraft:village_snowy` | Vanilla / DnT | `snowy_plains` | `34/8` |  |
| `nova_structures:stray_fort` | Dungeons & Taverns | snowy biomes — 3 vanilla + 6 EE | `100/16` |  |
| `nova_structures:tavern_snowy` | Dungeons & Taverns | snowy and icy biomes — 10 vanilla + 15 EE | `44/15` | § |

## Mountains & highlands

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `explorify:guide_post_warm` | Explorify | `stony_peaks` | `32/10 x0.75` |  |
| `gtbcs_geomancy_plus:geomancer_tower` | Geomancy Plus | peaks, highlands and crags — 9 vanilla + 17 EE | `40/25` | § |
| `irons_spellbooks:mountain_tower` | Iron's Spells | peaks, groves, snowy highlands and volcanoes — 9 vanilla + 9 EE | `45/33` |  |
| `minecraft:ruined_portal_mountain` | Vanilla / DnT | peaks, highlands, badlands and windswept biomes — 14 vanilla + 21 EE | `40/15` |  |

## Savannas

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `bettermineshafts:mineshaft_acacia` | YUNG's Better Mineshafts | `savanna`, `savanna_plateau` | `1/0 x0.003` | underground |
| `born_in_chaos_v1:clown_caravan_savanna` | Born in Chaos | all savannas — 3 vanilla + 18 EE | `90/10` |  |
| `explorify:watchtower/savanna` | Explorify | `savanna`, `EE dry_savanna`, `EE wooded_savanna` | `48/24 x0.75` |  |
| `integrated_villages:mediterranean_village` | Integrated Villages | all savannas — 3 vanilla + 18 EE | `26/19` |  |
| `minecraft:village_savanna` | Vanilla / DnT | all savannas — 3 vanilla + 18 EE | `34/8` | § |
| `nova_structures:firewatch_tower_savanna` | Dungeons & Taverns | all savannas — 3 vanilla + 18 EE | `50/15` |  |
| `nova_structures:tavern_acacia` | Dungeons & Taverns | all savannas — 3 vanilla + 18 EE | `44/15` |  |
| `nova_structures:well_savana` | Dungeons & Taverns | all savannas — 3 vanilla + 18 EE | `40/15` |  |
| `realmrpg_quests:cooks` | Realm RPG Quests | plains and savannas — 7 vanilla + 28 EE | `26/16` |  |
| `takesapillage:bastille` | It Takes a Pillage | savannas + plains/grasslands + snowy/cold — 12 EE | `32/12` | underground, § |
| `wythers:baobab_savanna` | Expanded Ecosphere | `savanna`, `EE dry_savanna`, `EE wooded_savanna` | `16/8` |  |

## Deserts & scrub

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:archeologist_camp_sand` | BetterArcheology | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `32/16` |  |
| `betterarcheology:buried_ruins_sand` | BetterArcheology | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `32/16` |  |
| `betterarcheology:desert_obelisk` | BetterArcheology | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `32/16` |  |
| `betterarcheology:ruins_sand` | BetterArcheology | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `32/16` |  |
| `betterdeserttemples:desert_temple` | YUNG's Better Desert Temples | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `30/20` |  |
| `bettermineshafts:mineshaft_desert` | YUNG's Better Mineshafts | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `1/0 x0.003` | underground |
| `born_in_chaos_v1:mound_of_hounds` | Born in Chaos | `desert` | `50/3` |  |
| `cataclysm:cursed_pyramid` | L_Ender's Cataclysm | `desert` | `80/50` |  |
| `cataclysm:desert_occupied_village` | L_Ender's Cataclysm | `desert` | `30/20` |  |
| `cataclysm:desert_site` | L_Ender's Cataclysm | `desert` | `30/20` |  |
| `explorify:desert_shrine` | Explorify | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_lakes`, `EE wooded_desert` | `24/8` |  |
| `explorify:supply_cache/desert` | Explorify | all deserts — 1 vanilla + 13 EE | `32/20 x0.75` | § |
| `integrated_villages:oasis_village` | Integrated Villages | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `26/19` |  |
| `minecraft:desert_pyramid` | Vanilla / DnT | `desert` | `32/8` | § |
| `minecraft:ruined_portal_desert` | Vanilla / DnT | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_lakes`, `EE desert_pinnacles` | `40/15` |  |
| `minecraft:village_desert` | Vanilla / DnT | `desert` | `34/8` |  |
| `nova_structures:desert_ruins` | Dungeons & Taverns | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `80/12` |  |
| `nova_structures:tavern_desert` | Dungeons & Taverns | `desert`, `EE cactus_desert`, `EE deep_desert`, `EE desert_beach`, `EE desert_island`, `EE desert_pinnacles`, `EE guelta`, `EE wooded_desert` | `44/15` |  |
| `realmrpg_quests:miners` | Realm RPG Quests | dry biomes — savanna, badlands, desert and scrub — 14 vanilla + 39 EE | `34/20` |  |

## Badlands & canyons

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:archeologist_camp_redsand` | BetterArcheology | all badlands / mesa — 3 vanilla + 9 EE | `32/16` |  |
| `betterarcheology:mesa_ruins` | BetterArcheology | all badlands / mesa — 3 vanilla + 9 EE | `32/16` |  |
| `bettermineshafts:mineshaft_mesa` | YUNG's Better Mineshafts | all badlands / mesa — 3 vanilla + 9 EE | `1/0 x0.003` | underground |
| `bettermineshafts:mineshaft_red_desert` | YUNG's Better Mineshafts | all badlands / mesa — 3 vanilla + 9 EE | `1/0 x0.003` | underground |
| `born_in_chaos_v1:grave_2_dling` | Born in Chaos | `badlands`, `eroded_badlands` | `96/9` | **off** |
| `explorify:badlands_pyramid` | Explorify | all badlands / mesa — 3 vanilla + 9 EE | `32/12 x0.75` | § |
| `integrated_villages:clockwork_village` | Integrated Villages | all badlands / mesa — 3 vanilla + 9 EE | `26/19` |  |
| `integrated_villages:kutcha_village` | Integrated Villages | deserts and badlands — 4 vanilla + 16 EE | `26/19` |  |
| `minecraft:mineshaft_mesa` | Vanilla / DnT | all badlands / mesa — 3 vanilla + 9 EE | `1/0 x0.004` | underground |
| `nova_structures:badlands_miner_outpost` | Dungeons & Taverns | all badlands / mesa — 3 vanilla + 9 EE | `90/12` | § |
| `wythers:village_badlands` | Expanded Ecosphere | `badlands` | `16/4` |  |
| `wythers:village_badlands_desert` | Expanded Ecosphere | `EE badlands_desert` | `16/4` |  |
| `wythers:village_wooded_badlands` | Expanded Ecosphere | `wooded_badlands` | `16/4` |  |

## Jungle & tropical

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:fossil_jungle_0` | BetterArcheology | all jungles — 3 vanilla + 18 EE | `24/12` |  |
| `betterarcheology:fossil_jungle_1` | BetterArcheology | all jungles — 3 vanilla + 18 EE | `24/12` |  |
| `betterarcheology:temple_jungle` | BetterArcheology | all jungles — 3 vanilla + 18 EE | `32/16` |  |
| `betterjungletemples:jungle_temple` | YUNG's Better Jungle Temples | all jungles — 3 vanilla + 18 EE | `24/8` | § |
| `bettermineshafts:mineshaft_jungle` | YUNG's Better Mineshafts | all jungles — 3 vanilla + 18 EE | `1/0 x0.003` | underground |
| `born_in_chaos_v1:grave_tem_187` | Born in Chaos | `savanna`, `sparse_jungle` | `115/6` | **off** |
| `companions:companions_monkey_temple` | Companions | jungles and EE rainforest — 3 vanilla + 10 EE | `50/20` | § |
| `dungeonnowloading:temple_of_duality` | Dungeon Now Loading | all jungles — 3 vanilla + 18 EE | `64/16` | underground, § |
| `explorify:ruins` | Explorify | `bamboo_jungle`, `jungle`, `EE sparse_bamboo_jungle`, `EE tropical_rainforest` | `48/24 x0.8` |  |
| `explorify:supply_cache/jungle` | Explorify | all jungles — 3 vanilla + 18 EE | `32/20 x0.75` | § |
| `feur_extension_jungle:ancient_statue` | Feur Jungle | jungle/tropical + badlands/canyons — 1 vanilla + 10 EE | `44/15` | § |
| `feur_extension_jungle:big_melon` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `30/11` | § |
| `feur_extension_jungle:fountain` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `69/15` | § |
| `feur_extension_jungle:lost_explorer` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `46/13` | § |
| `feur_extension_jungle:old_house` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `53/9` | § |
| `feur_extension_jungle:protector` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `30/29` | § |
| `feur_extension_jungle:pyramid` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `34/29` | § |
| `feur_extension_jungle:tree_ancestral` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `27/12` | § |
| `feur_extension_jungle:tree_ancestral_house` | Feur Jungle | all jungles — 3 vanilla + 18 EE | `73/24` |  |
| `feur_extension_jungle:tree_double` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `18/9` | § |
| `feur_extension_jungle:tree_giant` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `60/16` | § |
| `feur_extension_jungle:tree_old` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `55/12` | § |
| `feur_extension_jungle:tree_spline` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `28/21` | § |
| `feur_extension_jungle:tree_stump` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `39/7` | § |
| `feur_extension_jungle:village` | Feur Jungle | jungle and EE tropical forest — 2 vanilla + 10 EE | `90/4` | § |
| `minecraft:jungle_pyramid` | Vanilla / DnT | `bamboo_jungle`, `jungle`, `EE sparse_bamboo_jungle`, `EE tropical_rainforest` | `32/8` |  |
| `minecraft:ruined_portal_jungle` | Vanilla / DnT | all jungles — 3 vanilla + 18 EE | `40/15` |  |
| `nova_structures:firewatch_tower_jungle` | Dungeons & Taverns | all jungles — 3 vanilla + 18 EE | `50/15` |  |
| `nova_structures:jungle_ruins` | Dungeons & Taverns | all jungles — 3 vanilla + 18 EE | `80/12` |  |
| `nova_structures:tavern_jungle` | Dungeons & Taverns | all jungles — 3 vanilla + 18 EE | `44/15` |  |
| `nova_structures:village_jungle` | Dungeons & Taverns | `sparse_jungle` | `34/8` |  |
| `nova_structures:well_jungle` | Dungeons & Taverns | all jungles — 3 vanilla + 18 EE | `40/15` |  |
| `wythers:banyan_sparse_jungle` | Expanded Ecosphere | `sparse_jungle`, `EE bamboo_swamp` | `8/4` | raw gen |
| `wythers:baobab_dry_tropical_forest` | Expanded Ecosphere | `EE dry_tropical_forest` | `6/3` |  |
| `wythers:baobab_tropical_forest` | Expanded Ecosphere | `EE tropical_forest` | `6/3` |  |
| `wythers:el_dorado` | Expanded Ecosphere | `EE tropical_rainforest` | `24/8` | underground |

## Wetlands & swamps

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterwitchhuts:witch_circle` | YUNG's Better Witch Huts | all swamps and wetlands — 2 vanilla + 19 EE | `40/10` |  |
| `betterwitchhuts:witch_hut` | YUNG's Better Witch Huts | all swamps and wetlands — 2 vanilla + 19 EE | `30/8` |  |
| `born_in_chaos_v1:grave_nino_4416` | Born in Chaos | `swamp` | `96/9` | **off** |
| `born_in_chaos_v1:grave_the_gentleman_frog` | Born in Chaos | `mangrove_swamp` | `60/6` | **off** |
| `companions:companions_babayaga` | Companions | all swamps and wetlands — 2 vanilla + 19 EE | `40/10` | § |
| `explorify:mangrove_hut` | Explorify | `mangrove_swamp` | `28/12` |  |
| `explorify:supply_cache/mangrove` | Explorify | `mangrove_swamp` | `32/20 x0.75` |  |
| `integrated_villages:sunken_village` | Integrated Villages | all swamps and wetlands — 2 vanilla + 19 EE | `26/19` |  |
| `irons_spellbooks:mangrove_hut` | Iron's Spells | all swamps and wetlands — 2 vanilla + 19 EE | `25/18` |  |
| `minecraft:ruined_portal_swamp` | Vanilla / DnT | all swamps and wetlands — 2 vanilla + 28 EE | `40/15` | § |
| `minecraft:swamp_hut` | Vanilla / DnT | all swamps and wetlands — 2 vanilla + 28 EE | `32/8` | § |
| `nova_structures:firewatch_tower_mangrove` | Dungeons & Taverns | `mangrove_swamp` | `50/15` |  |
| `nova_structures:firewatch_tower_swamp` | Dungeons & Taverns | `swamp` | `50/15` |  |
| `nova_structures:mangrove_witch_hut` | Dungeons & Taverns | `mangrove_swamp` | `32/8` |  |
| `nova_structures:tavern_mangrove` | Dungeons & Taverns | `mangrove_swamp`, `EE autumnal_swamp`, `EE bamboo_swamp`, `EE bayou`, `EE berry_bog`, `EE coastal_mangroves`, `EE dripleaf_swamp`, `EE tangled_forest` | `44/15` |  |
| `nova_structures:tavern_swamp` | Dungeons & Taverns | all swamps and wetlands — 2 vanilla + 19 EE | `44/15` |  |
| `nova_structures:village_swamp` | Dungeons & Taverns | all swamps and wetlands — 2 vanilla + 19 EE | `34/8` |  |
| `nova_structures:witch_villa` | Dungeons & Taverns | all swamps and wetlands — 2 vanilla + 19 EE | `80/20` |  |
| `ribbits:ribbit_village` | Ribbits | all swamps and wetlands — 2 vanilla + 19 EE | `25/8` |  |

## Rivers

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `realmrpg_quests:anglers` | Realm RPG Quests | rivers — 2 vanilla + 11 EE | `20/19` |  |

## Ocean & coast

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `betterarcheology:underwater_0` | BetterArcheology | all oceans — 9 vanilla + 2 EE | `16/12` |  |
| `betterarcheology:underwater_1` | BetterArcheology | all oceans — 9 vanilla + 2 EE | `16/12` |  |
| `betterarcheology:underwater_2` | BetterArcheology | all oceans — 9 vanilla + 2 EE | `16/12` |  |
| `betterarcheology:underwater_3` | BetterArcheology | all oceans — 9 vanilla + 2 EE | `16/12` |  |
| `cataclysm:acropolis` | L_Ender's Cataclysm | `warm_ocean` | `80/50` |  |
| `cataclysm:sunken_city` | L_Ender's Cataclysm | `deep_cold_ocean`, `deep_frozen_ocean`, `deep_lukewarm_ocean`, `deep_ocean` | `100/70` |  |
| `integrated_villages:pirate_village` | Integrated Villages | all oceans — 9 vanilla + 2 EE | `26/19` |  |
| `minecraft:buried_treasure` | Vanilla / DnT | ocean/coast — 2 vanilla + 11 EE | `1/0 x0.01` | underground |
| `minecraft:monument` | Vanilla / DnT | `deep_cold_ocean`, `deep_frozen_ocean`, `deep_lukewarm_ocean`, `deep_ocean`, `EE deep_icy_ocean` | `32/5` |  |
| `minecraft:ocean_ruin_cold` | Vanilla / DnT | `cold_ocean`, `deep_cold_ocean`, `deep_frozen_ocean`, `deep_ocean`, `frozen_ocean`, `ocean` | `20/8` |  |
| `minecraft:ocean_ruin_warm` | Vanilla / DnT | `deep_lukewarm_ocean`, `lukewarm_ocean`, `warm_ocean` | `20/8` |  |
| `minecraft:ruined_portal_ocean` | Vanilla / DnT | all oceans — 9 vanilla + 2 EE | `40/15` |  |
| `minecraft:shipwreck` | Vanilla / DnT | all oceans — 9 vanilla + 2 EE | `24/4` | § |
| `minecraft:shipwreck_beached` | Vanilla / DnT | ocean/coast — 2 vanilla + 7 EE | `24/4` |  |
| `nova_structures:conduit_ruin` | Dungeons & Taverns | all oceans — 9 vanilla + 2 EE | `140/90` |  |

## Volcanic

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `born_in_chaos_v1:firewell` | Born in Chaos | `EE icy_volcano`, `EE tropical_volcano`, `EE volcanic_crater`, `EE volcano` | `65/9` | § |
| `wythers:elephant_graveyard_fossil` | Expanded Ecosphere | `EE savanna_basaltic_incursions` | `3/1` | underground |

## Not generating

| Structure | Mod | Biomes | Spacing | Notes |
|---|---|---|---|---|
| `born_in_chaos_v1:grave_d_4rk_devil_x` | Born in Chaos | **none** — keyed to `cherry_grove`, which EE never places | `60/6` | **off** |
| `born_in_chaos_v1:grave_fubuki_banzai` | Born in Chaos | **none** — keyed to `cherry_grove`, which EE never places | `84/10` | **off** |
| `nova_structures:firewatch_tower_cherry` | Dungeons & Taverns | **none** — keyed to `cherry_grove`, which EE never places | `50/15` | **never spawns** |
| `nova_structures:tavern_cherry` | Dungeons & Taverns | **none** — keyed to `cherry_grove`, which EE never places | `44/15` | **never spawns** |
| `nova_structures:well_birch` | Dungeons & Taverns | **none** — its biome tag resolves to nothing on this server | `40/15` | §, **never spawns** |
| `supplementaries:way_sign` | Supplementaries | **none** — its biome tag resolves to nothing on this server | `19/10` | **off**, underground |

---

# The Nether

57 structures.

One row per structure, one column per biome. **+** = generates there.

| Structure | Mod | Wastes | Crimson | Warped | Soul | Basalt | Ice | Mire | Squash | Spacing | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `betterdungeons:small_nether_dungeon` | YUNG's Better Dungeons | + | + | + | + | + | + |  |  | `16/10` | underground |
| `betterfortresses:fortress` | YUNG's Better Nether Fortresses | + | + | + | + | + | + |  |  | `30/20` |  |
| `bosses_of_mass_destruction:gauntlet_arena` | Bosses of Mass Destruction | + | + | + | + | + | + |  |  | `32/16` | underground |
| `cataclysm:burning_arena` | L_Ender's Cataclysm | + |  |  |  |  |  |  |  | `80/50` |  |
| `cataclysm:soul_black_smith` | L_Ender's Cataclysm | + | + | + | + |  |  |  |  | `60/50` |  |
| `companions:companions_tent` | Companions | + |  |  |  |  |  |  |  | `30/5` | underground |
| `explorify:black_spiral` | Explorify | + | + | + | + |  | + | + | + | `40/18` |  |
| `formationsnether:basalt_hut` | Formations Nether |  |  |  |  | + |  |  |  | `5/2` | underground |
| `formationsnether:blackstone_castle` | Formations Nether | + | + | + | + | + | + |  |  | `15/10` |  |
| `formationsnether:blackstone_remnant` | Formations Nether | + | + | + | + | + | + |  |  | `5/2` |  |
| `formationsnether:campsite` | Formations Nether |  | + | + |  |  |  |  |  | `5/2` |  |
| `formationsnether:checkerboard_temple` | Formations Nether | + |  |  | + | + |  |  |  | `15/10` |  |
| `formationsnether:flaming_pillar` | Formations Nether | + |  |  |  |  |  |  |  | `5/2` |  |
| `formationsnether:large_house` | Formations Nether |  | + | + |  |  |  |  |  | `5/2` |  |
| `formationsnether:large_pedestal` | Formations Nether | + |  |  | + | + |  |  |  | `5/2` |  |
| `formationsnether:large_tree` | Formations Nether |  | + | + |  |  |  |  |  | `5/2` |  |
| `formationsnether:nether_wart_field` | Formations Nether | + |  |  | + |  |  |  |  | `5/2` |  |
| `formationsnether:obsidian_pillar` | Formations Nether | + | + | + | + |  |  |  |  | `5/2` |  |
| `formationsnether:ore_shard` | Formations Nether | + | + | + | + | + | + |  |  | `4/1` | underground |
| `formationsnether:parkour_challenge` | Formations Nether | + |  |  |  |  |  |  |  | `5/2` |  |
| `formationsnether:quartz_spikes` | Formations Nether | + |  |  | + |  |  |  |  | `5/2` |  |
| `formationsnether:respawn_anchor_altar` | Formations Nether | + | + | + | + | + | + |  |  | `5/2` |  |
| `formationsnether:rib_cage` | Formations Nether | + | + | + | + |  |  |  |  | `5/2` |  |
| `formationsnether:small_fire_arena` | Formations Nether | + |  |  | + | + |  |  |  | `5/2` |  |
| `formationsnether:small_lava_arena` | Formations Nether | + | + | + | + | + | + |  |  | `5/2` |  |
| `formationsnether:small_lava_shrine` | Formations Nether | + |  |  |  |  |  |  |  | `5/2` |  |
| `formationsnether:small_pedestal` | Formations Nether |  | + | + |  |  |  |  |  | `5/2` |  |
| `formationsnether:sphere_cage` | Formations Nether | + | + | + | + | + | + |  |  | `5/2` |  |
| `friendsandfoes:citadel` | Friends & Foes | + | + | + | + | + | + |  |  | `16/8` | underground |
| `irons_spellbooks:ancient_battleground` | Iron's Spells | + | + | + | + |  | + | + | + | `35/12` | underground |
| `irons_spellbooks:citadel` | Iron's Spells | + | + | + | + |  | + | + | + | `100/80` | late |
| `minecraft:bastion_remnant` | Vanilla / DnT | + | + | + | + |  | + | + | + | `27/4` |  |
| `minecraft:fortress` | Vanilla / DnT | + | + | + | + | + | + |  |  | `27/4` | underground |
| `minecraft:nether_fossil` | Vanilla / DnT |  |  |  | + |  | + |  |  | `2/1` | underground |
| `minecraft:ruined_portal_nether` | Vanilla / DnT | + | + | + | + | + | + |  |  | `40/15` |  |
| `netherdepthsupgrade:nether_fortress_piece` | Nether Depths Upgrade | + | + | + | + | + | + |  |  | `25/10` |  |
| `netherexp:chapel` | Jaden's Nether Expansion | + | + | + | + |  | + |  |  | `30/10` | underground |
| `netherexp:devils_bluff` | Jaden's Nether Expansion | + |  |  |  |  |  |  |  | `30/8` | underground |
| `netherexp:mega_fossil` | Jaden's Nether Expansion |  |  |  | + |  | + |  |  | `3/1` | underground |
| `netherexp:mega_fossil_campsite` | Jaden's Nether Expansion |  |  |  | + |  | + |  |  | `3/1` | underground |
| `netherexp:sanctum` | Jaden's Nether Expansion |  |  |  | + |  | + |  |  | — | raw gen |
| `nova_structures:donjon` | Dungeons & Taverns |  | + |  |  |  |  |  |  | `85/40` | underground |
| `nova_structures:hamlet` | Dungeons & Taverns | + | + | + | + |  | + | + | + | `85/40` |  |
| `nova_structures:nether_keep` | Dungeons & Taverns | + | + | + | + | + | + |  |  | `85/40` | underground |
| `nova_structures:nether_port` | Dungeons & Taverns | + | + | + | + | + | + |  |  | `85/40` | underground |
| `nova_structures:nether_skeleton_tower_basalt` | Dungeons & Taverns | + |  |  |  |  |  |  |  | `54/20` |  |
| `nova_structures:nether_skeleton_tower_crimson` | Dungeons & Taverns |  | + |  |  |  |  |  |  | `54/20` |  |
| `nova_structures:nether_skeleton_tower_soul` | Dungeons & Taverns |  |  |  | + |  | + |  | + | `54/20` | § |
| `nova_structures:nether_skeleton_tower_warped` | Dungeons & Taverns |  |  | + |  |  |  |  |  | `54/20` |  |
| `nova_structures:piglin_camp` | Dungeons & Taverns | + | + | + | + |  | + | + | + | `20/10` |  |
| `nova_structures:piglin_outstation` | Dungeons & Taverns | + | + | + | + |  | + | + | + | `85/40` |  |
| `nova_structures:skeleton_camp_crimson` | Dungeons & Taverns |  | + |  |  |  |  |  |  | `20/10` |  |
| `nova_structures:skeleton_camp_soul` | Dungeons & Taverns |  |  |  | + |  |  |  |  | `20/10` |  |
| `nova_structures:skeleton_camp_warped` | Dungeons & Taverns |  |  | + |  |  |  |  |  | `20/10` |  |
| `nova_structures:skeleton_camp_waste` | Dungeons & Taverns | + |  |  |  |  |  |  |  | `20/10` |  |
| `realmrpg_quests:headless_skeletons` | Realm RPG Quests | + | + | + | + | + | + |  |  | `18/14` | underground |
| `realmrpg_quests:piglin_gamblers` | Realm RPG Quests | + | + | + | + | + | + |  |  | `15/11` | underground |

`Ice` = `netherexp:black_ice_glaciers`, `Mire` = `netherexp:exhaust_mire`, `Squash` = `netherexp:sorrowsquash_pastures` — the three Jaden's Nether Expansion sub-biomes. Only the structures using `#minecraft:has_structure/bastion_remnant` reach Mire and Squash; those two are injected by mixin and are not in `#minecraft:is_nether`, so nothing that relies on that tag sees them.

---

# The End

6 structures.

One row per structure, one column per biome. **+** = generates there.

| Structure | Mod | Central island | Highlands | Small islands | Crystal peaks | Shadowlands | Void barrens | Spacing | Notes |
|---|---|---|---|---|---|---|---|---|---|
| `bosses_of_mass_destruction:obsidilith_arena` | Bosses of Mass Destruction |  | + | + |  |  |  | `64/32` |  |
| `cataclysm:ruined_citadel` | L_Ender's Cataclysm |  | + |  |  |  |  | `50/25` |  |
| `endersdelight:end_house` | Ender's Delight |  | + |  |  |  |  | `25/15` |  |
| `explorify:end_shipwreck` | Explorify |  | + |  | + | + | + | `24/6 x0.5` |  |
| `minecraft:end_city` | Vanilla / DnT |  | + |  | + | + | + | `26/18` |  |
| `nullscape:dragon_skeleton` | Nullscape |  |  |  |  | + |  | `22/12` |  |

These are the only six End biomes that Nullscape places. `end_midlands` and `end_barrens` are registered but never placed, so any structure that lists only those two cannot generate — that is why `cataclysm:ruined_citadel` and `endersdelight:end_house` are down to Highlands alone. Nothing generates on the central island, which is vanilla behaviour.
