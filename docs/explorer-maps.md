# Explorer Maps

Maps that point at modded structures, sold by cartographers and found in chests.

Not implemented.

## What it takes

| Piece | Why |
|---|---|
| `datapacks/Explorer_Maps` | Structure tags. A map's destination is a **structure tag**, not a structure id, so every target needs one. |
| `mod_compats/ExplorerMaps` | Map trades search for a structure when offered. Moonlight's datapack trades ([`datapacks/Trades`](../datapacks/Trades/README.md)) sell fixed items; whether one can sell a structure map is not checked. |

Other routes to villager trades:

- KubeJS `2001.6.5` dropped the villager-trade event, there is no such class in the jar.
- CraftTweaker is listed in `modlist-server.yaml` but **is not installed**, no jar in `magic server/mods`.
- Loot Integrations injects loot, not trades. Its `skipMapItems: true` only blocks its own random map injection; maps placed directly in a loot table are untouched.

## Constraints

| Constraint | Effect on design |
|---|---|
| **Overworld only** | A cartographer is an Overworld villager and cannot know what is in the Nether or the End. No Nether or End target, in trades or in loot. |
| **Maps are dimension-locked** | `ExplorationMapFunction` and `TreasureMapForEmeralds` search the level the map is *created* in, so the rule above is also the engine's. |
| **Every map runs a structure search** | Synchronous, on the server thread, at trade-offer generation or chest-loot roll. Rare targets (spacing 100-200) are the expensive ones. Search radius is capped per target. |
| **Failed search is silent** | Trade: the offer does not appear. Loot: the player gets a blank map. |
| **Icons are vanilla only** | `MapDecoration.Type`: `red_x`, `target_x`, `target_point`, `mansion`, `monument`, `red_marker`, `blue_marker`, 16x `banner_<colour>`. Enough to separate tiers, no custom art. |
| **`skip_existing_chunks: false`** | Every target, every tier. Maps point at the nearest structure even if it sits in already-generated land. |

Search radius by spacing: `<=32` to 32 chunks, `33-64` to 50, `65-100` to 75, `>100` to 100.

## Delivery order

| Phase | Deliverable |
|---|---|
| 1 | Structure tags datapack, 69 targets |
| 2 | `ExplorerMaps` mod, cartographer trades |
| 3 | Chest loot, Forge GLM in the same mod |
| 4 | `modpack-wiki.md` entry |

Phase 3 uses a global loot modifier rather than overriding loot tables, so DnT's rewritten `pillager_outpost` and every modded chest keep their own contents.

Loot slots: village and outpost chests, modded dungeon and structure chests, buried treasure and shipwreck. All Overworld.

## Trade tiers

Cartographer, on top of the vanilla Ocean and Woodland maps. Each villager rolls one target per level, so no two cartographers sell the same set.

| Level | Tier | Price | Max uses | Targets |
|---|---|---|---|---|
| Novice | Villages | 8 emeralds + compass | 5 | 21 |
| Apprentice | Ruins and hideouts | 12 emeralds + compass | 3 | 16 |
| Journeyman | Dungeons and lairs | 18 emeralds + compass | 2 | 15 |
| Expert | Keeps and towers | 24 emeralds + compass | 2 | 11 |
| Master | Boss lairs and great dungeons | 32 emeralds + compass | 1 | 6 |

Target list lives in `config/explorer_maps.json` so prices and picks retune without a rebuild.

## Map targets

69 targets, all Overworld. Spacing is `spacing/separation` in chunks from [`structures.md`](structures.md); higher means rarer and a more expensive search.

### Villages, Novice tier

| Target | Structure(s) | Mod | Spacing |
|---|---|---|---|
| Marketstead Village | `integrated_villages:marketstead_village` | Integrated Villages | `26/19` |
| Mossy Mounds | `integrated_villages:mossy_mounds` | Integrated Villages | `26/19` |
| Minka Village | `integrated_villages:quark/minka_village` | Integrated Villages | `26/19` |
| Tavern Village | `integrated_villages:tavern_village` | Integrated Villages | `26/19` |
| Cabin Village | `integrated_villages:cabin_village` | Integrated Villages | `26/19` |
| Mediterranean Village | `integrated_villages:mediterranean_village` | Integrated Villages | `26/19` |
| Oasis Village | `integrated_villages:oasis_village` | Integrated Villages | `26/19` |
| Clockwork Village | `integrated_villages:clockwork_village` | Integrated Villages | `26/19` |
| Kutcha Village | `integrated_villages:kutcha_village` | Integrated Villages | `26/19` |
| Sunken Village | `integrated_villages:sunken_village` | Integrated Villages | `26/19` |
| Pirate Village | `integrated_villages:pirate_village` | Integrated Villages | `26/19` |
| Airship Village | `integrated_villages:airship_village` | Integrated Villages | `115/90` |
| Jungle Village | `nova_structures:village_jungle` | D&T | `34/8` |
| Swamp Village | `nova_structures:village_swamp` | D&T | `34/8` |
| Feur Jungle Village | `feur_extension_jungle:village` | Feur Jungle | `90/4` |
| Ribbit Village | `ribbits:ribbit_village` | Ribbits | `25/8` |
| Plains Village | `minecraft:village_plains` | Vanilla / DnT | `34/8` |
| Taiga Village | `minecraft:village_taiga` | Vanilla / DnT | `34/8` |
| Snowy Village | `minecraft:village_snowy` | Vanilla / DnT | `34/8` |
| Savanna Village | `minecraft:village_savanna` | Vanilla / DnT | `34/8` |
| Desert Village | `minecraft:village_desert` | Vanilla / DnT | `34/8` |

