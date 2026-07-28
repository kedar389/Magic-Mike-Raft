// priority: 0
// Iron's Companions — remove the crafting recipes of the removed/converted Companions "magic books".
//
// The 4 converted books (ice_tornado, heal_ring, stone_spikes, naginata) are replaced by real
// Iron's Spells scrolls; the other 5 (ice_shard, fire_mark, brace, magic_ray, black_hole) are cut.
// Removing by output catches the recipe regardless of its path, so we don't need per-mod datapack
// overrides. (Naginata is now the legendary scroll dropped by the Sacred Pontiff.)

ServerEvents.recipes((event) => {
  const removedBooks = [
    'companions:book_ice_shard',
    'companions:book_ice_tornado',
    'companions:book_fire_mark',
    'companions:book_brace',
    'companions:book_heal_ring',
    'companions:book_stone_spikes',
    'companions:book_magic_ray',
    'companions:book_black_hole',
    'companions:book_naginata',
  ]

  removedBooks.forEach((id) => {
    // no-op if the book has no recipe (some are loot/gameplay-only)
    event.remove({ output: id })
  })
})
