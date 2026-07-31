// priority: 0
// Waystones balance, recipe gating.
//
// Design (see docs/modpack-wiki.md → Waystones):
//   * Warp Stone crafting is DISABLED. It is meant to be an expensive trader trade later
//     (see TODO "Update loot tables for traders..."). For now it is unobtainable in survival.
//   * The Waystone recipe is left as the mod default, which ALREADY requires a Warp Stone
//     (pattern " S / SWS / OOO", stone bricks + obsidian + warp stone). So with Warp Stone
//     unobtainable, waystones can't be crafted either; the only source is world generation
//     (sparse + unbreakable, see waystones-common.toml).
//
// Removing by output catches the recipe regardless of its path (same approach as
// irons_companions_removals.js), so we don't need per-mod datapack overrides.

ServerEvents.recipes((event) => {
  // Kill the Warp Stone recipe entirely, no survival crafting path for now.
  event.remove({ output: 'waystones:warp_stone' })
})
