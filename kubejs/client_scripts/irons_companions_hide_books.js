// priority: 0
// Iron's Companions — hide the removed/converted Companions "magic books" from JEI.
//
// They are unobtainable (recipes removed in server_scripts, replaced by Iron's Spells scrolls), so
// hiding them keeps JEI clean. Client-side only — JEIEvents run on the client.

JEIEvents.hideItems((event) => {
  const hiddenBooks = [
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

  hiddenBooks.forEach((id) => event.hide(id))
})
