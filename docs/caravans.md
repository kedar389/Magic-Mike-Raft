# Pillager Caravans

Design for a custom caravan datapack. Replaces the mod's built-in `classic_caravans` pack (5 placements, 15 caravans, vanilla illagers only).

Implemented in `datapacks/Pillager_Caravans/`, names in `resourcepacks/Caravan_Names/`.

## What the mod does

**Pillager Caravans** (`caravans`, Obscuria, 3.0.0) spawns roaming illager convoys: a leader, a walking escort, leashed pack animals carrying loot. Kill the animals for the cargo.

Config in `config/Obscuria/pillager_caravans-common.toml`, already set: enabled, first spawn after one in-game day, 32 +/- 32 blocks out, block light 4 or below, 3 sections clear of a village. Small/medium/large spawn weights 9/3/1, cooldowns 48k/96k/144k ticks.

## Datapack format

```json
// caravans/placement/desert.json
{ "weight": 1000, "biomes": "#magic_caravans:has_desert_caravan",
  "pools": [ { "weight": 10, "variations": ["magic_caravans:dune_miners_tier_1", "magic_caravans:dune_miners_tier_2", "magic_caravans:dune_miners_tier_3"] } ] }

// caravans/variation/dune_miners_tier_1.json
{ "tier": "small", "leader": { "type": "minecraft:vindicator" },
  "members": [
    { "type": "minecraft:pillager", "count": 2 },
    { "type": "minecraft:llama", "armor": "minecraft:orange_carpet", "cargo": "magic_caravans:cargo/dune_miners/small", "count": 2 } ] }
```

Match a placement by biome, roll a pool by weight, take the variation matching the already-rolled tier. Member fields: `type` (required), `count`, `armor`, `cargo`, `loot_table`. Biome sets in `tags/worldgen/biome/`, names in a resource pack's `caravan.<namespace>.<variation_id>` keys.

| Rule | Effect |
|---|---|
| `cargo` needs `AbstractChestedHorse` | Only llama, trader_llama, donkey, mule carry loot |
| `armor` on a llama must be a carpet | Anything else is ignored. Horses take horse armor |
| Leashing needs a holder | Animals leash to escort members, so escorts >= leash chains |
| Members follow the leader | Killing the leader scatters them, it does not stop the caravan |
| Hostile non-illagers fight the escort | Only passive and illager-allied mobs work |

## Mob palette

Tables below use bare names.

| Bare name | Namespace | Mod |
|---|---|---|
| pillager, vindicator, evoker, illusioner, witch, ravager | `minecraft:` | Vanilla |
| archer, skirmisher, legioner | `takesapillage:` | It Takes a Pillage |
| igniter, absorber, crocofang | `illageandspillage:` | Illage and Spillage |
| zapper, golem_of_last_resort | `raidsenhanced:` | Raids Enhanced |
| enchanter | `enchantwithmob:` | Enchant With Mob |
| hunter | `hunters_return:` | Hunter's Return |
| iceologer | `friendsandfoes:` | Friends & Foes |
| tesla_golem | `companions:illager_golem` | Companions |
| archevoker | `irons_spellbooks:` | Iron's Spells |

Illagers Wear Armor adds no entities. It re-skins the ones above, so caravan illagers are armoured automatically.

Three mobs constrain the design. Class facts verified with `javap` against the deployed jars.

| Mob | Fact | Effect |
|---|---|---|
| archevoker | `AbstractSpellCastingMob` implements `Enemy`, not `Raider`. Targets `Player` only, so illagers ignore it. `HurtByTargetGoal` has no exclusion array. Carries `PatrolNearLocationGoal`. | Friendly fire turns it on its own column, so caravans it leads carry no witch and no igniter. Patrol goal may fight follow-the-leader. |
| tesla_golem | `Raider`. Targets player, `AbstractVillager`, `IronGolem`; `HurtByTargetGoal` ignores `Raider`. Joins raids via `IllagerGolemRaidMixin` on odd waves. No biome spawn, no statue form. | Safe heavy. Weaker than a ravager, travels in pairs. |
| golem_of_last_resort | `FDRaider` -> `Raider`, same targets and exclusion. `REMixinHandler.raidMixin` spawns it at bad omen level 3. | Safe heavy, strong enough alone. |

## Placements

19 placements. Tags by default: Expanded Ecosphere adds its 188 biomes to the vanilla `minecraft:`, `forge:` and `c:` tags with `replace: false`, so one tag covers both.

