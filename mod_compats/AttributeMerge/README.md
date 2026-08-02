# Attribute Merge (`attribute_merge`)

Forge 1.20.1-47.4.0 mod, sibling of `IronsCompanions` / `OriginSpellSchools`. Folds **Cataclysm**'s,
**Obscure API**'s and **Additional Entity Attributes**' duplicate RPG attributes into the canonical
**Apothic Attributes** set, so each stat is one number that stacks in one place.

## Why a mod and not the datapack

Iron's Lib's datapack remap (`datapacks/Attribute_Merge/`) handles the 7 Iron's Lib duplicates, but
it transfers the modifier amount as-is, no rescale, no operation change. These two blockers force a
code hook, both measured from the installed jars. Additional Entity Attributes needs neither and
could in principle use the datapack, but it lives here so the whole merge table is one table:

- **Cataclysm** counts in percent **points**: `The_Annihilator.initAttributes` grants
  `ADDITION 75.0` = "+75% crit damage" (The Immolator `60.0`). Apothic's `crit_damage` is a
  1.5-based multiplier, so transferring `75.0` unchanged is a 76.5× crit. → **×0.01**.
- **Obscure API** (Aquamirae) reads its attributes with a hand-rolled formula
  (`ObscureAPIAttributes.getPercent`) that **sums** ADDITION + MULTIPLY_BASE + MULTIPLY_TOTAL
  amounts with plain addition, while Aquamirae grants them as `MULTIPLY_BASE` (e.g. `0.3`). Under
  vanilla math (what Apothic uses) `MULTIPLY_BASE` on a base-0 attribute contributes **nothing**, so
  a raw transfer would silently delete dodge/crit-chance bonuses. → **force `ADDITION`**.

## Merge table

`MergeRules.TABLE`, source → canonical, scale, forced operation:

| From | To | Scale | Operation |
|---|---|---|---|
| `cataclysm:additional_critical_damage` | `attributeslib:crit_damage` | ×0.01 | forced ADDITION |
| `obscure_api:critical_damage` | `attributeslib:crit_damage` | ×1 | forced ADDITION |
| `obscure_api:critical_hit` | `attributeslib:crit_chance` | ×1 | forced ADDITION |
| `obscure_api:dodge` | `attributeslib:dodge_chance` | ×1 | forced ADDITION |
| `obscure_api:penetration` | `attributeslib:armor_shred` | ×1 | forced ADDITION |
| `obscure_api:healing_power` | `attributeslib:healing_received` | ×1 | forced ADDITION |
| `additionalentityattributes:player.critical_bonus_damage` | `attributeslib:crit_damage` | ×1 | kept |
| `additionalentityattributes:player.dig_speed` | `attributeslib:mining_speed` | ×1 | kept |
| `additionalentityattributes:player.dropped_experience` | `attributeslib:experience_gained` | ×1 | kept |

Penetration is a *percentage* of armor ignored → `armor_shred` (percentage armor reduction), not
`armor_pierce` (flat armor points).

AEA keeps its original operation because it reads its values through vanilla `getValue()`, so there
is no custom formula to work around. Measured base / min / max, and why ×1 is right:

| AEA | Apothic | Same unit because |
|---|---|---|
| `player.critical_bonus_damage` 0.5 / −1 / 1024 | `crit_damage` 1.5 / 1 / 100 | AEA counts the bonus above 1.0, Apothic counts the multiplier, so `1.0 + 0.5` and `1.5` are the same crit |
| `player.dig_speed` 0.0 / 0 / 2048 | `mining_speed` 1.0 / 0 / 10 | AEA applies `speed * (1 + value)`, Apothic multiplies by the value |
| `player.dropped_experience` 1.0 / 0 / 1024 | `experience_gained` 1.0 / 0 / 1000 | both multipliers based at 1.0 |

Because the bases differ, only `ADDITION` transfers exactly. `MULTIPLY_BASE` and `MULTIPLY_TOTAL`
scale off a different base on each side. Nothing grants these three today and Apoli's
`origins:attribute` power emits `ADDITION`, so this is forward-compatibility only.

**Not merged**, no Apothic counterpart exists: `obscure_api` `parry` / `accuracy` / `resilience` /
`regeneration` / `magic_damage` / `magic_resistance`, `cataclysm` `nature_heal` / `eat_speed` /
`charge_time`, and the ten remaining `additionalentityattributes` attributes. Three of those ten
carry real values: `generic.water_speed` from Origins' Merling, and
`player.bonus_rare_loot_rolls` / `player.bonus_loot_count_rolls` from Medieval Origins' Goblin.

## How it works

`ItemAttributeMergeHandler` listens to `ItemAttributeModifierEvent` at `LOW` priority (after Iron's
Lib, which remaps at `HIGHEST`). For each duplicate attribute the item declares, it drains that
attribute (`event.removeAttribute`) and re-adds each modifier on the canonical attribute with the
amount scaled and the operation forced.

Consequences of intercepting at the source rather than fixing up the player afterwards:

- the bonus stacks on the canonical attribute with every other source of that stat;
- the duplicate's owning mod sees its base value → contributes nothing, no double-dipping, and no
  cancelling modifier is needed;
- the **item tooltip** now lists the canonical stat, so tooltip and character sheet agree;
- mobs holding/wearing the gear get the same treatment.

Rewritten modifiers get a deterministic UUID derived from `(original UUID, target attribute id)`.
Determinism matters: vanilla removes an item's modifiers by recomputing the same multimap on
unequip, so the transform must be pure. Including the target id stops the two crit-damage rules
from colliding when both land on `attributeslib:crit_damage`.

**Cost:** for any item without a duplicate attribute, everything in the pack bar a handful, the
handler does one `isEmpty()` check and a few identity-hash lookups, allocating nothing.

**Not covered:** attributes granted by potions, spells or per-tick code instead of item modifiers.

**Curios.** For well-behaved curios, Iron's Lib's remapper already covers `CurioAttributeModifierEvent`,
and Unique Accessories grants the Apothic attributes directly. Cataclysm's curios cannot be reached
by any listener: `CuriosItem.getAttributeModifiers` posts the event but returns its own local
`LinkedHashMultimap`, while `CurioAttributeModifierEvent.getModifiableMap()` lazily copies with
`LinkedHashMultimap.create(originalModifiers)`, so `addModifier` and `removeAttribute` write to a
copy that is discarded. `cataclysm:ring_of_grudged` is affected, see
`docs/technical-notes.md`.

## Build

Forge 1.20.1-47.4.0, official mappings, JDK 17 toolchain. The wrapper is present in this folder, and
JDK 17 is installed at `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot`.

```
cd mod_compats/AttributeMerge
./gradlew build
```

Output: `build/libs/attribute_merge-0.1.0.jar` → drop into each instance's `mods/`. Forge only, no
compile dependency on the mods it touches: everything is resolved by `ResourceLocation` at runtime,
and each rule is skipped with a debug line if its mod is absent.

## Verifying in game

Startup log, `attribute_merge` logger:

```
Merging 9 duplicate attribute(s) into Apothic Attributes
  cataclysm:additional_critical_damage -> attributeslib:crit_damage (x0.01)
  ...
```

Then hold **The Annihilator** (`ADDITION 75.0`): its tooltip should show **+75% Crit Damage** on the
Apothic stat, the Attributes GUI should read **225%** crit damage (1.5 base + 0.75), and Cataclysm's
own `% Critical Damage` row should stay at its base (it is also hidden via
`mod_configs_client/attributeslib.cfg`).
