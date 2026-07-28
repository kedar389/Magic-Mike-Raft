# Attribute_Merge

Collapses **duplicate RPG attributes** so gear that grants a stat all feeds
**one** attribute, stacks there, and shows up in one place (the Apothic
Attributes screen) instead of being split across two parallel stats with the
same name.

## Why

Several mods in the pack each ship their own attribute for the same concept.
The pack currently has these duplicate families (scanned from the jars in
`servers_folder/magic server/mods`):

| Concept | Apothic Attributes | Iron's Lib | Tinkers' Construct | Cataclysm | Obscure API |
|---|---|---|---|---|---|
| Critical damage | `attributeslib:crit_damage` | `irons_lib:crit_damage` | `Critical Damage` (`player.critical_damage`) | `additional_critical_damage` | `criticalDamage` |
| Crit chance | `attributeslib:crit_chance` | — | — | — | `criticalHit` |
| Dodge chance | `attributeslib:dodge_chance` | `irons_lib:dodge_chance` | — | — | `dodge` |
| Arrow damage | `attributeslib:arrow_damage` | `irons_lib:arrow_damage` | — | — | — |
| Armor pierce | `attributeslib:armor_pierce` | `irons_lib:armor_pierce` | — | — | `penetration` |
| Experience gained | `attributeslib:experience_gained` | `irons_lib:experience_gained` | `player.experience_multiplier` | — | — |
| Healing received | `attributeslib:healing_received` | `irons_lib:healing_received` | — | `nature_heal` | `healingPower` |
| Mining speed | `attributeslib:mining_speed` | `irons_lib:mining_speed` | `player.mining_speed_multiplier` | — | — |

**Apothic Attributes is the canonical set** — it owns the Attributes GUI, the
tooltip/description layer, the combat formulas (`config/attributeslib.cfg`) and
covers the widest set of stats.

Iron's Lib's 7 attributes are a straight fork of the Apothic ones (identical
names, identical descriptions, identical value conventions). Iron's Lib even
ships its own Apothic compat that remaps them away — but it is gated on modid
**`apothic_attributes`**, while the installed Apothic Attributes 1.20.1-1.3.7
declares modid **`attributeslib`** (Shadows renamed the mod later). So that
built-in merge never fires on this pack, and both sets stay live.

## How it works

Iron's Lib exposes its remap registry to **datapacks**: a
`SimpleJsonResourceReloadListener` reads `irons_lib_attribute_remap` and syncs
the result to clients
(`io.redspace.ironslib.attribute.AttributeRemapRegistry`,
`AttributeRemapReloadListener`). Each file is one entry:

```json
{ "from": "irons_lib:crit_damage", "to": "attributeslib:crit_damage" }
```

At runtime Iron's Lib intercepts `ItemAttributeModifierEvent` and Curios'
`CurioAttributeModifierEvent` and rewrites any modifier for `from` onto `to`.
Consequences:

- The bonus lands on the Apothic attribute, so **it stacks** with every other
  source of that stat and is visible in the Apothic Attributes GUI.
- The Iron's Lib attribute stops receiving modifiers, so Iron's own handlers
  (crit multiplier, dodge, XP, break speed, healing) contribute nothing — no
  double-dipping. No cancel/zeroing hack needed.
- No value scaling happens: the modifier amount is transferred as-is. That is
  safe here only because the two sets share the same conventions.

## What it changes

`data/attribute_merge/irons_lib_attribute_remap/` — 7 remaps, all
`irons_lib:X` → `attributeslib:X`:

| File | From | To |
|---|---|---|
| `armor_pierce.json` | `irons_lib:armor_pierce` | `attributeslib:armor_pierce` |
| `arrow_damage.json` | `irons_lib:arrow_damage` | `attributeslib:arrow_damage` |
| `crit_damage.json` | `irons_lib:crit_damage` | `attributeslib:crit_damage` |
| `dodge_chance.json` | `irons_lib:dodge_chance` | `attributeslib:dodge_chance` |
| `experience_gained.json` | `irons_lib:experience_gained` | `attributeslib:experience_gained` |
| `healing_received.json` | `irons_lib:healing_received` | `attributeslib:healing_received` |
| `mining_speed.json` | `irons_lib:mining_speed` | `attributeslib:mining_speed` |

Apothic value conventions the merged stats land in (from `ALObjects`):
`crit_damage` base 1.5 (multiplier, 1.0–100.0), `dodge_chance` base 0.0
(fraction, 0.0–1.0), `arrow_damage` / `experience_gained` / `mining_speed` /
`healing_received` base 1.0 (multiplier), `armor_pierce` base 0.0 (flat armor
points).

## Deliberately NOT merged (yet)

- **Tinkers' Construct** (`Critical Damage`, `Experience`, `Mining Speed`),
  **Cataclysm** (`additional_critical_damage`, `nature_heal`) and **Obscure API**
  (`criticalDamage`, `criticalHit`, `dodge`, `penetration`, `healingPower`,
  used by Aquamirae). The remap transfers modifier amounts **without scaling**,
  and these mods' unit conventions are unverified — Obscure API in particular
  looks like whole percent points (`20` = 20%) where Apothic uses fractions
  (`0.2`), which would be a 100× error. Verify base/min/max in the Apothic
  Attributes GUI (or the mods' sources) before adding remaps for them.
- **Jump height** — there is only **one** jump-height attribute in the pack
  (`tconstruct:generic.jump_boost`, "Jump Height"). Artifacts' Bunny Hoppers,
  Unique Accessories' Durable Spring, More Relics' Gravitum Strider and
  Botania's Sojourner's Sash all change jump height in **code**, not through an
  attribute, so there is nothing to remap. Making those feed one attribute
  would need a mod/mixin, not a datapack.
- Attributes granted by **potions, spells or per-tick code** rather than item /
  curio modifiers are not intercepted by the remapper.

## Verifying

Server log on load/`reload` — Iron's Lib logs
`[Attribute Remapper] Unknown attribute remap {} -> {} (missing registry entry)`
if an id is wrong, and `Failed to parse attribute remap {}: {}` on a bad file.
Silence = all 7 applied. In game, equip an Iron's Spells piece that grants e.g.
Crit Damage and check that the bonus appears on **Crit Damage** in the Apothic
Attributes screen, with no second "Critical Damage" line.

## Install (server)

Copy this folder into `<world>/datapacks/`
(e.g. `servers_folder/magic server/world/datapacks/Attribute_Merge`), then
`/reload`. Remaps are synced to clients by Iron's Lib, so no client-side file is
needed. pack_format 15 (MC 1.20.1).
