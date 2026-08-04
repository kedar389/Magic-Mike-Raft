# Attribute Merge (`attribute_merge`)

Forge 1.20.1-47.4.0 mod, sibling of `IronsCompanions` / `OriginSpellSchools`. Folds **Cataclysm**'s,
**Obscure API**'s and **Additional Entity Attributes**' duplicate RPG attributes into the canonical
**Apothic Attributes** set, so each stat is one number that stacks in one place. It also gives
**Epic Knights**' armour piercing a real attribute, which is the opposite problem: that mod carries
the stat as a plain field and registers no attribute at all.

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
and Unique Accessories grants the Apothic attributes directly. Cataclysm's curios need the mixin
below.

## Epic Knights armour piercing

Not a merge: Epic Knights registers no attribute, so there is no source to drain.
`WeaponType.armorPiercing` is an `int` percent baked into the weapon type at registration and
editable in `config/epicknights/weapons.json5`. Ten melee weapons carry a nonzero value:
`lucerneHammer` 20, `stiletto` 15, `heavyWarHammer` 15, `ahlspiess` 12, `giantLance` 10,
`zweihander` 4, `flameBladedSword` 4, `blacksmithHammer` 4, `estoc` 3, `concaveEdgedHalberd` 2. The
other 18 sit at 0.

`item/EpicKnightsPiercing` walks the item registry once at common setup, keeps the weapons that carry
piercing in an identity map, and adds `attributeslib:armor_shred` `ADDITION armorPiercing / 100` on
`MAINHAND`. Mainhand only: Epic Knights reads the attacker's main hand, and `armor_shred` is an
attacker-side attribute. Resolved by name string, so there is still no compile dependency, and the
whole thing is inert when Epic Knights is absent.

**`disableArmorPiercing: true` in `config/epicknights/general.json5` is required, on both sides**
(`mod_configs_server/epicknights/` and `mod_configs_client/epicknights/`). Without it the stat
applies twice: `CommonEvents.onLivingHurt` always returns `EventResult.pass()` and discards
`MedievalWeaponItem.onHurtEntity`'s return value, so the mod's piercing is a *second* damage instance
on top of the normal hit rather than a replacement for it. The flag makes `getArmorPiercing()` return
0, which fails the `!= 0` gate in `onHurtEntity`. Server side stops the damage, client side stops
Epic Knights' own `armorpiercing` tooltip line, which is gated on the same getter, so this modifier's
line replaces it instead of sitting beside it.

Two consequences of that flag being the off-switch:

- the amount is read from the private `armorPiercing` **field**, never through `getArmorPiercing()`,
  which returns 0 once the flag is set and would leave every weapon with no modifier. It is re-read
  per call rather than baked into the map, so an in-game config save still changes the stat; vanilla
  removes modifiers by UUID, so a value that changes under an equipped item unequips cleanly;
- `CombatHelper.getArmorPiercingFactor` returns `p / (1 - p)` for an Epic Knights weapon and `1.0`
  for anything else, and scales shield durability damage in `MedievalShieldItem.onBlocked`. Those
  weapons stop wearing down Epic Knights shields, which is how the 18 at `armorPiercing: 0` already
  behave.

`armor_shred` and not `armor_pierce` because the field is a percentage, and `armor_pierce` is flat
armour points. Epic Knights deals `p × blocked` extra damage and lets armour reduce it again, a net
multiplier of `(1-r)(1 + p×r)` on the raw hit, while `ALCombatRules.getDamageAfterArmor` does
`armor -= armor × shred × (1 - min(toughness × 0.02, 0.6))`. Under Apothic's default `a / (a + armor)`
formula with a = 10 and no toughness:

| Armour | Piercing | Epic Knights | `armor_shred` |
|---|---|---|---|
| 20 | 15% | 0.367 | 0.370 |
| 20 | 20% | 0.378 | 0.385 |
| 40 | 15% | 0.224 | 0.227 |
| 40 | 20% | 0.232 | 0.238 |

Shred is a hair stronger at zero toughness and weaker against high-toughness targets, which Epic
Knights ignores entirely.

## The Cataclysm curio mixin

`CuriosItem.getAttributeModifiers` cannot be influenced by any event listener. It posts
`CurioAttributeModifierEvent` and then returns **its own local `LinkedHashMultimap`**, not
`event.getModifiers()`, while `CurioAttributeModifierEvent.getModifiableMap()` lazily copies with
`LinkedHashMultimap.create(originalModifiers)`. So `addModifier` and `removeAttribute` both write to
a copy that is thrown away.

Cataclysm also gates a curio's attributes on the slot it sits in:

```java
if (slotContext.identifier().equals(this.attributeSlot) && this.attributes != null) { ... }
```

`attributeSlot` is baked in at registration. `ring_of_grudged` declares `"rings"`, but the pack sets
`id=rings;size=0` in `curios-common.toml` and routes the item into the standard `ring` slot so every
ring shares one slot, so the gate fails and the ring grants nothing at all.

