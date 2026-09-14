// Spiral spawn. Every first-time joiner is placed one node further out along a
// diamond spiral: the ring |x| + |z| grows by STEP per player already placed,
// and the direction rotates TURN degrees per player. Node 0 is the origin.
//
//   n | ring |    x |     z        n | ring |    x |     z
//   0 |    0 |    0 |     0        4 |  400 |    0 |  -400
//   1 |  100 |   50 |    50        5 |  500 | -250 |  -250
//   2 |  200 |  200 |     0        6 |  600 | -600 |     0
//   3 |  300 |  150 |  -150        7 |  700 | -350 |   350
//
// The index and the set of already-placed players live in the world's KubeJS
// persistent data (<world>/kubejs_persistent_data.nbt), written on level save.

const SPIRAL = {
  origin: [0, 0],
  step: 100,         // blocks of ring (|x| + |z|) added per player already placed
  turn: 45,          // degrees rotated per player
  clockwise: false,  // false = E -> N -> W -> S on the map
  scanTop: 220,      // surface search window
  scanBottom: -60,
  probeStep: 16,     // sideways nudge when the node itself is unusable
  probeRings: 10,    // so at most 160 blocks off the intended node
  unsafe: ['minecraft:water', 'minecraft:lava', 'minecraft:powder_snow'],
}

const PROBE_DIRS = [[1, 0], [1, 1], [0, 1], [-1, 1], [-1, 0], [-1, -1], [0, -1], [1, -1]]

// Bearing 0 = north (-z), 90 = east (+x). The node sits on the diamond
// |x| + |z| = ring, so cardinal nodes land at the full ring distance and
// diagonal nodes at half of it on each axis.
function node(index) {
  const ring = SPIRAL.step * index
  const bearing = (180 + (SPIRAL.clockwise ? 1 : -1) * SPIRAL.turn * index) * Math.PI / 180
  const s = Math.sin(bearing)
  const c = Math.cos(bearing)
  const k = ring / (Math.abs(s) + Math.abs(c))
  return [SPIRAL.origin[0] + Math.round(k * s), SPIRAL.origin[1] + Math.round(-k * c)]
}

// First solid block from the top with two blocks of headroom, or null.
function surfaceY(level, x, z) {
  for (let y = SPIRAL.scanTop; y > SPIRAL.scanBottom; y--) {
    const id = level.getBlock(x, y, z).id
    if (id === 'minecraft:air' || id === 'minecraft:cave_air' || id.endsWith('_leaves')) continue
    if (SPIRAL.unsafe.includes(id)) return null
    if (level.getBlock(x, y + 1, z).id !== 'minecraft:air') return null
    if (level.getBlock(x, y + 2, z).id !== 'minecraft:air') return null
    return y + 1
  }
  return null
}

// Force-loading keeps getBlock off an unloaded chunk; with the area pregenerated
// this is a disk read rather than worldgen.
function groundAt(server, level, x, z) {
  server.runCommandSilent(`forceload add ${x} ${z}`)
  const y = surfaceY(level, x, z)
  server.runCommandSilent(`forceload remove ${x} ${z}`)
  return y
}

function findSpot(server, level, index) {
  const [x0, z0] = node(index)
  const y0 = groundAt(server, level, x0, z0)
  if (y0 !== null) return [x0, y0, z0]

  for (let ring = 1; ring <= SPIRAL.probeRings; ring++) {
    for (const [dx, dz] of PROBE_DIRS) {
      const x = x0 + dx * ring * SPIRAL.probeStep
      const z = z0 + dz * ring * SPIRAL.probeStep
      const y = groundAt(server, level, x, z)
      if (y !== null) return [x, y, z]
    }
  }
  return null
}

PlayerEvents.loggedIn(event => {
  const { player, server } = event
  const data = server.persistentData
  const placed = data.getCompound('spiralPlaced')
  const key = player.stringUuid
  if (placed.contains(key)) return

  const index = data.getInt('spiralIndex')
  const spot = findSpot(server, server.overworld, index)

  // Claim the index before teleporting, so a failed placement cannot hand the
  // same node to the next joiner.
  placed.putBoolean(key, true)
  data.put('spiralPlaced', placed)
  data.putInt('spiralIndex', index + 1)

  if (spot === null) {
    console.warn(`[spiral_spawn] no usable ground near node ${index}, ${player.username} left at world spawn`)
    return
  }

  const [x, y, z] = spot
  server.scheduleInTicks(20, () => {
    server.runCommandSilent(`execute in minecraft:overworld run tp ${player.username} ${x} ${y} ${z}`)
    server.runCommandSilent(`spawnpoint ${player.username} ${x} ${y} ${z}`)
    console.info(`[spiral_spawn] ${player.username} -> node ${index} at ${x} ${y} ${z}`)
  })
})
