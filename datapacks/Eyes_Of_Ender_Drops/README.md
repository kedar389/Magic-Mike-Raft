# eyes_of_ender_drops

Reworks where **End Remastered** (`endrem`) eyes come from. End Remastered
replaces the single vanilla Eye of Ender with ~12 unique eyes that must each be
found to open the End portal. This datapack **boss-gates** most eyes, they drop
(guaranteed) from modded bosses, while keeping a few in vanilla chests/mobs and
**disabling** the mod's default random-chest sources for eyes that now come from
a boss.

> Scope: only changes **loot tables + Forge loot modifiers + recipes**. It does
> not add worldgen or entities. pack_format 15 (MC 1.20.1).

## Where each eye comes from now

### Boss drops, guaranteed (full loot-table overrides)

| Eye | Boss | Mod | Notes |
|---|---|---|---|
| `old_eye`     | Ancient Remnant      | Cataclysm | guaranteed ×1 |
| `lost_eye`    | The Harbinger        | Cataclysm | guaranteed ×1 |
| `nether_eye`  | Netherite Monstrosity| Cataclysm | guaranteed ×1 |
| `exotic_eye`  | Coralssus            | Cataclysm | guaranteed ×1 (only source, recipe removed) |
| `cold_eye`    | Lich                 | Bosses of Mass Destruction | guaranteed on **player** kill |
| `rogue_eye`   | Sculptor             | Mowzie's Mobs | guaranteed on **player** kill |
| `undead_soul` *(component)* | Maledictus | Cataclysm | guaranteed ×1 → craft `undead_eye` |

These files fully redefine each mob's loot table (the boss still drops its normal
items too).

### Injected into vanilla chests (Forge loot modifiers, added on top of vanilla loot)

| Eye | Chest | Chance |
|---|---|---|
| `cursed_eye`    | Bastion treasure   | 50% |
| `black_eye`     | Buried treasure    | 30% |
| `black_eye`     | Shipwreck treasure | 5%  |
| `corrupted_eye` | Pillager outpost   | 30% |
| `magical_eye`   | Woodland mansion   | 10% |

### Injected into vanilla mob drops

| Eye / item | Mob | Chance |
|---|---|---|
| `guardian_eye`  | Elder Guardian | 30% |
| `magical_eye`   | Evoker | 5% |
| `wither_eye`    | Wither | 90% per roll, **2 rolls** |
| `witch_pupil` *(component)* | Witch | 10% → craft `witch_eye` |

### Crafted (recipes)

| Eye | Recipe | Ingredients |
|---|---|---|
| `undead_eye` | shaped | phantom membrane, bone, `undead_soul`, rotten flesh, ghast tear |
| `witch_eye`  | shapeless | vanilla `ender_eye` + `witch_pupil` |

## Disabled defaults

To avoid double-sourcing eyes that are now boss drops, these End Remastered
loot modifiers are neutered (`random_chance: 0.0`, so they never inject):

| Source (disabled) | Eye it would have given | Now comes from |
|---|---|---|
| Abandoned mineshaft chest | `lost_eye` | The Harbinger |
| Desert pyramid chest | `old_eye` | Ancient Remnant |
| Igloo chest | `cold_eye` | Lich |
| Jungle temple chest | `rogue_eye` | Sculptor |
| Nether bridge chest | `nether_eye` | Netherite Monstrosity |
| Simple dungeon chest | `old_eye` / `lost_eye` | bosses above |
| Skeleton horse | `undead_soul` | Maledictus |

Also: the **`exotic_eye` crafting recipe is removed** (overridden to produce
`minecraft:air`), the exotic eye is now Coralssus-only.

## Install (server)

Copy this folder into `<world>/datapacks/`
(e.g. `servers_folder/magic server/world/datapacks/eyes_of_ender_drops`), then
`reload` (or restart). Loot changes apply to newly generated chests and future
mob kills.
