// Removed items: no recipe makes them and no tag lists them. The list is in startup_scripts/removed_items.js.

ServerEvents.recipes((event) => {
  global.removedItems.forEach((id) => event.remove({ output: id }))
})

ServerEvents.tags('item', (event) => {
  global.removedItems.forEach((id) => event.removeAllTagsFrom(id))
})
