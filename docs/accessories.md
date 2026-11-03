# Accessories & Relics — Loot Placement Reference

Working sheet for deciding which loot family each wearable belongs to. Companion to [`structures.md`](structures.md).

**149 items from 4 mods:** Artifacts 45 · Relics 30 · More Relics 29 · Unique Accessories 45.

**Family** uses the 11-theme vocabulary Relics already ships (below). **Proposed** is a suggestion, not a decision.

## How each mod distributes its items today

| Mod | Mechanism | Reaches modded structure chests? |
|---|---|---|
| **Relics** | `relics:relic_loot` GLM, matches loot table ids by **regex across all namespaces** (e.g. `[\w]+:chests/.*desert.*`) | **Yes** — already lands in DnT, DNL, Wythers, YUNG's chests |
| **More Relics** | Same `LootCollections` regex system, plus 4 hardcoded vanilla chests | **Yes**, except the 4 hardcoded ones |
| **Artifacts** | 34 GLMs, each pinned with `forge:loot_table_id` to one **vanilla** table | No |
| **Unique Accessories** | GLMs pinned to vanilla tables + mob drops; drops an **Accessory Box** that rolls common/rare/epic | No |

Relics and More Relics self-distribute. Artifacts and Unique Accessories are invisible in every modded structure.

## Family vocabulary

| Family | Matches loot table ids containing |
|---|---|
| `VILLAGE` | `village` |
| `PILLAGE` | `pillag`, `outpost`, + `woodland_mansion` |
| `DESERT` | `desert`, `sand`, `pyramid` |
| `JUNGLE` | `jungle`, `temple` |
| `AQUATIC` | `water`, `ocean`, `river`, `ship`, `aqua`, + `buried_treasure` |
| `COLD` | `fros`, `taiga`, `cold`, `winter`, `snow`, `ice`, `glac`, + `igloo_chest` |
| `NETHER` | `nether`, `hell`, `lava`, `magma`, `fire`, `burn`, `fortress`, + `ruined_portal` |
| `BASTION` | `bastion`, `piglin` |
| `END` | `end`, `warp` |
| `SCULK` | `warden`, `sculk`, `echo`, + `ancient_city` |
| `ANTHROPOGENIC` | `mineshaft`, `city`, `stronghold` |

## Artifacts (45)

Flat rarity — every artifact is weight 8 in `artifacts:artifact`, scaled by a single global `artifactRarity: 1.0`. Tiering is entirely ours to assign. `v.` = village sub-chest, `arch:` = suspicious sand/gravel.

### Head (8)

| Item | Effect | Currently in | Proposed |
|---|---|---|---|
| Angler's Hat | +Luck of the Sea and Lure levels | buried_treasure, jungle_temple, underwater_ruin_big, v.tannery, arch:ocean_ruin_cold | AQUATIC |
| Cowboy Hat | Speed while mounted | nether_bridge, ruined_portal, bastion_hoglin_stable, arch:desert_pyramid | DESERT |
| Night Vision Goggles | Night vision | abandoned_mineshaft, nether_bridge, ruined_portal, shipwreck_treasure, arch:desert_pyramid | ANTHROPOGENIC |
| Novelty Drinking Hat | Faster eating and drinking | — | VILLAGE |
| Plastic Drinking Hat | Faster eating and drinking | — | VILLAGE |
| Snorkel | Water breathing | buried_treasure, shipwreck_treasure, underwater_ruin_big, arch:desert_pyramid | AQUATIC |
| Superstitious Hat | +Looting level | abandoned_mineshaft, pillager_outpost, stronghold_corridor, underwater_ruin_big, v.armorer, arch:desert_well | PILLAGE |
| Villager Hat | +Villager reputation | igloo_chest, v.desert_house, v.plains_house, v.savanna_house, v.snowy_house, v.taiga_house, arch:trail_ruins_rare | VILLAGE |

### Necklace (8)

| Item | Effect | Currently in | Proposed |
|---|---|---|---|
| Charm of Sinking | Walk on the bottom underwater | ancient_city, desert_pyramid, shipwreck_treasure, v.temple, arch:desert_well | AQUATIC |
| Cross Necklace | Longer invincibility after a hit | bastion_treasure, nether_bridge, pillager_outpost, underwater_ruin_big, v.temple | VILLAGE |
| Flame Pendant | Chance to ignite attackers, grants fire resistance | bastion_hoglin_stable, desert_pyramid, arch:trail_ruins_rare | NETHER |
| Lucky Scarf | +Fortune level | buried_treasure, igloo_chest, ruined_portal, stronghold_corridor, underwater_ruin_big, arch:ocean_ruin_cold | ANTHROPOGENIC |
| Panic Necklace | Speed when damaged | abandoned_mineshaft, bastion_treasure, pillager_outpost | PILLAGE |
| Scarf of Invisibility | Invisibility | desert_pyramid, pillager_outpost, shipwreck_treasure, v.tannery | PILLAGE |
| Shock Pendant | Chance to strike attackers with lightning | ancient_city, desert_pyramid, arch:desert_pyramid | SCULK |
| Thorn Pendant | Chance to damage attackers | desert_pyramid, ruined_portal, arch:trail_ruins_rare | JUNGLE |