| Placement | Biome set | Covers |
|---|---|---|
| `desert` | `#wythers:is_desert` | `minecraft:desert` + 13 EE |
| `salt_flats` | `wythers:salt_lakes_white`, `salt_lakes_pink`, `salt_lakes_turquoise`, `danakil_desert`, `mud_pools` | 5 EE |
| `badlands` | `#minecraft:is_badlands`, `#c:mesa` | 3 vanilla + 9 EE |
| `outback` | `wythers:outback`, `outback_desert`, `ayers_rock`, `kwongan_heath`, `eucalyptus_woodland`, `sparse_eucalyptus_woodland`, `eucalyptus_salubris_woodland`, `billabong` | 8 EE |
| `jungle` | `#minecraft:is_jungle` minus bamboo | `jungle`, `sparse_jungle` + 14 EE |
| `bamboo` | `minecraft:bamboo_jungle`, `wythers:sparse_bamboo_jungle`, `bamboo_jungle_canyon`, `bamboo_jungle_highlands`, `bamboo_swamp`, `bamboo_jungle_swamp` | 1 vanilla + 5 EE |
| `plains` | `#forge:is_plains` | 3 vanilla + 10 EE |
| `savanna` | `#minecraft:is_savanna` | 3 vanilla + 4 EE |
| `forest` | `#minecraft:is_forest`, `#c:flower_forests` | temperate forests + EE |
| `autumnal` | `wythers:autumnal_forest`, `autumnal_forest_edge`, `autumnal_birch_forest`, `autumnal_flower_forest`, `autumnal_plains`, `autumnal_swamp`, `autumnal_crags` | 7 EE |
| `taiga` | `#minecraft:is_taiga`, `#forge:is_coniferous` | 4 vanilla + 7 EE |
| `snowy` | `#forge:is_snowy`, `#c:icy` | 3 vanilla + 5 EE, plus EE peaks |
| `swamp` | `#wythers:is_swamp` minus `mangrove_swamp` | `minecraft:swamp` + 21 EE |
| `river` | `#minecraft:is_river` | 2 vanilla + 10 EE |
| `dark_forest` | `#wythers:is_dark_forest`, `#forge:is_spooky` | `dark_forest` + EE dark variants |
| `mountain` | `#forge:is_mountain`, `#forge:is_peak`, `#forge:is_slope` | 7 vanilla + 14 EE |
| `volcanic` | `wythers:volcano`, `volcanic_crater`, `volcanic_chamber`, `tropical_volcano`, `icy_volcano` | 5 EE |
| `coast` | `#minecraft:is_beach`, `minecraft:stony_shore` | 3 vanilla + 9 EE |
| `mystical` | `minecraft:mushroom_fields`, `#forge:is_mushroom`, `wythers:forbidden_forest`, `phantasmal_forest`, `deep_dark_forest` | 1 vanilla + 8 EE |

