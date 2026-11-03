# Loot Tables

Every chest loot table a structure in this pack can point at, grouped by the family it should inherit from. Items to place: [`accessories.md`](accessories.md). Where the structures generate: [`structures.md`](structures.md).

**553 structure chest tables from 30 mods**, plus 42 vanilla tables and 45 injector tables.

## Authoring model

- Loot Integrations is removed. Loot is authored in one datapack, `datapacks/Loot_Tables/`.
- An override goes at the mod's own path: `data/<ns>/loot_tables/chests/<same path>.json`. Datapacks load above mod jars, so ours wins.
- Shared ancestors go at `data/magicserver/loot_tables/family/<name>.json` and `.../tier/<name>.json`.
- Inheritance is the `minecraft:loot_table` entry type: a pool entry naming another table rolls that table and merges its items. Nests to any depth.
- Granularity is fixed by the ids the structure NBT already references. One id per structure means one loot design for every chest in it.
- `minecraft:location_check` works in chest context (`ORIGIN` is supplied), so one shared table can still branch on biome, dimension or Y.

## Families

| Family | Covers | Tables |
|---|---|---:|
| `VILLAGE` | Villages, hamlets, villager buildings, homesteads | 175 |
| `ANTHROPOGENIC` | Dungeons, strongholds, mineshafts, crypts, catacombs | 130 |
| `PILLAGE` | Illager, pillager and witch structures | 56 |
| `ARCANE` | Iron's Spells and Geomancy structures | 47 |
| `NETHER` | Fortresses, Nether props and arenas | 35 |
| `QUEST` | Quest and reward tables, not placed chests | 26 |
| `DESERT` | Deserts, badlands, pyramids | 20 |
| `AQUATIC` | Oceans, shipwrecks, conduits, fishing | 9 |
| `SHARED` | Sub-tables referenced by other tables | 9 |
| `COLD` | Snowy and frozen structures | 8 |
| `UNDEAD` | Born in Chaos structures | 7 |
| `JUNGLE` | Jungle temples and groves | 8 |
| `BASTION` | Piglin structures | 5 |
| `SCULK` | Ancient cities | 5 |
| `END` | End structures | 1 |

`ARCANE` and `UNDEAD` are additions. The other 11 are the collections Relics and More Relics already match by regex, so relics self-distribute into them without any work — see [`accessories.md`](accessories.md).

## VILLAGE

| Id prefix | Mod | Tables |
|---|---|---:|
| `integrated_villages:chests/swamp_village` | Integrated Villages | 15 |
| `integrated_villages:chests/airship_village` | Integrated Villages | 14 |
| `integrated_villages:chests/marketstead_village` | Integrated Villages | 14 |
| `integrated_villages:chests/mossy_mounds` | Integrated Villages | 14 |
| `integrated_villages:chests/cabin_village` | Integrated Villages | 13 |
| `integrated_villages:chests/clockwork_village` | Integrated Villages | 13 |
| `integrated_villages:chests/kutcha_village` | Integrated Villages | 13 |
| `integrated_villages:chests/pirate_village` | Integrated Villages | 13 |
| `integrated_villages:chests/minka_village` | Integrated Villages | 11 |
| `integrated_villages:chests/oasis_village` | Integrated Villages | 11 |
| `integrated_villages:chests/mediterranean_village` | Integrated Villages | 10 |
| `integrated_villages:chests/tavern_village` | Integrated Villages | 10 |
| `wythers:chests/village` | Expanded Ecosphere | 9 |
| `nova_structures:chests/hamlet` | Dungeons & Taverns | 2 |
| `minecraft:chests/village` | D&T override | 2 |
| `nova_structures:chests/firewatch_tower` | Dungeons & Taverns | 1 |
| `explorify:chests/supply_cache` | Explorify | 1 |
| `ribbits:chests/gardener` | Ribbits | 1 |
| `ribbits:chests/merchant` | Ribbits | 1 |
| `ribbits:chests/nitwit` | Ribbits | 1 |
| `ribbits:chests/sorcerer` | Ribbits | 1 |
| `realmrpg_quests:chests/horse_chest` | Realm RPG Quests | 1 |
| `realmrpg_quests:chests/old_hut_coal_chest` | Realm RPG Quests | 1 |
| `realmrpg_quests:chests/old_hut_vine_barrel` | Realm RPG Quests | 1 |
| `realmrpg_quests:chests/simple_barrel` | Realm RPG Quests | 1 |
| `realmrpg_quests:chests/vine_barrel` | Realm RPG Quests | 1 |

