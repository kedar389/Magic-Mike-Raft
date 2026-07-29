# KubeJS scripts (repo source of truth)

Canonical source for this pack's KubeJS scripts. KubeJS (`kubejs-forge-2001.6.5`) is already installed.
Radek deploys these to the live instances himself (do not edit the live instances directly).

## Layout

| Folder | Runs on | Reload | Used for |
|---|---|---|---|
| `server_scripts/` | server (incl. client's integrated server) | `/reload` | recipes, tags, loot |
| `client_scripts/` | client only | F3+T | JEI events, tooltips |
| `startup_scripts/` | both, at load | restart | registries (none yet) |

## Deploy

Copy the folders into each instance's `kubejs/`:

- **Servers:** `servers_folder/magic server/kubejs/` and `servers_folder/testing_server/kubejs/`
  (server_scripts only strictly needed there; client_scripts are ignored on a dedicated server).
- **Client:** `<Prism>/instances/Client/minecraft/kubejs/` (needs client_scripts for the JEI-hide).

## Current scripts — Iron's Companions compat

- `server_scripts/irons_companions_removals.js` — removes crafting recipes for all nine
  `companions:book_*` magic books (converted → Iron's Spells scrolls, or cut entirely).
- `client_scripts/irons_companions_hide_books.js` — hides those nine books from JEI.

## Current scripts — Waystones balance

- `server_scripts/waystones_balance.js` — disables the `waystones:warp_stone` recipe (Warp Stone
  is a future expensive trader trade). The `waystones:waystone` recipe is left as the mod default,
  which already requires a Warp Stone — so with Warp Stone gone, waystones are only obtainable from
  world gen. World-gen/XP tuning lives in `mod_configs_server/waystones-common.toml`.
  See `docs/modpack-wiki.md` → Waystones.

> These supersede the earlier `datapacks/Irons_Companions_Data/` recipe override (removed). See
> `mod_compats/IronsCompanions/README.md` for the full compat design.
