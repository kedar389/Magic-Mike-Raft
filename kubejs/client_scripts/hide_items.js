// Removed items: hidden in JEI. The list is in startup_scripts/removed_items.js.

JEIEvents.hideItems((event) => {
  global.removedItems.forEach((id) => event.hide(id))
})
