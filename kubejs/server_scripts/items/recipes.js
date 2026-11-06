// Recipe changes: additions, edits and removals.

ServerEvents.recipes((event) => {
  // Waystones: no survival crafting path for the Warp Stone, it is sold by traders (datapacks/Trades).
  event.remove({ output: 'waystones:warp_stone' })
})