### Belt (7)

| Item | Effect | Currently in | Proposed |
|---|---|---|---|
| Antidote Vessel | Caps negative effect duration | bastion_treasure, stronghold_corridor, v.temple, arch:ocean_ruin_cold | VILLAGE |
| Chorus Totem | On lethal damage teleports and heals, cooldown | end_city_treasure, woodland_mansion | END |
| Cloud in a Bottle | Sprint-jump dash | abandoned_mineshaft, end_city_treasure, pillager_outpost, underwater_ruin_big, arch:trail_ruins_rare | ANTHROPOGENIC |
| Crystal Heart | +Max health | bastion_treasure, end_city_treasure, pillager_outpost | BASTION |
| Helium Flamingo | Timed flight, recharges | ancient_city, end_city_treasure, stronghold_corridor | END |
| Obsidian Skull | Fire resistance after burning, cooldown | abandoned_mineshaft, ruined_portal, shipwreck_treasure, arch:desert_pyramid | NETHER |
| Universal Attractor | Item magnet | desert_pyramid, ruined_portal, shipwreck_treasure, arch:desert_well | ANTHROPOGENIC |

### Hands (9)

| Item | Effect | Currently in | Proposed |
|---|---|---|---|
| Digging Claws | +Dig speed and tool tier | abandoned_mineshaft, buried_treasure, v.toolsmith, arch:ocean_ruin_cold | ANTHROPOGENIC |
| Feral Claws | +Attack speed | buried_treasure, shipwreck_treasure, v.weaponsmith, arch:trail_ruins_rare | JUNGLE |
| Fire Gauntlet | Attacks ignite | bastion_treasure, ruined_portal, underwater_ruin_big, arch:ocean_ruin_cold | NETHER |
| Golden Hook | +XP from kills | bastion_treasure, buried_treasure, shipwreck_treasure | AQUATIC |
| Onion Ring | Haste after eating | abandoned_mineshaft, ancient_city, desert_pyramid, arch:ocean_ruin_warm | VILLAGE |
| Pickaxe Heater | Auto-smelts mined ores | abandoned_mineshaft, ancient_city, bastion_hoglin_stable, stronghold_corridor, arch:trail_ruins_rare | ANTHROPOGENIC |
| Pocket Piston | +Knockback | nether_bridge, pillager_outpost, v.toolsmith, arch:trail_ruins_rare | PILLAGE |
| Power Glove | +Attack damage | pillager_outpost, ruined_portal, stronghold_corridor, underwater_ruin_big, arch:desert_pyramid | PILLAGE |
| Vampiric Glove | Chance to heal on hit | abandoned_mineshaft, desert_pyramid, v.armorer, arch:ocean_ruin_warm | SCULK |

### Feet (8)

| Item | Effect | Currently in | Proposed |
|---|---|---|---|
| Aqua-Dashers | Sprint across water | abandoned_mineshaft, ancient_city, buried_treasure, stronghold_corridor, arch:ocean_ruin_warm | AQUATIC |
| Bunny Hoppers | Jump boost, cancels fall damage | bastion_hoglin_stable, buried_treasure, jungle_temple, v.tannery, arch:ocean_ruin_warm | JUNGLE |
| Flippers | +Swim speed | buried_treasure, shipwreck_treasure, underwater_ruin_big | AQUATIC |
| Kitty Slippers | Scares creepers | buried_treasure, jungle_temple, v.tannery, arch:desert_pyramid | VILLAGE |
| Rooted Boots | Refills hunger on grass, grows plants | ancient_city, jungle_temple, pillager_outpost, stronghold_corridor, arch:trail_ruins_rare | JUNGLE |
| Running Shoes | +Speed and step height | nether_bridge, shipwreck_treasure, v.armorer, arch:ocean_ruin_warm | VILLAGE |
| Snowshoes | No slipping, walk on powder snow | ancient_city_ice_box, igloo_chest, v.snowy_house, v.taiga_house, arch:trail_ruins_rare | COLD |
| Steadfast Spikes | Knockback resistance | bastion_treasure, pillager_outpost, shipwreck_treasure, v.armorer, arch:trail_ruins_rare | BASTION |

