// Waystones balance, recipe gating.



ServerEvents.recipes((event) => {
  // Kill the Warp Stone recipe entirely, no survival crafting path for now.
  event.remove({ output: 'waystones:warp_stone' })
})