`mixin/CataclysmCuriosMixin.java` fixes both, and both are needed: fixing only the gate would give
the ring a raw `cataclysm:additional_critical_damage`, which Cataclysm's own handler applies as a
working +10% crit while the row stays hidden and the value never reaches `attributeslib:crit_damage`,
an invisible buff.

| Injection | Effect |
|---|---|
| `@Redirect` on the method's only `String.equals` | `ring` satisfies `rings`, nothing else |
| `@Inject` at `RETURN`, mutating `cir.getReturnValue()` | `MergeEngine.mergeInPlace` on the map actually returned |

Verified against the installed jar: the method has exactly one `String.equals` (offset 13, the gate)
and exactly one `areturn` (offset 109), so neither injection is ambiguous. Both use `require = 1`, so
a Cataclysm update that moves either one fails at load rather than silently reverting the ring.

Still no compile dependencies. The mixin targets Cataclysm by name string
(`@Mixin(targets = "com.github.L_Ender.cataclysm.items.CuriosItem.CuriosItem")`) and captures no
target method arguments, which keeps Curios' `SlotContext` off the classpath too. Every annotation
sets `remap = false`, so no refmap is generated or needed. `AttributeMergeMixinPlugin` skips the
whole config when Cataclysm is absent, checking `LoadingModList` because mixin configs are queried
during class loading, before `ModList` exists.

The config is registered by the **`MixinConfigs` attribute in `META-INF/MANIFEST.MF`**, set from
`build.gradle`. Forge 1.20.1 accepts a `[[mixins]]` block in `mods.toml` and never hands it to
Mixin: the config simply never appears in Mixin's `Preparing <config>` list, with no warning and no
error, and `require = 1` cannot fire for a config that was never loaded.

## Curio tooltip formatting

Curios renders its own "when worn" attribute lines in
`ClientEventHandler.onTooltip`, using vanilla's `ItemStack.ATTRIBUTE_MODIFIER_FORMAT` and vanilla's
`attribute.modifier.*` keys. It never consults `IFormattableAttribute`, so an Apothic attribute on a
curio prints its raw value (`+0.1 Crit Damage`) while the same attribute on a weapon prints through
`PercentBasedAttribute` (`+75%`). Apothic's `CuriosClientCompat` only adds the Attributes button to
the Curios screen.

`client/CurioTooltipFormatHandler` runs last on `ItemTooltipEvent` and re-renders those lines through
Apothic's own static `IFormattableAttribute.toComponent`. Matching on the vanilla translation keys
selects exactly the unformatted lines: Apothic rewrites equipment lines during tooltip construction,
before any event fires, so a vanilla-keyed attribute line at `LOWEST` priority was added by a
listener.

This covers every curio from every mod, and every attribute rather than only `attributeslib` ones:
Apothic's `AttributeMixin` injects `IFormattableAttribute` into vanilla's `Attribute` base class, so
every attribute implements it and picks up either the flat default or its own override
(`PercentBasedAttribute`, `BooleanAttribute`). A curio granting `generic.armor` renders exactly as
armour with the same modifier does. Client only, and inert without Apothic Attributes.

The amount is recovered by parsing the number vanilla already formatted, which is what lets the line
be rebuilt without a compile dependency on Curios. Vanilla's `#.##` rounding is therefore inherited:
an amount of 0.075 has already been printed as `0.08` and reformats to `8%`.

Apothic is reached through `client/ApothicFormatter`, a `MethodHandle` on that one static method,
resolved once and disabled after a single warning if it ever throws.

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
Granting armorPiercing as attributeslib:armor_shred on 10 Epic Knights weapon(s)
```

The mixin config must be registered, which `debug.log` states outright:

```
Preparing attribute_merge.mixins.json (1)
Mixing CataclysmCuriosMixin from attribute_merge.mixins.json into com.github.L_Ender...CuriosItem
```

Both lines missing means the config never reached Mixin, and every injection error it would have
raised is missing with it. If Cataclysm is absent, the plugin skips the config silently.

Equip **Ring of the Grudge** in a `ring` slot: Crit Damage should go from 1.5 to 1.6, with no
`% Critical Damage` row, and its tooltip should read **+10% Crit Damage** rather than `+0.1`. A
curio's tooltip is not evidence the attribute applies: Curios renders a line for every slot the item
is *tagged* for, and `ring_of_grudged` is still in Cataclysm's `rings` tag, where the gate passes
even when the item can only be worn in `ring`. Confirm on the Attributes screen.

Hold a **Lucerne Hammer**: its tooltip should show **+20% Armor Shred** and no `20% armor piercing`
line, and the Attributes screen should read 20% Armor Shred. If the Epic Knights line is still there,
that instance's `general.json5` was not updated and the stat is being applied twice.

Then hold **The Annihilator** (`ADDITION 75.0`): its tooltip should show **+75% Crit Damage** on the
Apothic stat, the Attributes GUI should read **225%** crit damage (1.5 base + 0.75), and Cataclysm's
own `% Critical Damage` row should stay at its base (it is also hidden via
`mod_configs_client/attributeslib.cfg`).