### Any slot / not a curio (5)

| Item | Slot | Effect | Currently in |
|---|---|---|---|
| Whoopee Cushion | any | Fart chance | desert_pyramid, spawn_bonus_chest, v.tannery, arch:trail_ruins_rare |
| Umbrella | held | Shield and glider | buried_treasure, desert_pyramid, v.tannery, arch:desert_well |
| Everlasting Beef | food | Reusable food | bastion_hoglin_stable, v.butcher |
| Eternal Steak | food | Reusable food | cooked Everlasting Beef |
| Mimic Spawn Egg | egg | Spawns a mimic | not chest loot |

## Relics (30)

Levelling relics with abilities. **Family** is the mod's own `LootCollections` assignment, already active by regex — these already appear in modded chests whose id matches.

| Relic | Slot | Family | Ability |
|---|---|---|---|
| Quiver | back | VILLAGE | Internal arrow storage, shot from first |
| Elytra Booster | back | END | Burns fuel to dash mid-elytra flight |
| Midnight Robe | back | ANTHROPOGENIC | Invisibility and speed in low light |
| Drowned Belt | belt | AQUATIC | +1 relic slot; levels off Riptide tridents |
| Hunter Belt | belt | VILLAGE | +1 relic slot; levels off pet attacks |
| Leather Belt | belt | VILLAGE | +1 relic slot |
| Blazing Flask | belt | NETHER | Creates a flight zone sized by nearby fire |
| Spore Sack | charm | JUNGLE | Throws exploding poison spores when hurt |
| Amphibian Boot | feet | AQUATIC | Swim speed scales with swim duration |
| Aqua Walker | feet | AQUATIC | Walk on water until the timer expires |
| Ice Breaker | feet | COLD | Faster falling, knockback resist, no slipping |
| Ice Skates | feet | COLD | Speed on ice scales with slide duration |
| Magma Walker | feet | NETHER | Walk on lava until overheated |
| Roller Skates | feet | ANTHROPOGENIC, VILLAGE | Speed scales with duration, all blocks slippery |
| Ender Hand | hands | END | Endermen neutral; End transposition |
| Rage Glove | hands | NETHER | Stacking damage and speed from rapid attacks |
| Wool Mitten | hands | COLD | Forms solid snowballs that stun and freeze |
| Holy Locket | necklace | DESERT | Redirects healing as damage, or drains it |
| Jellyfish Necklace | necklace | AQUATIC | Wearer does not sink |
| Reflecting Necklace | necklace | NETHER | Stores damage, then bursts into obsidian shards |
| Bastion Ring | ring | BASTION | Piglins neutral, points to nearby bastions |
| Chorus Inhibitor | ring | END | Chorus fruit teleports along line of sight |
| Infinity Ham | — | VILLAGE | Rechargeable food charges |
| Magic Mirror | — | ANTHROPOGENIC | Teleports to respawn point |
| Shadow Glaive | — | END, SCULK | 8 charges of a bouncing glaive |
| Horse Flute | — | ANTHROPOGENIC, VILLAGE | Stores and releases a horse |
| Spatial Sign | — | ANTHROPOGENIC | Rewinds the wearer along their path |
| Space Dissector | — | NETHER | Places a linked pair of portals |
| Frozen Snowball | — | crafted | Made by the Wool Mitten |
| Relic Experience Bottle | — | not loot | Levels relics |

## More Relics (29)

Same `LootCollections` system. Five have no collection and no injection — progression or upgrade items, not chest loot.