## ANTHROPOGENIC

| Id prefix | Mod | Tables |
|---|---|---:|
| `dungeonnowloading:chests/temple_of_duality` | Dungeon Now Loading | 55 |
| `dungeonnowloading:chests/labyrinth` | Dungeon Now Loading | 19 |
| `artifacts:chests/campsite_barrel` | Artifacts | 11 |
| `minecraft:chests/stronghold` | D&T override | 7 |
| `betterarcheology:archeology/chest_catacombs_*` | BetterArcheology | 5 |
| `betterdungeons:zombie_dungeon/chests` | YUNG's Better Dungeons | 3 |
| `dungeonnowloading:vanilla/blocks` | Dungeon Now Loading | 3 |
| `betterdungeons:skeleton_dungeon/chests` | YUNG's Better Dungeons | 2 |
| `dungeonnowloading:vanilla/chests` | Dungeon Now Loading | 2 |
| `nova_structures:chests/creeping_crypt` | Dungeons & Taverns | 2 |
| `nova_structures:chests/dungeon_1` … `dungeon_7` | Dungeons & Taverns | 7 |
| `betterdungeons:small_dungeon/chests` | YUNG's Better Dungeons | 1 |
| `betterdungeons:small_nether_dungeon/chests` | YUNG's Better Dungeons | 1 |
| `betterdungeons:spider_dungeon/chests/egg_room` | YUNG's Better Dungeons | 1 |
| `cataclysm:chests/abandoned` | L_Ender's Cataclysm | 1 |
| `cataclysm:chests/abandoned_treasure` | L_Ender's Cataclysm | 1 |
| `cataclysm:chests/acropolis_treasure` | L_Ender's Cataclysm | 1 |
| `cataclysm:chests/amethyst_nest_chest` | L_Ender's Cataclysm | 1 |
| `artifacts:chests/campsite_chest` | Artifacts | 1 |
| `explorify:chests/mausoleum_pot` | Explorify | 1 |
| `friendsandfoes:barrels/citadel` | Friends & Foes | 1 |
| `mowziesmobs:chests/monastery_chest` | Mowzie's Mobs | 1 |
| `nova_structures:chests/ruin_loot_master` | Dungeons & Taverns | 1 |
| `nova_structures:chests/undead_crypts_grave` | Dungeons & Taverns | 1 |
| `realmrpg_quests:chests/mine_barrel` | Realm RPG Quests | 1 |

## PILLAGE

| Id prefix | Mod | Tables |
|---|---|---:|
| `minecraft:chests/illager_mansion` | D&T override | 13 |
| `nova_structures:chests/witch_villa` | Dungeons & Taverns | 11 |
| `takesapillage:chests/bastille` | It Takes a Pillage | 5 |
| `nova_structures:chests/illager_hideout_*` | Dungeons & Taverns | 13 |
| `nova_structures:chests/pillager_outpost_kitchen` | Dungeons & Taverns | 1 |
| `nova_structures:chests/pillager_outpost_treasure` | Dungeons & Taverns | 1 |
| `nova_structures:chests/illager_camp` | Dungeons & Taverns | 1 |
| `nova_structures:chests/mangrove_witchhud` | Dungeons & Taverns | 1 |
| `hunters_return:chests/illager_woodhut` | Hunter's Return | 1 |
| `hunters_return:chests/illager_woodhut_arrow` | Hunter's Return | 1 |
| `hunters_return:chests/illager_woodhut_barrel` | Hunter's Return | 1 |
| `friendsandfoes:chests/illusioner_shack` | Friends & Foes | 1 |
| `friendsandfoes:barrels/illusioner_shack_attic` | Friends & Foes | 1 |
| `friendsandfoes:barrels/illusioner_shack_basement` | Friends & Foes | 1 |
| `explorify:chests/dark_forest_settlement` | Explorify | 1 |
| `conjurer_illager:chests/dressing_room` | Conjurer | 1 |
| `betterwitchhuts:chests/hut_0` | YUNG's Better Witch Huts | 1 |
| `wythers:chests/outpost` | Expanded Ecosphere | 1 |

## ARCANE

