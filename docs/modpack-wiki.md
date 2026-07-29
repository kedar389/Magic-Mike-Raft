# Magic Minecraft Server — Modpack Wiki

A reference of the deliberate **server-side** config in this modpack: what is set, **where** the setting lives, and (for mobs) **why**. This page is the single index for "is X changed, and how?" — progress/tasks are tracked in [`TODO.MD`](../TODO.MD), not here. For the full list of biomes that generate on this server, see [`biomes.md`](biomes.md).

## How config works here

- **Source of truth = this repo.** Curated server configs live in [`mod_configs_server/`](../mod_configs_server). Only files we've intentionally customized are tracked — not the full config dump.
- **Live installs** in `servers_folder/` are **untracked**. Changes are made in the repo first, then copied to the live server. See [[mirror-instance-changes-to-repo]].
- Different behaviors are controlled by different systems — a mod's own config, vanilla/Forge **game rules** (datapack), or the **Mob Control** mod. Each section notes which.

## Contents

1. [Banned Mobs](#banned-mobs)
2. [Game Rules](#game-rules)
3. [Mob Biome Spawns](#mob-biome-spawns)
4. [Loot Tables](#loot-tables)
5. [Mob Drops](#mob-drops)
6. [Attributes](#attributes)
7. [Waystones](#waystones)

---

## Banned Mobs

### Alex's Mobs

Disabled via each mob's `<mob>SpawnWeight = 0` in [`mod_configs_server/alexsmobs.toml`](../mod_configs_server/alexsmobs.toml) (`0 = disable spawn`). Leafcutter ants are world-gen anthills, disabled via `leafcutterAnthillSpawnChance = 0.0`.

| Mob | Reason | Setting |
|---|---|---|
| Fly | Swarm lag, no value | `flySpawnWeight = 0` |
| Cockroach | Breeds rapidly → entity lag | `cockroachSpawnWeight = 0` |
| Mungus | Spreads mushrooms; clutter | `mungusSpawnWeight = 0` |
| Farseer | World-border ranged pest | `farseerSpawnWeight = 0` |
| Enderiophage | Multiplies & teleports → End chaos | `enderiophageSpawnWeight = 0` |
| Guster | Sandstorm particles + ranged sand attacks | `gusterSpawnWeight = 0` |
| Leafcutter Ant | Documented TPS killer (AlexsMobs #382) | `leafcutterAnthillSpawnChance = 0.0` |
| Anaconda | Large aggressive water snake | `anacondaSpawnWeight = 0` |
| Anteater | Owner preference | `anteaterSpawnWeight = 0` |
| Triops | Overpopulates water | `triopsSpawnWeight = 0` |
| Banana Slug | Clutter / owner preference | `bananaSlugSpawnWeight = 0` |
| Terrapin | Overpopulates coasts | `terrapinSpawnWeight = 0` |
| Flying Fish | Overpopulates water | `flyingFishSpawnWeight = 0` |
| Lobster | Overpopulates coasts | `lobsterSpawnWeight = 0` |
| Comb Jelly | Overpopulates water | `combJellySpawnWeight = 0` |
| Tiger | Owner preference | `tigerSpawnWeight = 0` |
| Gazelle | Owner preference (very high spawn weight) | `gazelleSpawnWeight = 0` |
| Blobfish | Clutter (high spawn weight) | `blobfishSpawnWeight = 0` |
| Underminer | Tunneling illager, destructive | `underminerSpawnWeight = 0` |

_Sources: [AlexsMobs #382 (Leafcutter TPS lag)](https://github.com/Alex-the-666/AlexsMobs/issues/382), [SteamPunk #83 (fish/lobster/terrapin overpopulation)](https://github.com/LunaPixelStudios/SteamPunk/issues/83)._

### Born in Chaos

Born in Chaos has no config file — spawns are disabled via its custom game rules
(set in the [Game Rules](#game-rules) datapack). Natural spawning is turned off for:

| Mob | Reason | Rule (off) |
|---|---|---|
| Corpse Fly | Swarm clutter / lag | `corpseFlySpawn` |
| Bloody Gadfly | Swarm clutter / lag | `bloodyGadflySpawn` |
| Door Knight | Owner preference | `doorKnightSpawn` |

Also off: **Maggots** from corpses on death (`maggotsAppearance`) — a death effect rather than a natural spawn. See [Game Rules → Born in Chaos](#born-in-chaos-1) for the full rule list.

---

## Game Rules

Datapack: [`datapacks/Server_Game_Rules/`](../datapacks/Server_Game_Rules/) (namespace `server_gamerules`), one function file per mod. See [[server-gamerules-datapack-convention]].

### Born in Chaos

`born_in_chaos.mcfunction`:

| Rule | State |
|---|-------|
| `disappearanceofSpiritsUndertheSun` | on    |
| `maggotsAppearance` | off   |
| `corpseFlySpawn` | off   |
| `bloodyGadflySpawn` | off   |
| `doorKnightSpawn` | off   |
| `soulStratificationEffect` | on    |
| `theappearanceoftheNightmareStalker` | on    |
| `generationofInfectedDiamonds` | on    |
| `naughtinessMechanics` | on    |

---

## Mob Biome Spawns

Spawn-rate and biome tuning that isn't a full ban.

| Mob | Change | Setting |
|---|---|---|
| Catfish | Reduced spawn rate (was 4) | `catfishSpawnWeight = 2` in [`mod_configs_server/alexsmobs.toml`](../mod_configs_server/alexsmobs.toml) |

---

## Loot Tables

### End Remastered eyes — vanilla chests

Datapack [`datapacks/eyes_of_ender_drops/`](../datapacks/eyes_of_ender_drops/) (see its README for the full scheme). Eyes injected into vanilla chest loot:

| Eye | Chest | Chance |
|---|---|---|
| `cursed_eye` | Bastion treasure | 50% |
| `black_eye` | Buried treasure | 30% |
| `black_eye` | Shipwreck treasure | 5% |
| `corrupted_eye` | Pillager outpost | 30% |
| `magical_eye` | Woodland mansion | 10% |

Default End Remastered chest sources (mineshaft, desert pyramid, igloo, jungle temple, nether bridge, simple dungeon) are **disabled** — those eyes now come from bosses (below).

---

## Mob Drops

### End Remastered eyes — mobs & bosses

Datapack [`datapacks/eyes_of_ender_drops/`](../datapacks/eyes_of_ender_drops/).

**Boss drops (guaranteed):**

| Eye | Boss | Mod |
|---|---|---|
| `old_eye` | Ancient Remnant | Cataclysm |
| `lost_eye` | The Harbinger | Cataclysm |
| `nether_eye` | Netherite Monstrosity | Cataclysm |
| `exotic_eye` | Coralssus | Cataclysm (only source; recipe removed) |
| `cold_eye` | Lich | Bosses of Mass Destruction (player kill) |
| `rogue_eye` | Sculptor | Mowzie's Mobs (player kill) |
| `undead_soul` (→ craft `undead_eye`) | Maledictus | Cataclysm |

**Injected into vanilla mob drops:**

| Eye / item | Mob | Chance |
|---|---|---|
| `guardian_eye` | Elder Guardian | 30% |
| `magical_eye` | Evoker | 5% |
| `wither_eye` | Wither | 90% ×2 rolls |
| `witch_pupil` (→ craft `witch_eye`) | Witch | 10% |

Crafted eyes: `undead_eye` (shaped) and `witch_eye` (`ender_eye` + `witch_pupil`, shapeless).

---

## Attributes

### Duplicate attributes merged into Apothic Attributes

Datapack [`datapacks/Attribute_Merge/`](../datapacks/Attribute_Merge/).

**Apothic Attributes (`attributeslib:*`) is the canonical attribute set** — it owns the Attributes GUI, the tooltips and the combat formulas in `config/attributeslib.cfg` (currently stock — not tracked in the repo).

Iron's Lib ships its own fork of 7 of those attributes (same names, same descriptions, same value conventions), so each stat existed twice. Iron's Lib's built-in Apothic compat is gated on modid `apothic_attributes`, but the installed Apothic Attributes 1.20.1-1.3.7 declares modid `attributeslib` — so it never fires. Iron's Lib also reads attribute remaps from datapacks (`irons_lib_attribute_remap`), which is what this datapack uses: item/curio modifiers for the `irons_lib` attribute are rewritten onto the Apothic one, so they **stack on a single attribute** and the Iron's Lib handlers stop contributing (no double-dipping).

| Merged stat | From | To |
|---|---|---|
| Critical damage | `irons_lib:crit_damage` | `attributeslib:crit_damage` |
| Dodge chance | `irons_lib:dodge_chance` | `attributeslib:dodge_chance` |
| Arrow damage | `irons_lib:arrow_damage` | `attributeslib:arrow_damage` |
| Armor pierce | `irons_lib:armor_pierce` | `attributeslib:armor_pierce` |
| Experience gained (mob + block XP) | `irons_lib:experience_gained` | `attributeslib:experience_gained` |
| Healing received | `irons_lib:healing_received` | `attributeslib:healing_received` |
| Mining speed | `irons_lib:mining_speed` | `attributeslib:mining_speed` |

### Cataclysm + Obscure API merged by the `attribute_merge` mod

Mod [`mod_compats/AttributeMerge/`](../mod_compats/AttributeMerge/) (`attribute_merge`, Forge 1.20.1-47.4.0). **Needs building — no JDK in the dev environment, so it ships unbuilt.**

These two families **cannot** use the datapack remap above, because the remap transfers the modifier amount as-is and neither shares Apothic's conventions:

- **Cataclysm** counts in percent **points** — The Annihilator grants `ADDITION 75.0` meaning "+75% crit damage", which remapped onto Apothic's 1.5-based multiplier would be a 76.5× crit. Needs a ×0.01 rescale.
- **Obscure API** reads its attributes by **summing** the amounts of ADDITION + MULTIPLY_BASE + MULTIPLY_TOTAL modifiers additively (`ObscureAPIAttributes.getPercent`), and Aquamirae grants them as `MULTIPLY_BASE` (e.g. `0.3`). Under the vanilla math Apothic uses, `MULTIPLY_BASE` on a base-0 attribute (dodge, crit chance) contributes **nothing** — a raw remap would silently delete those bonuses.

So the mod listens to `ItemAttributeModifierEvent` (at `LOW` priority, after Iron's Lib) and rewrites the modifier **as the item declares it** — draining the duplicate attribute and re-adding each modifier on the Apothic one with the amount scaled and the operation forced to `ADDITION`. Nothing ticks and there is no per-player state: the bonus never reaches the duplicate attribute, so its owning mod contributes nothing without needing a cancelling modifier, the item's own tooltip shows the canonical stat, and mobs wearing the gear are covered too.

| Merged stat | From | To | Scale |
|---|---|---|---|
| Critical damage (percent points) | `cataclysm:additional_critical_damage` | `attributeslib:crit_damage` | ×0.01 |
| Critical damage | `obscure_api:critical_damage` | `attributeslib:crit_damage` | ×1 |
| Crit chance | `obscure_api:critical_hit` | `attributeslib:crit_chance` | ×1 |
| Dodge chance | `obscure_api:dodge` | `attributeslib:dodge_chance` | ×1 |
| Armor penetration (% of armor) | `obscure_api:penetration` | `attributeslib:armor_shred` | ×1 |
| Healing received | `obscure_api:healing_power` | `attributeslib:healing_received` | ×1 |

Obscure's Penetration is a *percentage* of armor ignored, so its twin is `armor_shred` (percentage armor reduction), not `armor_pierce` (flat armor points).

Because the rewrite happens at the item level, an Aquamirae weapon's tooltip lists the **Apothic** stat directly — tooltip and character sheet agree.

### Hiding the duplicate rows from the Attributes GUI

Client config [`mod_configs_client/attributeslib.cfg`](../mod_configs_client/attributeslib.cfg) → `general` → `Hidden Attributes`.

Merging moves the **values**; it does not unregister the attributes, so the duplicate rows still appear in the Apothic Attributes screen (reading their base value) until they are hidden. Added to the hidden list: all 7 `irons_lib:*`, the 5 merged `obscure_api:*` (`critical_damage`, `critical_hit`, `dodge`, `penetration`, `healing_power`) and `cataclysm:additional_critical_damage`. Left visible: `tconstruct:*` (not merged), the unmerged Obscure stats (`parry`, `accuracy`, `resilience`, `regeneration`, `magic_damage`, `magic_resistance`) and `gtbcs_spell_lib` spell-crit stats. This is a **client-side** file — the GUI is client-only.

### Every "critical damage" attribute in the pack

Registered base / min / max, read out of the installed jars. Six rows show in the Attributes GUI until the hidden list below is applied:

| GUI label | Attribute | Base | Min | Max | Status |
|---|---|---|---|---|---|
| Crit Damage | `attributeslib:crit_damage` | 1.5 | 1.0 | 100 | **canonical** — fed by Unique Accessories, the datapack remap and the mod |
| Critical Damage | `irons_lib:crit_damage` | 1.0 | 0.0 | 1024 | remapped by datapack → hidden |
| Critical Damage | `obscure_api:critical_damage` | 2.0 | 0.0 | 100 | merged by the mod (Aquamirae) → hidden |
| % Critical Damage | `cataclysm:additional_critical_damage` | 0.0 | −512 | 512 | merged by the mod ×0.01 → hidden |
| Critical Damage | `tconstruct:player.critical_damage` | 1.5 | 0.0 | 100 | **left as-is** (Tinkers not merged) |
| Spell Critical Damage | `gtbcs_spell_lib:spell_crit_damage` | 1.0 | 1.0 | 10 | different stat (spell crits only) — left as-is |

Attribute registrars pack-wide (all of them): Apothic Attributes, Iron's Lib, Iron's Spellbooks, Obscure API, Cataclysm, Tinkers' Construct, Geomancy spell lib, Geomancy Plus, Caelus. No other mod registers an attribute.

### Dodge chance — where it comes from

| Source | Mechanism | Amount |
|---|---|---|
| **Unique Accessories** — Black Belt (belt) | `attributeslib:dodge_chance`, `ADDITION` | `blackBeltDodgeChance = 0.15` → +15%; also grants temporary attack-damage + move-speed buffs on a successful dodge |
| **Unique Accessories** — Championship Belt (belt) | `attributeslib:dodge_chance` via curio tick | `championshipBeltDodgeChance = 0.05` → +5% (plus armor) |
| **Unique Accessories** — Hero Emblem (charm) | `attributeslib:dodge_chance`, only at 1 HP | `heroEmblem1HpDodgeChance = 0.2` → +20% |
| **Medieval Origins** — Moon Elf, "Nimble" power | **NOT an attribute** — `origins:modify_damage_taken` (`multiply_total_multiplicative −2.0`) gated on predicate `medievalorigins:dodge_chance` = `random_chance 0.2` | flat 20% chance to negate **any** incoming damage, with end-rod particles + enderman sound |

Amounts are configurable in `config/uniqueaccessories-common.toml` (present on both server and client; stock values above).

Two consequences worth knowing:

- The Moon Elf dodge is invisible to the Attributes GUI and rolls **independently** of the Apothic stat, so Black Belt + Moon Elf ≈ 32% effective (`1 − 0.85 × 0.8`), not 35%. It could be rewritten as an `origins:attribute` power in a datapack to fold into `attributeslib:dodge_chance` — but note Apothic's dodge only applies to **melee** damage from entities in range, while the power negates any damage type, so converting it would be a nerf, not just a display change.
- `irons_lib:dodge_chance` and `obscure_api:dodge` are registered and their handlers work, but **nothing in the pack grants them** (Aquamirae grants accuracy / crit damage / crit hit / penetration, not dodge). They were empty rows → hidden.

### Which mods actually feed these stats

| Attribute family | Granted by | Notes |
|---|---|---|
| Apothic `attributeslib:*` (canonical) | **Unique Accessories** | armor pierce, arrow damage, arrow velocity, crit chance, crit damage, dodge chance, draw speed, mining speed — already canonical, nothing to merge |
| Obscure API `obscure_api:*` | **Aquamirae** (accuracy, critical damage, critical hit, penetration), **Aquamirae Delight** (critical hit) | vanilla item modifiers, `MULTIPLY_BASE` → merged by the mod |
| Cataclysm `cataclysm:*` | **Cataclysm itself** — The Annihilator (`ADDITION 75.0`) and The Immolator (`ADDITION 60.0`) for crit damage; `nature_heal` via its armor tiers + food mixin | → crit damage merged by the mod, `nature_heal` left alone |
| Tinkers `tconstruct:*` | **Tinkers itself** (tool/armor modifiers: `dragonborn`, `experienced`, `haste`) | not merged, by choice |
| Iron's Lib `irons_lib:*` | **nothing** | No installed mod grants them. Iron's Spellbooks' only `irons_lib:` references are a particle texture and a worldgen processor. The rows were pure noise → hidden; the datapack remap stays as a safety net |

**Deliberately left alone:** **Tinkers' Construct** (`Critical Damage`, `Experience`, `Mining Speed` — owner's call to skip Tinkers integration); `obscure_api` `parry` / `accuracy` / `resilience` / `regeneration` / `magic_damage` / `magic_resistance` and `cataclysm` `nature_heal` / `eat_speed` / `charge_time`, which have no Apothic counterpart to merge into. **Jump height** is not duplicated at all — `tconstruct:generic.jump_boost` is the only jump-height attribute; Artifacts / Unique Accessories / More Relics / Botania change jump height in code. Details in the datapack's [README](../datapacks/Attribute_Merge/README.md).

### Showing the source mod in the Attributes GUI

Client resource pack [`resourcepacks/Attribute_Mod_Labels/`](../resourcepacks/Attribute_Mod_Labels/).

Apothic Attributes has no built-in way to show which mod an attribute belongs to. Because merging leaves several stats with the same name, this pack appends the source mod in parentheses to each attribute's display name — `Crit Damage (Apotheosis)`, `Spell Power (Iron's Spellbooks)`, `Parry (Obscure API)`, `Max Health (Minecraft)`, etc. It's a lang-key override (`assets/minecraft/lang/en_us.json`), one flat file overriding every namespace's keys.

**Full coverage: 119 attributes across 13 sources** — Minecraft, Forge, Apotheosis (`attributeslib`), Iron's Spellbooks, Iron's Lib, Tinkers' Construct, Obscure API, Cataclysm, GTBC's SpellLib, GTBC's Geomancy Plus, Additional Entity Attributes, Reach Entity Attributes, Botania. Additional Entity Attributes and Reach Entity Attributes are client-only (Sinytra Connector, bundled with Origins), so the scan was run against the **client** instance's `mods/`, not the server modset. All base strings verified from the jars (vanilla from the client jar, Forge from `forge-1.20.1-47.4.16`).

Two things to know:

- **The override is global, not GUI-only** — the same name string is reused on item/curio tooltips and in JEI (`+7% Crit Chance (Apotheosis)`, `+2 Max Health (Minecraft)`), not just the Attributes screen. With vanilla stats now labelled too, that suffix appears on essentially all gear tooltips. Scoping it to the GUI alone would need a client mixin.
- **Merged/hidden attributes are still labelled** but won't show in the GUI — `irons_lib:*`, the merged `obscure_api:*` and `cataclysm:additional_critical_damage` are in the Hidden Attributes list; their labels only surface if a source item tooltip lists them.

Client-only — no server file. Copy into the client instance's `resourcepacks/` and enable it above the mod defaults.

---

## Waystones

Waystones are a **discovered, XP-gated** travel network — not a cheap early convenience.
Two systems: mod config for world-gen + XP, and KubeJS for the recipes.

**World-gen & teleport cost** — [`mod_configs_server/waystones-common.toml`](../mod_configs_server/waystones-common.toml):

| Setting | Value | Effect |
|---|---|---|
| `worldGen.frequency` | `31` | ~500 blocks between world-gen waystones (was 25 ≈ 400) — sparse |
| `restrictions.generatedWaystonesUnbreakable` | `true` | World-gen waystones cannot be mined |
| `xpCost.minimumBaseXpCost` | `1.0` | Every warp costs ≥ 1 level |
| `xpCost.maximumBaseXpCost` | `5.0` | Long-haul cap of 5 levels (was 3) |
| `xpCost.blocksPerXpLevel` | `1000` | Cost scales 1 level per 1000 blocks between the min/max |
| `xpCost.*XpCostMultiplier` (waystone, warpStone, sharestone, portstone, warpPlate, global, inventoryButton) | `1.0` | All warp methods now pay the base cost (were `0.0` = free) |

Because Minecraft XP levels get progressively more expensive, a flat few-level cost stings early game but is trivial by endgame — **usable at endgame, harder early**. All values are tunable.

**Recipes** — [`kubejs/server_scripts/waystones_balance.js`](../kubejs/server_scripts/waystones_balance.js):

| Item | Change |
|---|---|
| `waystones:warp_stone` | Recipe **removed** — no survival craft. Intended as a future expensive trader trade (see [`TODO.MD`](../TODO.MD)); unobtainable for now. |
| `waystones:waystone` | **Unchanged** — the mod's default recipe already requires a Warp Stone (3× stone bricks + 4× obsidian + 1× Warp Stone). Since Warp Stone is unobtainable, the waystone is uncraftable too, so waystones are currently **only** available from world gen. |

---

## Conventions for this wiki

Add each server-side change to the relevant category: for mobs, include **why**; for game rules, just the **rule name + state** (on/off/value). Record current state only — no task/progress notes (that's [`TODO.MD`](../TODO.MD)). Keep the source-of-truth file in the repo. Client-side/visual tweaks do **not** belong here.