| Relic | Slot | Family | Ability |
|---|---|---|---|
| Guts Orb | charm | BASTION, DESERT, NETHER | Extra damage while debuffed or on fire |
| Thermoseismic Heart | body | DESERT | Immune to explosions, gains Overload stacks |
| Whims of Fate | charm | BASTION, DESERT | d20 per hit: 20 negates the damage, 1 doubles it |
| Opal Necklace | necklace | COLD, DESERT | Negates all damage once, then cooldown |
| Runic Plate | back, charm | ANTHROPOGENIC, COLD | First hits taken in combat are reduced |
| Swiftedge | back, charm | ANTHROPOGENIC, COLD | First hits dealt in combat are boosted |
| Shieldweave Cape | back | ANTHROPOGENIC, COLD, VILLAGE | Armor-scaled chance to block a hit, max 33% |
| Sentient Rust | hands | ANTHROPOGENIC, AQUATIC, JUNGLE | Stacking armor corrosion, −5% per stack |
| Depleted Spool | belt, charm | ANTHROPOGENIC, PILLAGE | Inert until restored into the Weavers Spool |
| Axolotl Cream | belt, charm | AQUATIC | Increases healing from any source |
| Biojoint | belt | NETHER | Damage reduction scaling with the hit size |
| Crown of the Legend | head | BASTION, NETHER | Extra damage to pig-type mobs |
| Tyrant Mask | head | BASTION | Cursed: lowers speed, damage, attack speed |
| Eject Button | belt, charm, ring | END, NETHER, VILLAGE | Speed below 20% health |
| Mass Gauntlet | hands | END, NETHER, VILLAGE | Attacks deal % of max health as bonus damage |
| Moodworm | ring | END, NETHER, VILLAGE | Rotates a buff every 10 minutes |
| Bionic Eye | head | SCULK | Applies Glowing and Vulnerability in a radius |
| Gravitum Glove | hands | END, SCULK | Multiplies damage, −66% attack speed |
| Gravitum Strider | feet | END, SCULK | Reduced gravity and adjusted fall damage |
| VertebraX | back | END, SCULK | Continuous poison; filters harmful effects |
| Slumbering Amulet | necklace | buried_treasure | Awakens into the Whispering Amulet |
| Converging Orb | back, belt, body | woodland_mansion | Permanently compresses each time you are hit |
| Twin Fangs | hands | woodland_mansion, pillager_outpost | Splits each attack in two |
| Epoch Apple | — | simple_dungeon | Regrows after being eaten |
| Whispering Amulet | necklace | not loot | Guides you to a location |
| Made in Heaven | all slots | not loot | Endgame upgrade, boosts every stat |
| Wonder of U | back, belt, body | not loot | Reflects damage to the attacker |
| King Crimson | head | not loot | Rewinds you 10 seconds |
| Weavers Spool | belt, charm | not loot | Restored Depleted Spool, stores 10 silk |

## Unique Accessories (45)

Explicit rarity tiers via the **Accessory Box** (`common` 80 · `rare` 18 · `epic` 2). The box drops in 18 vanilla chests at ~3%. Entries naming a mob are drops, not chest loot.

### Epic (5)

| Item | Slot | Source today |
|---|---|---|
| Sun Stone | charm | ancient_city |
| Moon Stone | charm | end_city_treasure |
| Burning Soul | charm | warden |
| Master Ninja's Tabi | feet | box only |
| Ender Lens | charm | box only |

### Rare (13)

| Item | Slot | Source today |
|---|---|---|
| Golden Egg | charm | box only |
| Royal Honeycomb | charm | box only |
| Soul Gem | charm | stronghold_corridor, stronghold_crossing |
| Devil's Eyeball | charm | bastion_treasure, nether_bridge |
| Tabi | feet | pillager, vindicator |
| Magick Quiver | charm | pillager_outpost |
| Shiny Stone | charm | jungle_temple |
| Championship Belt | belt | box only |
| Silver Cat Tail | belt | woodland_mansion |
| Rocket Shoes | feet | box only |
| Still Beating Heart | charm | piglin_brute |
| Power Crystal | charm | abandoned_mineshaft |
| Rusty Guillotine Blade | charm | box only |

### Common (25)

| Item | Slot | Source today |
|---|---|---|
| Strong Sticky Slime Ball | charm | slime |
| Starved Wolf Skull | charm | desert_pyramid, desert_well |
| Ancient Chisel | charm | desert_pyramid |
| Withered Heart | charm | nether_bridge |
| Small Propulsion Device | charm | village houses, village_weaponsmith |
| Supreme Meat | charm | hoglin |
| Amplifier Stone | charm | abandoned_mineshaft |
| Fossil Shark Tooth | charm | ocean_ruin_cold, ocean_ruin_warm |
| Cursed Doll Head | charm | witch |
| Snow Golem Doll | charm | igloo_chest |
| Survival Belt | belt | village houses |
| Black Belt | belt | pillager, vindicator |
| Tool Belt | belt | village houses, village_toolsmith |
| Life Jacket Belt | belt | shipwreck_treasure |
| Waist Warmer | belt | village houses |
| Sharp Fishing Hook | charm | shipwreck_supply |
| Blade Shoes | feet | village houses |
| Durable Spring | feet | village houses, village_armorer |
| Anklet of Fortune | feet | village houses |
| All-Purpose Seasoning | charm | village houses |
| Bloody Knife | charm | husk, witch, zombie |
| Suspicious Mushroom | charm | mooshroom |
| Ninjutsu Manual | charm | box only |
| Large Paddle | charm | village houses, village_fisher |
| Rose of Temptation | charm | simple_dungeon |

Not in any box tier: **Hero Emblem** (charm) — Ender Dragon drop. The **Accessory Box** itself is the 45th item.
