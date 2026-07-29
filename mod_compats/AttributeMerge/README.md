# Attribute Merge (`attribute_merge`)

Forge 1.20.1-47.4.0 mod, sibling of `IronsCompanions` / `OriginSpellSchools`. Folds **Cataclysm**'s
and **Obscure API**'s duplicate RPG attributes into the canonical **Apothic Attributes** set, so
each stat is one number that stacks in one place.

## Why a mod and not the datapack

Iron's Lib's datapack remap (`datapacks/Attribute_Merge/`) handles the 7 Iron's Lib duplicates, but
it transfers the modifier amount as-is — no rescale, no operation change. These two blockers force a
code hook, both measured from the installed jars:

- **Cataclysm** counts in percent **points**: `The_Annihilator.initAttributes` grants
  `ADDITION 75.0` = "+75% crit damage" (The Immolator `60.0`). Apothic's `crit_damage` is a
  1.5-based multiplier, so transferring `75.0` unchanged is a 76.5× crit. → **×0.01**.
- **Obscure API** (Aquamirae) reads its attributes with a hand-rolled formula
  (`ObscureAPIAttributes.getPercent`) that **sums** ADDITION + MULTIPLY_BASE + MULTIPLY_TOTAL
  amounts with plain addition, while Aquamirae grants them as `MULTIPLY_BASE` (e.g. `0.3`). Under
  vanilla math (what Apothic uses) `MULTIPLY_BASE` on a base-0 attribute contributes **nothing**, so
  a raw transfer would silently delete dodge/crit-chance bonuses. → **force `ADDITION`**.

## Merge table

`MergeRules.TABLE` — source → canonical, scale, forced operation:

| From | To | Scale |
|---|---|---|
| `cataclysm:additional_critical_damage` | `attributeslib:crit_damage` | ×0.01 |
| `obscure_api:critical_damage` | `attributeslib:crit_damage` | ×1 |
| `obscure_api:critical_hit` | `attributeslib:crit_chance` | ×1 |
| `obscure_api:dodge` | `attributeslib:dodge_chance` | ×1 |
| `obscure_api:penetration` | `attributeslib:armor_shred` | ×1 |
| `obscure_api:healing_power` | `attributeslib:healing_received` | ×1 |

Penetration is a *percentage* of armor ignored → `armor_shred` (percentage armor reduction), not
`armor_pierce` (flat armor points).

**Not merged:** Tinkers' Construct (owner's decision); `obscure_api` `parry` / `accuracy` /
`resilience` / `regeneration` / `magic_damage` / `magic_resistance` and `cataclysm` `nature_heal` /
`eat_speed` / `charge_time` — no Apothic counterpart exists.

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

**Cost:** for any item without a duplicate attribute — everything in the pack bar a handful — the
handler does one `isEmpty()` check and a few identity-hash lookups, allocating nothing.

**Not covered:** attributes granted by potions, spells or per-tick code instead of item modifiers,
and curio slots. Nothing in the pack grants these attributes from a curio (Unique Accessories
already grants the Apothic ones directly); if that changes, add a `CurioAttributeModifierEvent`
listener next to the item one.

## Build

No local JDK in this dev environment (see `.wolf/cerebrum.md`), so **this has not been compiled** —
it is scaffolded to the same conventions as the sibling compat mods (Forge 1.20.1-47.4.0, official
mappings, JDK 17 toolchain auto-provisioned by the foojay resolver in `settings.gradle`).

No Gradle wrapper in this folder. Either use a system Gradle, or copy `gradlew`, `gradlew.bat` and
`gradle/` from `../OriginSpellSchools` (the only sibling that has them):

```
cd mod_compats/AttributeMerge
gradle build          # or ./gradlew build after copying the wrapper
```

Output: `build/libs/attribute_merge-0.1.0.jar` → drop into each instance's `mods/`. Forge only, no
compile dependency on the mods it touches: everything is resolved by `ResourceLocation` at runtime,
and each rule is skipped with a debug line if its mod is absent.

## Verifying in game

Startup log, `attribute_merge` logger:

```
Merging 6 duplicate attribute(s) into Apothic Attributes
  cataclysm:additional_critical_damage -> attributeslib:crit_damage (x0.01)
  ...
```

Then hold **The Annihilator** (`ADDITION 75.0`): its tooltip should show **+75% Crit Damage** on the
Apothic stat, the Attributes GUI should read **225%** crit damage (1.5 base + 0.75), and Cataclysm's
own `% Critical Damage` row should stay at its base (it is also hidden via
`mod_configs_client/attributeslib.cfg`).