| Id prefix | Mod | Tables |
|---|---|---:|
| `irons_spellbooks:chests/catacombs` | Iron's Spells | 7 |
| `irons_spellbooks:chests/pyromancer_tower` | Iron's Spells | 7 |
| `irons_spellbooks:chests/citadel` | Iron's Spells | 6 |
| `irons_spellbooks:chests/ice_spider_den` | Iron's Spells | 6 |
| `irons_spellbooks:chests/trial_chambers` | Iron's Spells | 6 |
| `irons_spellbooks:chests/mangrove_hut` | Iron's Spells | 3 |
| `irons_spellbooks:chests/battleground` | Iron's Spells | 2 |
| `irons_spellbooks:chests/evoker_fort` | Iron's Spells | 2 |
| `irons_spellbooks:chests/impaled_icebreaker` | Iron's Spells | 2 |
| `irons_spellbooks:chests/mountain_tower` | Iron's Spells | 2 |
| `gtbcs_geomancy_plus:chests/geomancer_tower` | Geomancy Plus | 2 |
| `irons_spellbooks:chests/priest_house` | Iron's Spells | 1 |
| `irons_spellbooks:chests/sewer_loot` | Iron's Spells | 1 |

## NETHER

| Id prefix | Mod | Tables |
|---|---|---:|
| `betterfortresses:chests/beacon`, `extra`, `hall`, `keep`, `obsidian`, `puzzle`, `quarters`, `storage`, `worship` | YUNG's Better Nether Fortresses | 9 |
| `formationsnether:basalt_hut`, `blackstone_castle`, `blackstone_remnant`, `campsite`, `large_tree`, `parkour_challenge`, `rib_cage`, `small_fire_arena`, `small_lava_arena`, `small_lava_shrine` | Formations Nether | 10 |
| `nova_structures:chests/nether_skeleton_tower` | Dungeons & Taverns | 3 |
| `nova_structures:chests/nether_port` | Dungeons & Taverns | 2 |
| `minecraft:chests/nether_fortress` | D&T override | 2 |
| `formationsnether:large_house/barrel` | Formations Nether | 1 |
| `formationsnether:large_house/chest` | Formations Nether | 1 |
| `formationsnether:sphere_cage/wither` | Formations Nether | 1 |
| `netherexp:chests/chapel` | Jaden's Nether Expansion | 1 |
| `netherexp:chests/sanctum_food` | Jaden's Nether Expansion | 1 |
| `netherexp:chests/sanctum_supply` | Jaden's Nether Expansion | 1 |
| `netherexp:brazier_chest/exposed` | Jaden's Nether Expansion | 1 |
| `netherexp:brazier_chest/hidden` | Jaden's Nether Expansion | 1 |
| `bosses_of_mass_destruction:chests/gauntlet` | Bosses of Mass Destruction | 1 |

## DESERT

| Id prefix | Mod | Tables |
|---|---|---:|
| `betterdeserttemples:chests/food_storage`, `lab`, `library`, `pharaoh_hidden`, `pot`, `statue`, `storage`, `tomb`, `tomb_pharaoh`, `wardrobe` | YUNG's Better Desert Temples | 10 |
| `nova_structures:chests/desert_ruins` | Dungeons & Taverns | 4 |
| `nova_structures:chests/badland_miner_outpost` | Dungeons & Taverns | 1 |
| `nova_structures:chests/badland_miner_outpost_forge` | Dungeons & Taverns | 1 |
| `nova_structures:chests/badland_miner_outpost_towers` | Dungeons & Taverns | 1 |
| `cataclysm:chests/desert_treasure` | L_Ender's Cataclysm | 1 |
| `betterarcheology:archeology/chest_buried_ruins_sand` | BetterArcheology | 1 |
| `betterarcheology:archeology/chest_mesa_ruins` | BetterArcheology | 1 |

## JUNGLE

| Id prefix | Mod | Tables |
|---|---|---:|
| `betterarcheology:archeology/chest_jungle_temple_*` | BetterArcheology | 4 |
| `betterjungletemples:chests/campsite` | YUNG's Better Jungle Temples | 1 |
| `betterjungletemples:chests/treasure` | YUNG's Better Jungle Temples | 1 |
| `nova_structures:chests/jungle_ruins` | Dungeons & Taverns | 1 |
| `mowziesmobs:chests/umvuthana_grove_chest` | Mowzie's Mobs | 1 |

