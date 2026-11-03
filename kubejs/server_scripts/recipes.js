// Recipe changes: additions, edits and removals.

ServerEvents.recipes((event) => {
  // Waystones: no survival crafting path for the Warp Stone, it is a future trader trade.
  event.remove({ output: 'waystones:warp_stone' })
})
