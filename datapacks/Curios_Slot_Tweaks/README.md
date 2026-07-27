# Curios_Slot_Tweaks

Routes **Cataclysm**'s ring/belt accessories into the **standard** Curios slots
instead of Cataclysm's own separate slot identifiers, so they share the normal
`ring` and `belt` slots players already have (slot "deduping").

## Why

Curios routes an item to a slot via the item tag `curios:<slotid>`. Cataclysm
ships its rings under its own `rings` identifier (a separate slot type) rather
than the standard `ring` slot used by Iron's Spellbooks / Relics / Botania /
Geomancy. That produces duplicate/parallel slot types. This datapack adds the
Cataclysm items to the **standard** slot tags so they occupy the common slots.

## What it changes

Adds items to two Curios item tags (`"replace": false` — additive, does not
wipe other mods' entries):

| Tag (`data/curios/tags/items/…`) | Items added |
|---|---|
| `ring.json`  | `cataclysm:ring_of_grudged` |
| `belt.json`  | `cataclysm:belt_of_beginner`, `cataclysm:belt_of_monstrosity` |

## Notes

- This governs **which slot** an item can go in, not how many slots exist. Slot
  **counts** are set in `config/curios-common.toml` (see the project notes on
  additive slot sizing).
- pack_format 15 (MC 1.20.1).

## Install (server)

Copy this folder into `<world>/datapacks/`
(e.g. `servers_folder/magic server/world/datapacks/Curios_Slot_Tweaks`), then
`reload` (or restart). This datapack is already present in the live main server.
