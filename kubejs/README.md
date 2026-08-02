# KubeJS scripts (repo source of truth)

Canonical source for this pack's KubeJS scripts. KubeJS (`kubejs-forge-2001.6.5`) is already installed.
Radek deploys these to the live instances himself (do not edit the live instances directly).

## Layout

| Folder | Runs on | Reload | Used for |
|---|---|---|---|
| `server_scripts/` | server (incl. client's integrated server) | `/reload` | recipes, tags, loot |
| `client_scripts/` | client only | F3+T | JEI events, tooltips |
| `startup_scripts/` | both, at load | restart | registries |

Only `server_scripts/` currently holds anything.

## Deploy

Copy the folder into each instance's `kubejs/`:

- **Servers:** `servers_folder/magic server/kubejs/` and `servers_folder/testing_server/kubejs/`.
- **Client:** nothing to deploy while there are no `client_scripts/`.

## Current scripts, Waystones balance

- `server_scripts/waystones_balance.js`, disables the `waystones:warp_stone` recipe (Warp Stone
  is a future expensive trader trade). The `waystones:waystone` recipe is left as the mod default,
  which already requires a Warp Stone, so with Warp Stone gone, waystones are only obtainable from
  world gen. World-gen/XP tuning lives in `mod_configs_server/waystones-common.toml`.
  See `docs/modpack-wiki.md` → Waystones.
