# Magic Minecraft Server, Modpack Wiki

Gameplay changes on this server compared to the mods' defaults. For the biomes that generate here, see [`biomes.md`](biomes.md). Bugs, profiling and implementation detail are in [`technical-notes.md`](technical-notes.md).

## Contents

1. [Banned Mobs](#banned-mobs)
2. [Game Rules](#game-rules)
3. [Mob Spawn Rates](#mob-spawn-rates)
4. [Eyes of Ender](#eyes-of-ender)
5. [Attributes](#attributes)
6. [Waystones](#waystones)
7. [Enchanting & XP](#enchanting--xp)

---

## Banned Mobs

### Alex's Mobs

These do not spawn:

| Mob | Why |
|---|---|
| Fly | Swarm lag, no value |
| Cockroach | Breeds rapidly, entity lag |
| Mungus | Spreads mushrooms, clutter |
| Farseer | World-border ranged pest |
| Enderiophage | Multiplies and teleports |
| Guster | Sandstorm particles, ranged sand attacks |
| Leafcutter Ant | Known TPS killer |
| Anaconda | Large aggressive water snake |
| Anteater | Owner preference |
| Triops | Overpopulates water |
| Banana Slug | Clutter |
| Terrapin | Overpopulates coasts |
| Flying Fish | Overpopulates water |
| Lobster | Overpopulates coasts |
| Comb Jelly | Overpopulates water |
| Tiger | Owner preference |
| Gazelle | Owner preference |
| Blobfish | Clutter |
| Underminer | Tunneling illager, destructive |

Set via `<mob>SpawnWeight = 0` in [`mod_configs_server/alexsmobs.toml`](../server_configuration/mod_configs_server/alexsmobs.toml). Leafcutter anthills via `leafcutterAnthillSpawnChance = 0.0`.

### Born in Chaos

| Mob | Why |
|---|---|
| Corpse Fly | Swarm clutter, lag |
| Bloody Gadfly | Swarm clutter, lag |
| Door Knight | Owner preference |

Maggots no longer appear from corpses on death. Born in Chaos has no config file, so these are set through the game-rules datapack.

---

## Game Rules

Datapack [`datapacks/Server_Game_Rules/`](../datapacks/Server_Game_Rules/), one function file per mod.

### Born in Chaos

| Rule | State |
|---|---|
| `disappearanceofSpiritsUndertheSun` | on |
| `maggotsAppearance` | off |
| `corpseFlySpawn` | off |
| `bloodyGadflySpawn` | off |
| `doorKnightSpawn` | off |
| `soulStratificationEffect` | on |
| `theappearanceoftheNightmareStalker` | on |
| `generationofInfectedDiamonds` | on |
| `naughtinessMechanics` | on |

---

## Mob Spawn Rates

| Mob | Change |
|---|---|
| Catfish | Spawn weight 4 to 2 |

---

## Eyes of Ender

End Remastered's default chest sources (mineshaft, desert pyramid, igloo, jungle temple, nether bridge, simple dungeon) are off. Eyes come from bosses and a smaller set of chests instead. Datapack [`datapacks/eyes_of_ender_drops/`](../datapacks/eyes_of_ender_drops/).

Bosses, guaranteed drop:

| Eye | Boss | Mod |
|---|---|---|
| `old_eye` | Ancient Remnant | Cataclysm |
| `lost_eye` | The Harbinger | Cataclysm |
| `nether_eye` | Netherite Monstrosity | Cataclysm |
| `exotic_eye` | Coralssus | Cataclysm (only source, recipe removed) |
| `cold_eye` | Lich | Bosses of Mass Destruction (player kill) |
| `rogue_eye` | Sculptor | Mowzie's Mobs (player kill) |
| `undead_soul`, craft into `undead_eye` | Maledictus | Cataclysm |

Regular mobs:

| Eye / item | Mob | Chance |
|---|---|---|
| `guardian_eye` | Elder Guardian | 30% |
| `magical_eye` | Evoker | 5% |
| `wither_eye` | Wither | 90% ×2 rolls |
| `witch_pupil`, craft into `witch_eye` | Witch | 10% |

Chests:

| Eye | Chest | Chance |
|---|---|---|
| `cursed_eye` | Bastion treasure | 50% |
| `black_eye` | Buried treasure | 30% |
| `black_eye` | Shipwreck treasure | 5% |
| `corrupted_eye` | Pillager outpost | 30% |
| `magical_eye` | Woodland mansion | 10% |

Two eyes are crafted: `undead_eye` (shaped) and `witch_eye` (`ender_eye` + `witch_pupil`, shapeless).

---

## Attributes

Several mods register their own copy of the same stat, so gear granted "Critical Damage" twice under two names and only one counted. The duplicates are merged into one stat each, so bonuses from every mod stack on one number:

| Stat | Fed by |
|---|---|
| Crit Damage | Unique Accessories, Cataclysm, Aquamirae, Iron's Lib |
| Crit Chance | Unique Accessories, Aquamirae |
| Dodge Chance | Unique Accessories, Aquamirae |
| Arrow Damage, Armor Pierce, Mining Speed, Healing Received, Experience Gained | Unique Accessories, Iron's Lib |

Item tooltips show the merged stat, so a weapon's tooltip and the character sheet agree.

Attribute names in the GUI and on tooltips are labelled with their source mod (`Crit Damage (Apotheosis)`, `Parry (Obscure API)`) via the client resource pack [`resourcepacks/Attribute_Mod_Labels/`](../resourcepacks/Attribute_Mod_Labels/). Enable it above the mod defaults.

### Dodge chance

| Source | Effect |
|---|---|
| Black Belt (belt, Unique Accessories) | +15% dodge, plus temporary attack-damage and move-speed buffs on a successful dodge |
| Championship Belt (belt, Unique Accessories) | +5% dodge, plus armor |
| Hero Emblem (charm, Unique Accessories) | +20% dodge, only at 1 HP |
| Moon Elf "Nimble" (Medieval Origins) | 20% chance to negate any incoming damage |

The Moon Elf dodge is not an attribute. It does not show in the Attributes GUI and rolls separately, so Black Belt plus Moon Elf is 32% effective, not 35%. It also covers any damage type, while the Apothic dodge stat only applies to melee damage from nearby entities.

---

## Waystones

| What | Value |
|---|---|
| World-gen spacing | ~500 blocks |
| World-gen waystones | Cannot be mined |
| Warp cost | 1 level minimum, 5 levels maximum, scaling 1 level per 1000 blocks |
| Free warps | None. Warp stones, sharestones, portstones, warp plates and the inventory button all pay the same cost |
| Warp Stone | Not craftable. Planned as a trader trade |
| Waystone | Recipe requires a Warp Stone, so also uncraftable. World-gen only |

XP levels get progressively more expensive, so a flat few-level cost is a larger share of early-game XP than late.

Settings in [`mod_configs_server/waystones-common.toml`](../server_configuration/mod_configs_server/waystones-common.toml), recipe changes in [`kubejs/server_scripts/waystones_balance.js`](../kubejs/server_scripts/waystones_balance.js).

---

## Enchanting & XP

Ascendant Arcana replaces vanilla enchanting with a capacity/relic system and disables vanilla XP by default. XP is kept on here: relic infusion and enchanting spend it, and waystone warps are XP-gated.

| Setting | Value | Effect |
|---|---|---|
| `disable_xp` | `false` | XP orbs, levels and the XP economy stay enabled (mod default is `true`) |
| `hide_xp_bar` | `false` | XP bar stays on the HUD |
| `xp_per_level` | `30` | Linear curve, 30 XP per level |

[`mod_configs_server/ascendant_arcana/server.json5`](../server_configuration/mod_configs_server/ascendant_arcana/server.json5).

---

## Conventions for this wiki

Gameplay changes only. For mobs include the why, for game rules just the rule name and state. Current state only, tasks live in [`TODO.MD`](../TODO.MD). No client/visual tweaks, no bug writeups, no implementation detail: those go in [`technical-notes.md`](technical-notes.md).
