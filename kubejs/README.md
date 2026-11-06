# KubeJS scripts (repo source of truth)

Canonical source for this pack's KubeJS scripts. KubeJS (`kubejs-forge-2001.6.5`) runs on the servers and the client; the client also needs Rhino.
Radek deploys these to the live instances himself (do not edit the live instances directly).

## Layout

| Folder | Runs on | Reload | Used for |
|---|---|---|---|
| `server_scripts/` | server (incl. client's integrated server) | `/reload` | recipes, tags, loot |
| `client_scripts/` | client only | F3+T | JEI events, tooltips |
| `startup_scripts/` | both, at load | restart | registries |

## Deploy

Copy the folder into each instance's `kubejs/`:

- **Servers:** `servers_folder/magic server/kubejs/` and `servers_folder/testing_server/kubejs/`.
- **Client:** `startup_scripts/` and `client_scripts/` into the client instance's `kubejs/`.

## Current scripts

### Item and recipe changes

| File | Holds |
|---|---|
| `server_scripts/items/recipes.js` | every recipe addition, edit and removal |
| `startup_scripts/removed_items.js` | `global.removedItems`, the list of items removed from the pack |
| `server_scripts/items/removed_items.js` | removes every recipe that outputs a removed item and every tag that lists one |
| `client_scripts/hide_items.js` | hides removed items in JEI |

| Recipe change | Why |
|---|---|
| `waystones:warp_stone` removed | Warp Stone is sold only by traders, see `datapacks/Trades/README.md`. The `waystones:waystone` recipe needs a Warp Stone, so waystones only come from world gen. World-gen/XP tuning lives in `mod_configs_server/waystones-common.toml`. See `docs/modpack-wiki.md` → Waystones. |

### `server_scripts/spawn/spiral_spawn.js`

Places every first-time joiner one node further out along a diamond spiral around `0, 0`, and sets
their spawn point there. Ring size is `|x| + |z|`, growing `step` per player already placed; the
direction rotates `turn` degrees per player.

| Setting | Default | Meaning |
|---|---|---|
| `origin` | `[0, 0]` | x, z the spiral winds out from |
| `step` | `100` | ring blocks added per player already placed |
| `turn` | `45` | degrees rotated per player |
| `clockwise` | `false` | `false` = E → N → W → S on the map |
| `probeStep` / `probeRings` | `16` / `10` | sideways search, up to 160 blocks, when the node is water, lava or has no headroom |

First eight nodes at the defaults:

| n | ring | x | z | n | ring | x | z |
|---|---|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 4 | 400 | 0 | -400 |
| 1 | 100 | 50 | 50 | 5 | 500 | -250 | -250 |
| 2 | 200 | 200 | 0 | 6 | 600 | -600 | 0 |
| 3 | 300 | 150 | -150 | 7 | 700 | -350 | 350 |

State (`spiralIndex`, `spiralPlaced`) lives in `server.persistentData`, written to
`<world>/kubejs_persistent_data.nbt` on level save.