### Ruins and hideouts, Apprentice tier

| Target | Structure(s) | Mod | Spacing |
|---|---|---|---|
| Ruin Town | `nova_structures:ruin_town` | D&T | `60/12` |
| Desert Ruins | `nova_structures:desert_ruins` | D&T | `80/12` |
| Badlands Miner Outpost | `nova_structures:badlands_miner_outpost` | D&T | `90/12` |
| Wild Ruin | `nova_structures:wild_ruin` | D&T | `23/18` |
| Bunker | `nova_structures:bunker` | D&T | `50/10` |
| Mangrove Witch Hut | `nova_structures:mangrove_witch_hut` | D&T | `32/8` |
| Badlands Pyramid | `explorify:badlands_pyramid` | Explorify | `32/12 x0.75` |
| Dark Forest Settlement | `explorify:dark_forest_settlement` | Explorify | `32/16 x0.8` |
| Overgrown Ruins | `explorify:ruins` | Explorify | `48/24 x0.8` |
| Desert Shrine | `explorify:desert_shrine` | Explorify | `24/8` |
| Feur Pyramid | `feur_extension_jungle:pyramid` | Feur Jungle | `34/29` |
| Ancient Statue | `feur_extension_jungle:ancient_statue` | Feur Jungle | `44/15` |
| Light Temple | `betterarcheology:light_temple` | BetterArcheology | `32/16` |
| Jungle Dig Temple | `betterarcheology:temple_jungle` | BetterArcheology | `32/16` |
| Illusioner Shack | `friendsandfoes:illusioner_shack` | Friends & Foes | `40/10` |
| Iceologer Cabin | `friendsandfoes:iceologer_cabin` | Friends & Foes | `40/10` |

### Dungeons and lairs, Journeyman tier

| Target | Structure(s) | Mod | Spacing |
|---|---|---|---|
| Better Desert Temple | `betterdeserttemples:desert_temple` | YUNG's | `30/20` |
| Better Jungle Temple | `betterjungletemples:jungle_temple` | YUNG's | `24/8` |
| Better Witch Hut | `betterwitchhuts:witch_hut` | YUNG's | `30/8` |
| Witch Circle | `betterwitchhuts:witch_circle` | YUNG's | `40/10` |
| Jungle Ruins | `nova_structures:jungle_ruins` | D&T | `80/12` |
| Illager Hideout | `nova_structures:illager_hideout` | D&T | `200/12` |
| Undead Crypt | `nova_structures:undead_crypt` | D&T | `100/20` |
| Creeping Crypt | `nova_structures:creeping_crypt` | D&T | `100/10` |
| Witch Villa | `nova_structures:witch_villa` | D&T | `80/20` |
| Conduit Ruin | `nova_structures:conduit_ruin` | D&T | `140/90` |
| Catacombs | `betterarcheology:catacombs` | BetterArcheology | `32/16` |
| Geomancer Tower | `gtbcs_geomancy_plus:geomancer_tower` | Geomancy Plus | `40/25` |
| Mound of Hounds | `born_in_chaos_v1:mound_of_hounds` | Born in Chaos | `50/3` |
| Pillager Camp | `takesapillage:pillager_camp` | It Takes a Pillage | `32/12` |
| Illusioner Training Grounds | `friendsandfoes:illusioner_training_grounds` | Friends & Foes | `40/10` |

### Keeps and towers, Expert tier

| Target | Structure(s) | Mod | Spacing |
|---|---|---|---|
| Grim Kingdoms castle | all 15 `grim_kingdoms:*`, one tag | Grim Kingdoms | `50/30` |
| Illager Manor | `nova_structures:illager_manor` | D&T | `80/20` |
| Illager Camp | `nova_structures:illager_camp` | D&T | `26/12` |
| Stray Fort | `nova_structures:stray_fort` | D&T | `100/16` |
| Bastille | `takesapillage:bastille` | It Takes a Pillage | `32/12` |
| Pillager Outpost | `minecraft:pillager_outpost` | Vanilla / DnT | `32/8 x0.2` |
| Dark Tower | 3x `born_in_chaos_v1:dark_tower_*` | Born in Chaos | `75-85/10-14` |
| Observation Tower | 2x `born_in_chaos_v1:observation_tower_*` | Born in Chaos | `65-200/12` |
| Companions Factory | `companions:companions_factory` | Companions | `80/10` |
| Teddy Tower | `companions:companions_teddytower` | Companions | `80/30` |
| Baba Yaga's Hut | `companions:companions_babayaga` | Companions | `40/10` |

### Boss lairs and great dungeons, Master tier

| Target | Structure(s) | Mod | Spacing |
|---|---|---|---|
| Ancient City | `minecraft:ancient_city` | Vanilla / DnT | `24/8` |
| Labyrinth | `dungeonnowloading:labyrinth` | Dungeon Now Loading | `80/20` |
| Temple of Duality | `dungeonnowloading:temple_of_duality` | Dungeon Now Loading | `64/16` |
| Monkey Temple | `companions:companions_monkey_temple` | Companions | `50/20` |
| Monastery | `mowziesmobs:monastery` | Mowzie's | `25/8` |
| Conjurer's Theatre | `conjurer_illager:theatre` | Conjurer | `40/? x0.5` |

Ancient City and Temple of Duality generate underground; the map marks the surface above them.

## Open decisions

1. **Airship Village is `115/90`**, seven times rarer than every other village, but priced at the Novice 8 emeralds. Leave it, or move it to Apprentice.
2. **`nova_structures:conduit_ruin` at `140/90`** is the most expensive search in the list, and `nova_structures:illager_hideout` at `200/12` is next. Cap them at loot-only if trade-offer generation stutters.