Explicit lists where a tag will not do: `salt_flats` (the lakes sit inside `#wythers:is_desert`), `bamboo` (Alex's Mobs excludes bamboo jungle from its jungle spawns, so a bamboo caravan cannot use a jungle beast), `autumnal` and `outback` (EE theme clusters spanning several terrain types), `volcanic` (no tag). Caves and islands carry no caravans: neither has room to walk a column.

`deep_frozen_ocean` is Aquamirae's Sea of Sorrow and is not a caravan biome. Aquamirae runs its own patrols there.

## Composition

| Slot | small | medium | large |
|---|---|---|---|
| Leader | 1 | 1 | 1 |
| Escort | see column size | | |
| Enchanter | 0 | 0 | 1 |
| Heavy | 0 | 0 | 1 to 3 |
| Pack llamas | 2 | 4 | 6 |
| Beast | 0 | 0 to 6 | 0 to 9 |

`enchanter` is large-tier only, one per caravan. It buffs nearby mobs instead of attacking.

### Column size

| Column | Escort s/m/l | Split | Raiders s/m/l |
|---|---|---|---|
| Light | 2 / 4 / 6 | A 1/3/4, B 1/1/2 | 3 / 5 / 9 to 11 |
| Standard | 3 / 6 / 9 | A 2/4/5, B 1/2/4 | 4 / 7 / 12 to 14 |
| Standard, 3-part | 3 / 6 / 9 | A 2/3/4, B 1/2/3, C 0/1/2 | 4 / 7 / 12 to 14 |
| Heavy | 4 / 8 / 12 | A 2/4/5, B 1/3/4, C 1/1/3 | 5 / 9 / 15 to 17 |

Three-part escorts put archer, skirmisher and pillager in the same column. Used for gems, magic, war spoil, trinkets and machinery.

### Heavies

Heavy count scales inversely with strength. Attributes read from the deployed jars with `javap`.

| Strength | Count | Mob | HP | Damage | Armour | Speed |
|---|---|---|---|---|---|---|
| Boss-grade | 1 | golem_of_last_resort | 200 | 7 | 8 | 0.30 |
| Boss-grade | 1 | zapper | 100 | 7 | 8 | ranged, 0.8 knockback resist |
| Boss-grade | 1 | archevoker | 60 | spells | - | 0.25 |
| Heavy | 1 | ravager | 100 | 12 | - | 0.30 |
| Heavy | 1 | absorber | 100 | 15 | - | 0.15 |
| Heavy | 1 | crocofang | 40 | 8 | 3 | 0.35 |
| Mid | 2 | tesla_golem | 40 | 5 | - | 0.30 |
| Mid | 2 | iceologer | 40 | ranged | - | 0.50 |
| Light | 3 | illusioner | 32 | ranged | - | 0.35 |
| Light | 3 | witch | 26 | potions | - | 0.25 |
| Light | 3 | igniter | 24 | 5 | - | 0.35 |

archevoker is used as a leader, not a heavy: the caravans it fronts are already boss-grade at the head. Leaders are always 1.

absorber at speed 0.15 is half a ravager. It will trail a large caravan; `allowFollowerTeleport = true` is what keeps the column together.

Leaders are always 1. Archevoker-led caravans take a mid heavy, not a boss-grade one.

## Cargo

Every caravan has its own tables, so the loot reads as that caravan's and not as generic chest loot. 116 tables under `loot_tables/cargo/<caravan>/`.

| Table | Pools | Who gets it |
|---|---|---|
| `small` | staple rolls 2-3 | every caravan |
| `medium` | staple rolls 2-4, mid rolls 1 | every caravan |
| `large` | staple rolls 3-5, mid rolls 1-2, class prize rolls 1 against empty at weight 4 | every caravan |
| `prize` | the special, guaranteed 1, plus class prize rolls 1-2 and mid rolls 2-3 | 11 caravans |

Bigger is better twice over: more llamas (2 / 4 / 6) and a richer table on each one.

Each table is built from the caravan's cargo classes plus one or two signature items of its own. Dune Miners drops sand and TNT on top of Ore, Sculk Cultists drops sculk and echo shards on top of Magic dust and Runes.

### The special

Eleven caravans carry one Artifacts curio at large tier, and only there. The prize rides a single dedicated llama whose table grants exactly one, so a caravan can never drop two.

Each item is matched to the caravan by the family it is proposed for in [`accessories.md`](accessories.md).

| Caravan | Special | Family |
|---|---|---|
| Marauder Column | Superstitious Hat | PILLAGE |
| Illusionist's Troupe | Scarf of Invisibility | PILLAGE |
| Grave Consort | Panic Necklace | PILLAGE |
| Deep Delvers | Pocket Piston | PILLAGE |
| Geode Cutters | Power Glove | PILLAGE |
| Tomb Robbers | Cowboy Hat | DESERT |
| Idol Looters | Thorn Pendant | JUNGLE |
| Crystal Coven | Bunny Hoppers | JUNGLE |
| Sculk Cultists | Shock Pendant | SCULK |
| Wreckers | Angler's Hat | AQUATIC |
| Frostbound Wardens | Snowshoes | COLD |

The other 24 caravans top out at their class prize.

### Classes

The shared backbone each caravan's tables draw from.

| Class | Staple | Mid | Prize |
|---|---|---|---|
| Ore | raw copper, raw iron, coal | iron, gold, Create andesite alloy | diamond, netherite scrap |
| Gems | amethyst, lapis, Sea Dwellers aquamarine | emerald, quartz, Ascendant restorine | diamond block, Aquamirae abyssal amethyst |
| Magic dust | redstone, glowstone, blaze powder | Common and Uncommon Ink, enchanted scrap | Arcane Essence |
| Runes | blank runestone, Arcane Salvage | typed runes, Rare Ink, Geomancy geo rune | Upgrade Orb |
| Arcane metal | Arcane Salvage, gold, iron | Arcane Ingot, Arcane Cloth | Epic Ink |
| Harvest | wheat, carrots, potatoes, hay | bread, Farmer's Delight ham and rice | golden carrot |
| Provisions | glass, gunpowder, bottles | teas, Fruits Delight jelly | Vinery wine, apple crate |
| Textiles | string, wool, dyes | carpet, banner, flax | leather horse armor |
| Timber | oak and spruce logs, Ribbits mossy planks | iron axe, canvas, charcoal | diamond axe |
| Furs | leather, rabbit hide | Alex's Delight bison, bow, snowballs | rabbit's foot |
| Bone | bone, bone meal | archeology shards, musket cartridges | totem of undying |
| Fish | cod, salmon, kelp, RealmRPG fish | Ocean's and Aquamirae Delight dishes, glow bait | Angler's Fang |
| Alchemy | nether wart, spider eyes, mushrooms | brewing stand, fermented eye | ghast tear |
| Relics | archeology shards, bricks, Sea Dwellers copper relics | emerald, pottery sherds, brush | music disc, Growth and Radiance Totem |
| Trinkets | fireworks, dyes, RealmRPG illager nose | Iron's Spells scroll, emerald | Mystery Scroll Pouch |
| War spoil | iron, arrows, shield | Epic Knights kettlehat and chainmail, iron sword | Epic Knights knight chestplate |
| Salvage | maps, nautilus shells | compass, Sea Dwellers broken trident | heart of the sea, Aquamirae dead sea scroll |
| Machinery | copper, lightning rods, redstone | Companions wrench and voltaic relay, brass, zinc | Tesla Coil |
| Grave goods | bones, rotten flesh, soul sand | Gravestone obituary, skulls, soul lantern | totem of undying |
| Livestock | leather, wheat, leads | saddle, Alex's Delight mince | name tag |

Every non-vanilla id was checked against a dump of all 172 jars' lang files. Nothing from Waystones (warp stone is trader-gated), End Remastered or Botania appears in any table.

## The caravans

35 themes x 3 tiers = 105 variation files. Escort column is A + B + C.

| # | Caravan | Placement | Column | Leader | Escort | Heavy | Cargo |
|---|---|---|---|---|---|---|---|
| 1 | Dune Miners | `desert` | Standard | vindicator | pillager + legioner | 1 ravager | Ore |
| 2 | Tomb Robbers | `desert` `badlands` | Standard | evoker | pillager + archer | 1 absorber | Relics |
| 3 | Salt Traders | `salt_flats` | Light | vindicator | pillager + skirmisher | 3 igniter | Provisions |
| 4 | Canyon Prospectors | `badlands` | Standard | igniter | pillager + skirmisher | 2 tesla_golem | Ore |
| 29 | Mesa Diggers | `badlands` | Heavy | vindicator | pillager + legioner + skirmisher | 3 igniter | Ore + Gems |
| 35 | Outback Runners | `outback` | Light | hunter | archer + skirmisher | 1 ravager | Bone + Provisions |
| 5 | Jungle Poachers | `jungle` | Standard | hunter | pillager + archer | 1 ravager | Livestock + Furs |
| 6 | Idol Looters | `jungle` | Heavy | evoker | vindicator + legioner + archer | 2 tesla_golem | Gems + Relics |
| 7 | Bamboo Smugglers | `bamboo` | Light | illusioner | pillager + skirmisher | 3 illusioner | Provisions |
| 8 | Grain Reavers | `plains` | Standard | vindicator | pillager + skirmisher | 1 ravager | Harvest |
| 10 | Marauder Column | `plains` `savanna` | Heavy | absorber | vindicator + legioner + skirmisher | 1 golem_of_last_resort | War spoil |
| 27 | Tesla Column | `plains` | Heavy | zapper | legioner + pillager + skirmisher | 1 zapper | Machinery + Ore |
| 12 | Cattle Drovers | `savanna` | Standard | vindicator | pillager + skirmisher | 1 ravager | Livestock |
| 13 | Ivory Hunters | `savanna` | Standard | hunter | archer + pillager | 1 ravager | Bone |
| 9 | Dye Traders | `forest` | Light | illusioner | pillager + archer | 3 illusioner | Textiles |
| 11 | Timber Gang | `forest` `taiga` | Standard | vindicator | pillager + legioner | 1 ravager | Timber |
| 28 | Illusionist's Troupe | `forest` `taiga` | Standard, 3-part | illusioner | illusioner + pillager + archer | 3 illusioner | Trinkets |
| 34 | Harvest Wake | `autumnal` | Standard | evoker | pillager + legioner | 3 witch | Harvest + Textiles |
| 14 | Pelt Hunters | `taiga` `snowy` | Standard | hunter | pillager + archer | 1 ravager | Furs |
| 15 | Frostbound Wardens | `snowy` `mountain` | Standard, 3-part | iceologer | legioner + pillager + archer | 2 iceologer | Magic dust + Runes |
| 16 | Ice Fishers | `coast` | Light | hunter | pillager + archer | 1 ravager | Fish |
| 24 | Wreckers | `coast` | Light | vindicator | pillager + archer | 1 ravager | Salvage |
| 17 | Bog Alchemists | `swamp` | Standard | witch | pillager + archer | 3 witch | Alchemy |
| 18 | Fen Poisoners | `swamp` | Standard | witch | witch + archer | 3 witch | Alchemy + Magic dust |
| 33 | Ford Wardens | `river` | Light | vindicator | pillager + skirmisher | 1 ravager | Fish + Provisions |
| 19 | Grave Consort | `dark_forest` | Standard | evoker | vindicator + legioner | 1 absorber | Grave goods |
| 20 | Carnival Column | `dark_forest` | Standard, 3-part | illusioner | illusioner + pillager + archer | 3 illusioner | Trinkets + Livestock |
| 21 | Deep Delvers | `mountain` | Heavy | absorber | pillager + legioner + skirmisher | 2 tesla_golem | Ore + Gems |
| 22 | Ore Train | `mountain` `volcanic` | Heavy | absorber | legioner + skirmisher + pillager | 1 golem_of_last_resort | Ore + Machinery |
| 30 | Geode Cutters | `mountain` | Heavy | vindicator | skirmisher + pillager + archer | 1 ravager | Gems |
| 23 | Ash Scavengers | `volcanic` | Standard, 3-part | igniter | pillager + skirmisher + archer | 1 crocofang | Magic dust |
| 31 | Rune Smelters | `volcanic` | Heavy | igniter | legioner + pillager + skirmisher | 2 tesla_golem | Arcane metal + Ore |
| 25 | Sporecallers | `mystical` | Standard | archevoker | pillager + skirmisher | 1 absorber | Alchemy |
| 26 | Sculk Cultists | `mystical` | Standard, 3-part | archevoker | pillager + vindicator + archer | 1 absorber | Magic dust + Runes |
| 32 | Crystal Coven | `mystical` | Heavy | archevoker | pillager + skirmisher + archer | 1 absorber | Runes + Gems |

## Animals

Every caravan has pack llamas. A **beast** is a live local animal travelling with the column: 9 caravans have one, 26 have none. A **drove** caravan is mostly animals and drops to 2 llamas at every tier.

| Caravan | Shape | s / m / l | Beast | Native to | w |
|---|---|---|---|---|---|
| Cattle Drovers | Drove | 3 / 6 / 9 | cow, `alexsmobs:emu` | vanilla savanna; `#minecraft:is_savanna` | 20 |
| Carnival Column | Drove | 3 / 6 / 9 | `alexsmobs:potoo`, `alexsmobs:raccoon`, wolf | `minecraft:dark_forest`; forest + taiga; vanilla forest | 15, 10 |
| Jungle Poachers | Single | 0 / 1 / 2 | `alexsmobs:capuchin_monkey` | `#minecraft:is_jungle` not bamboo | 28 |
| Ivory Hunters | Single | 0 / 1 / 2 | `alexsmobs:elephant` | `#minecraft:is_savanna` | 30 |
| Grain Reavers | Single | 0 / 1 / 2 | horse | vanilla plains | - |
| Pelt Hunters | Single | 0 / 1 / 2 | wolf | vanilla forest and taiga | - |
| Ice Fishers | Single | 0 / 1 / 2 | `alexsmobs:seal` | `#minecraft:is_beach`, cold ocean, `stony_shore` | 20 |
| Wreckers | Single | 0 / 1 / 2 | `alexsmobs:seagull` | `#minecraft:is_beach`, `stony_shore` | 21 |
| Grave Consort | Single | 0 / 1 / 2 | `alexsmobs:crow` | `#minecraft:is_forest` incl. dark forest, plains, taiga | 10 |

A beast must spawn in the Overworld, have a weight above 0 in `alexsmobs.toml`, and have a spawn rule covering the whole placement rather than one biome inside it.

ravager, crocofang and tesla_golem are raiders, not wildlife. They fill the heavy slot and carry no biome rule.

Llama carpet colour keys the caravan on sight: orange ore, yellow desert, green jungle, white snow, purple magic, black grave and sculk, red war, blue coast, brown harvest.

## File layout

```
datapacks/Pillager_Caravans/
  pack.mcmeta
  README.md
  data/magic_caravans/caravans/placement/*.json     19 files
  data/magic_caravans/caravans/variation/*.json    105 files (35 themes x 3 tiers)
  data/magic_caravans/tags/worldgen/biome/*.json    19 files
  data/magic_caravans/loot_tables/cargo/<caravan>/*.json  116 files, 35 caravans x small/medium/large + 11 prize
resourcepacks/Caravan_Names/
  pack.mcmeta
  assets/magic_caravans/lang/en_us.json            105 keys
```

Measured entity totals across the generated files:

| Shape | small | medium | large |
|---|---|---|---|
| Normal | 5 to 9 | 9 to 13 | 15 to 23 |
| Drove | 9 | 15 | 23 to 25 |

Each llama carries a `cargo` field pointing at its caravan tier table. On a large tier with a special, one llama is split off onto the `prize` table.

The built-in `classic_caravans` pack overlaps desert, jungle, plains, snow and swamp. Leaving it on roughly halves the chance of a custom caravan there.

## Checked

Resolved against the deployed jars before implementing.

| Item | Result |
|---|---|
| Beasts can be leashed and follow | **Pass.** All nine beasts extend `Animal` or `TamableAnimal`: elephant, crow, raccoon and capuchin_monkey are `TamableAnimal`; seal, seagull, potoo and emu are `Animal`. `CaravanVariation.leashAnimals` handles both. |
| Raid credit | **Not an issue.** `CaravanMember.spawn` finalizes members with `MobSpawnType.PATROL`, not a raid spawn. Members never join a raid or touch bad omen. |
| Golem walk speed | **Fine.** golem_of_last_resort and tesla_golem are both 0.30, the same as a ravager and faster than a pillager at 0.35 only by a hair. The slow one is absorber at 0.15. |
| autumnal beasts | **Available.** EE puts `autumnal_forest`, `autumnal_forest_edge`, `autumnal_birch_forest` and `autumnal_flower_forest` in `#minecraft:is_forest`, and `autumnal_plains` in `#forge:is_plains`. Forest beasts (crow, raccoon, blue_jay, wolf) are valid there if one is ever wanted. |
| crocofang riderless | **Cosmetic only.** It reads `getPassengers()` and the config list `crocofang_rideableMobs`, but nothing requires a rider. The caravan format cannot mount one, so it walks empty. |
| Cave biomes | **Cut.** No caravan spawns underground. |
| Structurify and Mob Control | **No conflict.** Neither touches caravans. |

## To verify in game

| Item | Why |
|---|---|
| absorber pace | Speed 0.15 against a caravan moving at 0.35. It will trail badly. `allowFollowerTeleport = true` should snap it back, but watch whether a large caravan visibly breaks apart. |
| iceologer as escort and heavy | It drops ice blocks on targets and could bury its own caravan. Two of them doubly so. |
| 3 witch, 3 illusioner, 3 igniter | Three casters may be worse than their individual stats suggest once spells overlap. Tune counts. |
| tesla_golem and the Tesla network | It uses Companions' Tesla behaviour system. Check it does not need a Tesla Coil block nearby. |
| Enchanter buff stacking | Check what it does to a cargo llama, and whether 6 enchanted llamas make a large caravan unkillable. |
| archevoker `PatrolNearLocationGoal` | May anchor to a patrol point instead of following the leader. |
| archevoker friendly fire | `HurtByTargetGoal` has no `Raider` exclusion. Confirm a stray bolt does not turn it on its own caravan. |
| Vanilla beasts under Expanded Ecosphere | EE rewrites all 52 surviving vanilla biomes' spawn lists. cow, horse and wolf are placed on their classic families by assumption, not by reading EE's lists. |
| `outback` beasts | Only `eucalyptus_woodland` carries `#minecraft:is_savanna`; `outback`, `outback_desert` and `ayers_rock` carry none of the checked tags. Outback Runners has no beast for that reason. |
