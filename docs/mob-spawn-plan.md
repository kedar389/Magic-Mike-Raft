# Magic Minecraft Server — Mob Spawn Plan (sketch)

**Nothing here is implemented.** This is a proposal for narrowing the mobs that currently spawn in every biome. Current reality is in [`mob-spawns.md`](mob-spawns.md); biome names and tags in [`biomes.md`](biomes.md).

## The problem

| Mod | Mobs | Current rule | Weight |
|---|---|---|---|
| Born in Chaos | 38 | `forge:any` — every Overworld biome, at night | 459 |
| Iron's Spells | Necromancer | `#minecraft:is_overworld` | 18 |
| Sea Dwellers | Depth Mermorph | `forge:any` | 1 |
| Spider Overhaul | Cavern Spider | `#minecraft:is_overworld` | 120 |

A typical Expanded Ecosphere biome carries **515** weight of vanilla monsters (spider 100, skeleton 100, creeper 100, slime 100, zombie 95, enderman 10, witch 5, zombie villager 5). Born in Chaos adds 459 on top of that in *every* biome (41 mobs use `forge:any` in the jar, but Bloody Gadfly, Corpse Fly and Door Knight are switched off by the game-rules datapack — see [`modpack-wiki.md` → Banned Mobs](modpack-wiki.md#banned-mobs)). Every biome reads the same after dark, and the vanilla mobs the rest of the pack is balanced around get crowded out.

The Cavern Spider is the single largest blanket spawn in the pack at w120 — more than vanilla spider itself — and it is not yet deployed (Spider Overhaul is in the client instance but missing from the server `mods/`; see [`mob-spawns.md`](mob-spawns.md)). **Baseline used below is 653** — 515 vanilla + 120 Cavern Spider + 18 Necromancer — i.e. the world as it will be once the jar is on the server. At that baseline Born in Chaos is **41 % of every night-time monster roll, everywhere**.

Three more mobs are nominally "anywhere" but already gated by height or block, and are fine as they are:

| Mob | Mod | Why it is fine |
|---|---|---|
| Bluff | Mowzie's | Underground only, dark, needs no sky |
| Grottol | Mowzie's | Y −65…16, on `#minecraft:base_stone_overworld` |
| Cave Centipede, Murmur | Alex's Mobs | Cave mobs, already exclude ocean / mushroom / deep dark |

## Design rules

1. **Tags, not biome IDs.** Expanded Ecosphere puts its 188 biomes into the vanilla `minecraft:`, `forge:` and `c:` tags, so `#forge:is_swamp` reaches all 30-odd swamps. A literal `minecraft:swamp` reaches one.
2. **One to three biome families per mob**, chosen by the mob's own theme — if it reads as a drowned fisherman it belongs on coasts, not on mountain peaks.
3. **Weights stay untouched.** Narrowing the biome list is enough; re-tune numbers only after playing.
4. **Every family keeps something.** No biome should go back to pure vanilla nights.
5. **Dark forest is the deliberate peak.** It is the pack's haunted biome; it is allowed to be the scariest place at night.

## Resulting balance

Born in Chaos weight per biome family, against the 653 baseline:

| Biome family | Mobs | Weight | Share of night rolls |
|---|---|---|---|
| `#wythers:is_dark_forest` | 14 | 154 | 19 % |
| `#forge:is_plains` | 8 | 119 | 15 % |
| `#minecraft:is_badlands` | 5 | 81 | 11 % |
| `#forge:is_spooky` | 6 | 71 | 10 % |
| `#minecraft:is_beach` | 3 | 63 | 9 % |
| `#forge:is_wasteland` | 7 | 63 | 9 % |
| `#minecraft:is_forest` | 3 | 59 | 8 % |
| `#forge:is_swamp` | 5 | 59 | 8 % |
| `#forge:is_dry/overworld` | 3 | 56 | 8 % |
| `#minecraft:is_taiga` | 4 | 52 | 7 % |
| `#minecraft:is_jungle` | 3 | 42 | 6 % |
| `#minecraft:is_river` | 2 | 37 | 5 % |
| `#minecraft:is_savanna` | 3 | 34 | 5 % |
| `#forge:is_snowy` | 4 | 30 | 4 % |
| `#forge:is_sandy` | 1 | 12 | 2 % |
| `#minecraft:is_hill` | 1 | 8 | 1 % |
| `#minecraft:is_mountain` | 1 | 5 | 1 % |
| `#minecraft:is_deep_ocean` | 1 | 2 | 0 % |

**Tags stack.** A dark forest is also `#minecraft:is_forest` and `#forge:is_spooky`, so it lands near 30 % — intentionally the worst place to be caught out. A plain vanilla `minecraft:plains` sees only the 15 % row. Everything else falls between 1 % and 11 %, against 41 % everywhere today.

---

# Proposed assignments — Born in Chaos

## Village dead — farmland, woods and shores

The ordinary undead, kept where players actually build.

| Mob | w | Proposed biomes |
|---|---|---|
| Decaying Zombie | 30 | `#forge:is_plains`, `#minecraft:is_forest` |
| Zombie Fisherman | 30 | `#minecraft:is_beach`, `#minecraft:is_river`, `#forge:is_swamp` |
| Zombie Lumberjack | 25 | `#minecraft:is_forest`, `#minecraft:is_taiga` |
| Barrel Zombie | 25 | `#forge:is_plains`, `#minecraft:is_beach` |
| Zombie Bruiser | 16 | `#forge:is_plains`, `#minecraft:is_savanna` |
| Missioner | 4 | `#forge:is_plains`, `#minecraft:is_forest` |

## Bone — dry and dead ground

Skeletal mobs move to badlands, deserts and wasteland, where bones belong and vanilla skeletons already dominate.

| Mob | w | Proposed biomes |
|---|---|---|
| Baby Skeleton | 25 | `#forge:is_dry/overworld`, `#minecraft:is_badlands` |
| Decrepit Skeleton | 20 | `#forge:is_dry/overworld`, `#minecraft:is_badlands` |
| Siamese Skeletons | 16 | `#minecraft:is_badlands`, `#forge:is_wasteland` |
| Skeleton Demoman | 12 | `#minecraft:is_badlands`, `#forge:is_sandy` |
| Bonescaller | 11 | `#forge:is_dry/overworld`, `#forge:is_wasteland` |
| Skeleton Thrasher | 8 | `#minecraft:is_badlands`, `#forge:is_wasteland` |
| Supreme Bonescaller | 1 | `#forge:is_wasteland` |

## Vermin — dense, wet and overgrown

| Mob | w | Proposed biomes |
|---|---|---|
| Baby Spider | 16 | `#minecraft:is_jungle`, `#wythers:is_dark_forest` |
| Mother Spider | 15 | `#minecraft:is_jungle`, `#wythers:is_dark_forest` |
| Swarmer | 11 | `#forge:is_swamp`, `#minecraft:is_jungle` |
| Thornshell Crab | 8 | `#minecraft:is_beach`, `#forge:is_swamp` |

## Drowned things

| Mob | w | Proposed biomes |
|---|---|---|
| Corpse Fish | 7 | `#minecraft:is_river`, `#forge:is_swamp` |
| Glutton Fish | 2 | `#minecraft:is_deep_ocean` |

## Hounds — cold and dark

| Mob | w | Proposed biomes |
|---|---|---|
| Dread Hound | 15 | `#minecraft:is_taiga`, `#forge:is_snowy`, `#wythers:is_dark_forest` |
| Dire Hound Leader | 4 | `#minecraft:is_taiga`, `#forge:is_snowy`, `#wythers:is_dark_forest` |

## Pumpkin court — dark forest and farmland

| Mob | w | Proposed biomes |
|---|---|---|
| Mr. Pumpkin | 15 | `#wythers:is_dark_forest`, `#forge:is_plains` |
| Mrs. Pumpkin | 13 | `#wythers:is_dark_forest`, `#forge:is_plains` |
| Señor Pumpkin | 10 | `#wythers:is_dark_forest`, `#minecraft:is_savanna` |
| Pumpkin Bruiser | 10 | `#wythers:is_dark_forest` |
| Sir Pumpkinhead | 4 | `#wythers:is_dark_forest` |

## Spirits — haunted ground

`#forge:is_spooky` is small and precise: dark forest, swamp, and EE's Forbidden, Phantasmal, Deep Dark and Ancient Moss forests plus the two haunted swamps.

| Mob | w | Proposed biomes |
|---|---|---|
| Restless Spirit | 20 | `#forge:is_spooky`, `#wythers:is_dark_forest` |
| Spirit Guide | 16 | `#forge:is_spooky`, `#wythers:is_dark_forest` |
| Dark Vortex | 14 | `#forge:is_wasteland`, `#forge:is_spooky` |
| Spirit Guide Assistant | 9 | `#forge:is_spooky`, `#wythers:is_dark_forest` |
| Spirit of Chaos | 8 | `#forge:is_spooky`, `#forge:is_wasteland` |
| Nightmare Stalker | 4 | `#wythers:is_dark_forest`, `#forge:is_spooky` |
| Lifestealer | 3 | `#wythers:is_dark_forest`, `#forge:is_swamp` |

Seared Spirit (w5) is already limited to `plains` and `sunflower_plains` — widen it to `#forge:is_plains` so it reaches EE grasslands.

## Knights — high and ruined ground

| Mob | w | Proposed biomes |
|---|---|---|
| Fallen Chaos Knight | 5 | `#minecraft:is_mountain`, `#forge:is_wasteland` |

Door Knight would have joined this group, but it is already off by game rule.

## Winter

| Mob | w | Proposed biomes |
|---|---|---|
| Krampus Henchman | 9 | `#forge:is_snowy` |
| Krampus | 2 | `#forge:is_snowy` |

## Leftovers

| Mob | w | Proposed biomes | Why |
|---|---|---|---|
| Phantom Creeper | 8 | `#minecraft:is_hill`, `#forge:is_plains` | Open ground, where a creeper ambush reads |
| Zombie Clown | 8 | `#forge:is_plains`, `#minecraft:is_savanna`, `#minecraft:is_taiga` | Matches the Clown Caravan structure biomes |

Already biome-limited, leave alone: Bone Imp (`basalt_deltas`, `nether_wastes`), Firelight (`crimson_forest`, `nether_wastes`).

---

# Proposed assignments — other mods

| Mob | Mod | Now | Proposed | Why |
|---|---|---|---|---|
| Necromancer | Iron's Spells | `#minecraft:is_overworld`, w18 | `#forge:is_spooky`, `#wythers:is_dark_forest`, `#minecraft:is_badlands` | A necromancer belongs near graves, not in every meadow. w18 in every biome is the second-largest blanket spawn in the pack. |
| Depth Mermorph | Sea Dwellers | any biome, w1 | `#minecraft:is_deep_ocean` | The only Mermorph not already ocean-bound; "Depth" implies deep ocean. |


# Proposed assignments — Spider Overhaul

Do this **when the jar is deployed to the server**, not before.

The mod already ships eleven spiders but gives only six a spawn entry, and plugs the gap with one Cavern Spider at w120 in every Overworld biome. The five unused entities cover exactly the families that are missing. Giving each a home turns "one giant generic spider everywhere" into "one themed spider per biome".

| Spider | Now | Proposed | w |
|---|---|---|---|
| Taiga | no spawn entry | `#minecraft:is_taiga` | 100 |
| Savanna | no spawn entry | `#minecraft:is_savanna` | 100 |
| Ocean | no spawn entry | `#minecraft:is_ocean` | 100 |
| Mushroom | no spawn entry | `#forge:is_mushroom` | 100 |
| Sculk | no spawn entry | `minecraft:deep_dark` | 100 |
| Cavern | `#minecraft:is_overworld`, w120 | `#minecraft:is_overworld`, w40 | 40 |

Birch, Desert, Ice, Jungle and Swamp keep what they have.

Then extend `#spider_overhaul:remove_vanilla_spawn_biomes` with the five new families, so every themed biome swaps its vanilla spider for the local one rather than stacking both.

**Why drop Cavern Spider to 40 rather than restrict its biomes.** It is the underground filler — the replacement for vanilla cave spider — and a biome modifier cannot express "below Y 0". Restricting it to the three cave *biomes* would leave ordinary caves empty, since most underground is still the surface biome. Lowering the weight keeps it everywhere underground without letting it out-spawn vanilla spider on the surface.

Alternative if the mod's own knob is preferred: `spawnWeightMultiplier` in the Spider Overhaul config scales all six weights at once. That thins the themed spiders too, so it is the blunter option.

---

# Implementation

Born in Chaos's spawn files are **plain `forge:add_spawns`** — no custom modifier type — so a datapack that ships the same file path replaces them outright. The mod's own file:

```json
{
  "type": "forge:add_spawns",
  "biomes": { "type": "forge:any" },
  "spawners": {
    "type": "born_in_chaos_v1:mr_pumpkin",
    "weight": 15, "minCount": 1, "maxCount": 3
  }
}
```

The override — only `biomes` changes:

```json
{
  "type": "forge:add_spawns",
  "biomes": ["#wythers:is_dark_forest", "#forge:is_plains"],
  "spawners": {
    "type": "born_in_chaos_v1:mr_pumpkin",
    "weight": 15, "minCount": 1, "maxCount": 3
  }
}
```

Layout, following the other datapacks in `datapacks/`:

```
datapacks/Mob_Spawn_Biomes/
  pack.mcmeta                                    pack_format 15
  README.md
  data/born_in_chaos_v1/forge/biome_modifier/    38 files, <mob>_biome_modifier.json
  data/irons_spellbooks/forge/biome_modifier/    necromancer_spawns.json
  data/seadwellers/forge/biome_modifier/         mermorph_cavern_biome_modifier.json
  data/spider_overhaul/forge/biome_modifier/     add_cavern_spider_spawn.json + 5 new files
  data/spider_overhaul/tags/worldgen/biome/      remove_vanilla_spawn_biomes.json
```

Filenames must match the mod's exactly — see [`mob-spawns.md`](mob-spawns.md) for the source paths. Two wrinkles: Iron's Spells's `necromancer_spawns.json` contains `//` comments in the jar, so the override must be plain JSON; and the five new spiders need *new* files rather than overrides, since the mod ships no spawn entry for them.

## Verifying

1. `/forge tags biome_modifier` after load — the overridden entries should resolve without errors.
2. Stand in a plains biome at night and confirm no pumpkins, spirits or skeleton variants outside their families.
3. `/forge tags block worldgen/biome <tag>` to confirm a tag actually covers the EE biomes you expect before relying on it.

## Open questions

- Is 19 % in dark forest (30 % once `#minecraft:is_forest` and `#forge:is_spooky` stack) the right peak, or should the pumpkin court be rarer still?
- Should the Necromancer keep a low-weight Overworld-wide presence instead of moving entirely to spooky biomes?
- Krampus and the Pumpkin court are seasonal in the mod's own logic — worth checking whether the biome narrowing makes them too rare to ever meet.
- Should the Ocean Spider get `#minecraft:is_ocean` outright, or only the warm oceans, given Aquamirae already owns the frozen ones and Variants & Ventures the lukewarm?