## AQUATIC

| Id prefix | Mod | Tables |
|---|---|---:|
| `nova_structures:chests/conduit_ruin` | Dungeons & Taverns | 3 |
| `aquamirae:chests/maze_camp_chest` | Aquamirae | 1 |
| `aquamirae:chests/maze_common_chest` | Aquamirae | 1 |
| `aquamirae:chests/ship_1` | Aquamirae | 1 |
| `aquamirae:chests/ship_2` | Aquamirae | 1 |
| `ribbits:chests/fisherman_main` | Ribbits | 1 |
| `ribbits:chests/fisherman_storage` | Ribbits | 1 |
| `realmrpg_quests:chests/fish_barrel` | Realm RPG Quests | 1 |
| `betterarcheology:archeology/chest_underwater` | BetterArcheology | 1 |

## COLD

| Id prefix | Mod | Tables |
|---|---|---:|
| `nova_structures:chests/stray_fort_archer` | Dungeons & Taverns | 1 |
| `nova_structures:chests/stray_fort_loot_generic` | Dungeons & Taverns | 1 |
| `nova_structures:chests/stray_fort_tresure` | Dungeons & Taverns | 1 |
| `aquamirae:chests/frozen_chest` | Aquamirae | 1 |
| `cataclysm:chests/frosted_prison_treasure` | L_Ender's Cataclysm | 1 |
| `friendsandfoes:chests/iceologer_cabin` | Friends & Foes | 1 |
| `friendsandfoes:barrels/iceologer_cabin` | Friends & Foes | 1 |
| `bosses_of_mass_destruction:chests/lich_tower` | Bosses of Mass Destruction | 1 |

## BASTION

| Id prefix | Mod | Tables |
|---|---|---:|
| `nova_structures:chests/piglin_outstation` | Dungeons & Taverns | 3 |
| `formationsnether:sphere_cage/piglin` | Formations Nether | 1 |
| `realmrpg_quests:chests/piglin_gambler_chest` | Realm RPG Quests | 1 |

## SCULK

| Id prefix | Mod | Tables |
|---|---|---:|
| `minecraft:chests/ancient_city` | vanilla | 1 |
| `minecraft:chests/ancient_city_center` | D&T override | 1 |
| `minecraft:chests/ancient_city_ice_box` | D&T override | 1 |
| `majruszsenchantments:chests/ancient_city_sixth_sense` | Majrusz's Enchantments | 1 |
| `betterarcheology:archeology/chest_sculk_sword` | BetterArcheology | 1 |

## END

| Id prefix | Mod | Tables |
|---|---|---:|
| `bosses_of_mass_destruction:chests/obsidilith` | Bosses of Mass Destruction | 1 |

## UNDEAD

Born in Chaos ships these under the `minecraft` namespace.

| Id | Tables |
|---|---:|
| `minecraft:chests/basic_chest` | 1 |
| `minecraft:chests/chest_level_1` | 1 |
| `minecraft:chests/chest_level_2` | 1 |
| `minecraft:chests/chest_level_3` | 1 |
| `minecraft:chests/farm_drop` | 1 |
| `minecraft:chests/firewell_d` | 1 |
| `minecraft:chests/shater` | 1 |

## Vanilla chest tables (42)

| Family | Tables |
|---|---|
| `VILLAGE` | `village/village_armorer`, `_butcher`, `_cartographer`, `_desert_house`, `_fisher`, `_fletcher`, `_mason`, `_plains_house`, `_savanna_house`, `_shepherd`, `_snowy_house`, `_taiga_house`, `_tannery`, `_temple`, `_toolsmith`, `_weaponsmith` |
| `ANTHROPOGENIC` | `abandoned_mineshaft`, `simple_dungeon`, `stronghold_corridor`, `stronghold_crossing`, `stronghold_library` |
| `PILLAGE` | `pillager_outpost`, `woodland_mansion` |
| `AQUATIC` | `buried_treasure`, `shipwreck_map`, `shipwreck_supply`, `shipwreck_treasure`, `underwater_ruin_big`, `underwater_ruin_small` |
| `NETHER` | `nether_bridge`, `ruined_portal` |
| `BASTION` | `bastion_bridge`, `bastion_hoglin_stable`, `bastion_other`, `bastion_treasure` |
| `DESERT` | `desert_pyramid` |
| `JUNGLE` | `jungle_temple` |
| `COLD` | `igloo_chest` |
| `SCULK` | `ancient_city`, `ancient_city_ice_box` |
| `END` | `end_city_treasure` |
| — | `spawn_bonus_chest` |

