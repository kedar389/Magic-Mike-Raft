# Attribute_Mod_Labels

Client resource pack. Appends the **source mod** in parentheses to attribute
names, so the Apothic Attributes screen reads `Crit Damage (Apotheosis)`,
`Spell Power (Iron's Spellbooks)`, `Max Health (Minecraft)`, etc.

**Coverage:** 102 attributes across 12 sources: Minecraft, Forge,
Apotheosis, Iron's Spellbooks, Iron's Lib, Obscure API,
Cataclysm, GTBC's SpellLib, GTBC's Geomancy Plus, Additional Entity Attributes,
Reach Entity Attributes, and Botania.

## Why

Apothic Attributes has no built-in way to show which mod an attribute comes
from, the GUI just renders each attribute's localised name. Because several
mods ship attributes with identical names (see `datapacks/Attribute_Merge`),
it's otherwise impossible to tell Apotheosis's `Crit Damage` from Iron's Lib's
`Critical Damage` or Additional Entity Attributes'
`Crit Damage` at a glance.

## How it works

Attribute names come from lang keys. A resource-pack `en_us.json` overrides
those keys with the same text plus a ` (Mod)` suffix. All keys live in one
`assets/minecraft/lang/en_us.json`, Minecraft merges every namespace's lang
into one flat map, so a `minecraft`-namespaced file can override any mod's key.

Key formats are **not** uniform across mods:

| Source | modid | Key format | Example |
|---|---|---|---|
| Minecraft | `minecraft` | `attribute.name.generic.<x>` | `attribute.name.generic.max_health` |
| Forge | `forge` | bare `forge.<x>` | `forge.block_reach` |
| Apotheosis / Apothic Attributes | `attributeslib` | colon-form registry id | `attributeslib:crit_damage` |
| Iron's Spellbooks | `irons_spellbooks` | `attribute.<modid>.<path>` | `attribute.irons_spellbooks.spell_power` |
| Iron's Lib | `irons_lib` | `attribute.name.<modid>.<path>` | `attribute.name.irons_lib.crit_damage` |
| Obscure API (Aquamirae) | `obscure_api` | `attribute.<modid>.<camelCase>` | `attribute.obscure_api.parry` |
| Cataclysm | `cataclysm` | `attribute.<modid>.<path>` | `attribute.cataclysm.nature_heal` |
| GTBC's SpellLib / Geomancy+ | `gtbcs_spell_lib`, `gtbcs_geomancy_plus` | `attribute.<modid>.<path>` | `attribute.gtbcs_spell_lib.spell_crit_chance` |
| Additional Entity Attributes | `additionalentityattributes` | `attribute.name.<modid>.<path>` | `attribute.name.additionalentityattributes.player.dig_speed` |
| Reach Entity Attributes | `reach-entity-attributes` | `attribute.name.generic.<modid>.<x>` | `attribute.name.generic.reach-entity-attributes.reach` |
| Botania | `botania` | `attribute.name.<modid>.<path>` | `attribute.name.botania.pixieSpawnChance` |

Additional Entity Attributes and Reach Entity Attributes are **client-side**
(via Sinytra Connector, bundled with Origins/Apoli), they don't appear in the
server modset, so the scan was run against the client instance's `mods/`.

## Scope / caveats

- **The override is global, not GUI-only.** A resource pack can only rewrite
  the name string, and that same string is reused on **item / curio tooltips**
  (`+7% Crit Chance (Apotheosis)`, `+2 Max Health (Minecraft)`) and in JEI, not
  just the Attributes screen. With every vanilla stat now labelled, that suffix
  shows on essentially all gear tooltips. There is no pure-resourcepack way to
  scope it to the GUI, that needs a client mixin mod. If the tooltip noise is
  too much, switch to the short grey-tag style or drop to a mixin.
- **Merged/hidden attributes are still labelled.** `irons_lib:*`, the
  `obscure_api:*` (criticalDamage / criticalHit / dodge / penetration /
  healingPower) and `cataclysm:additional_critical_damage` are merged into
  Apotheosis and hidden from the GUI by `mod_configs_client/attributeslib.cfg`,
  so their labels won't show in the Attributes screen, only on a source item's
  own tooltip, if one lists them. Their *values* live on the Apotheosis stat.
- **Base strings are verified from the jars**, including vanilla (client
  `en_us.json`) and Forge (`forge-1.20.1-47.4.16` `en_us.json`), no guessed
  names. GTBC's names use the mods' own `displayName`.
- If a new mod adds attributes, re-run the scan (see the scratchpad
  `attr_scan*.txt` procedure) and add the keys here.

## Install (client)

Copy this folder into the client instance's `resourcepacks/`, then enable
**Attribute_Mod_Labels** in Options → Resource Packs and move it **above** the
mod defaults. pack_format 15 (MC 1.20.1). Client-only, no server file needed.