## Shared sub-tables

Referenced by other tables, never placed as a chest on their own.

| Id | Mod |
|---|---|
| `irons_spellbooks:chests/bookshelf_loot` | Iron's Spells |
| `irons_spellbooks:chests/magic_bookshelf_loot` | Iron's Spells |
| `irons_spellbooks:chests/component_storage` | Iron's Spells |
| `irons_spellbooks:chests/filler_storage_loot` | Iron's Spells |
| `irons_spellbooks:chests/generic_magic_treasure` | Iron's Spells |
| `irons_spellbooks:chests/wheat` | Iron's Spells |
| `nova_structures:chests/food_supply` | Dungeons & Taverns |
| `nova_structures:chests/mining_supply` | Dungeons & Taverns |
| `nova_structures:chests/water_supply` | Dungeons & Taverns |
| `betterarcheology:archeology/loot_catacombs_equipment` | BetterArcheology |
| `betterarcheology:archeology/loot_catacombs_generic` | BetterArcheology |
| `betterarcheology:archeology/sword_loot` | BetterArcheology |
| `betterarcheology:identifying_loot` | BetterArcheology |

## Quest and reward tables

Handed out on completion, not generated in a chest.

| Id prefix | Mod | Tables |
|---|---|---:|
| `nova_structures:chests/tavern_quest` | Dungeons & Taverns | 13 |
| `realmrpg_quests:rewards/cook` | Realm RPG Quests | 4 |
| `realmrpg_quests:rewards/old` | Realm RPG Quests | 2 |
| `realmrpg_quests:rewards/angler` | Realm RPG Quests | 1 |
| `realmrpg_quests:rewards/angler_generic` | Realm RPG Quests | 1 |
| `realmrpg_quests:rewards/angler_rare` | Realm RPG Quests | 1 |
| `realmrpg_quests:rewards/headless_skeleton` | Realm RPG Quests | 1 |
| `realmrpg_quests:rewards/monster_hunter` | Realm RPG Quests | 1 |
| `realmrpg_quests:rewards/shell_game` | Realm RPG Quests | 1 |
| `friendsandfoes:rewards/rascal_good_reward` | Friends & Foes | 1 |

## Item injectors

These tables hold a mod's own items and are merged into other tables by a global loot modifier. They are contributors, not chests.

| Id prefix | Mod | Tables | Targets |
|---|---|---:|---|
| `farmersdelight:chests/fd_*` | Farmer's Delight | 14 | matching vanilla chest |
| `musketmod:chests/*` | Musket | 9 | matching vanilla chest |
| `magistuarmory:chests/*` | Magistu Armory | 8 | matching vanilla chest |
| `irons_spellbooks:chests/additional_*` | Iron's Spells | 7 | vanilla ancient city, end city, library, nether, treasure, generic, good |
| `artifacts:inject/chests/*` | Artifacts | 19 | one pinned vanilla table each |
| `artifacts:inject/archaeology/*` | Artifacts | 5 | suspicious sand and gravel |
| `uniqueaccessories:loot_modifiers/chest/*` | Unique Accessories | 18 | one pinned vanilla table each |
| `relics:relic_loot` | Relics, More Relics | 1 | every table matching a family regex |
| `supplementaries:inject/*` | Supplementaries | 21 | vanilla chests |

## Needs a call

| Id | Mod | Note |
|---|---|---|
| `betterarcheology:archeology/chest_light_temple_*` | BetterArcheology | 4 tables; Light Temple sits in plains and taiga, has a pillager chest |
| `betterarcheology:archeology/chest_mott` | BetterArcheology | forest and taiga earthwork |
| `betterarcheology:archeology/chest_tumulus` | BetterArcheology | forest, plains and taiga burial mound |
| `betterarcheology:archeology/chest_radiance_totem` | BetterArcheology | no matching structure entry |
| `cluttered:chests/sapling_chest_loot` | Cluttered | single chest, no structure page entry |
| `chefsdelight:chests/cooker` | Chef's Delight | block inventory, not a structure chest |
| `betterarcheology:archeology/chest_loot_prototype` | BetterArcheology | looks unused |
