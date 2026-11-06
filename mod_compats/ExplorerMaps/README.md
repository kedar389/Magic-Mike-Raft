# ExplorerMaps

Server-only Forge mod. Each cartographer level adds one explorer map to a structure picked at random from that level's tier. Targets and tiers: [`docs/explorer-maps.md`](../../docs/explorer-maps.md).

## Files

| Path | Purpose |
|---|---|
| `mixin/VillagerUpdateTradesMixin` | `@Inject` at TAIL of `Villager#updateTrades` (`m_7604_`, `remap = false`) |
| `MapTrades` | Resolves the config at server start, builds the offer |
| `MapTradeConfig` | Reads `config/explorer_maps.json`, writes the jar default on first start |
| [`configuration_server/mod_configs_server/explorer_maps.json`](../../configuration_server/mod_configs_server/explorer_maps.json) | The config. Packed into the jar as `defaults/explorer_maps.json` by `processResources` |

## Behaviour

- Runs after the vanilla pool roll, so the map is extra: it never pushes a vanilla trade out of the two picks per level.
- Runs once per level reached: on first trade-list creation, then on each level-up.
- Overworld cartographers only. Skipped when the world has `generate-structures=false`.
- `skip_existing_chunks` is `false`: the map points at the nearest structure even in explored land.
- Search runs on the server thread, like vanilla's explorer maps. Async Locator does not cover it.
- Up to `attempts` targets are tried per level. All fail: no map offer at that level, nothing logged above `DEBUG`.
- Offer: `emeralds` + 1 compass for a `filled_map` named `<target> Map`, scale 2, biome preview, one `decoration` marker.

## Search radius

`ChunkGenerator#findNearestMapStructure` counts its radius in placement cells, one cell is `spacing` chunks. The mod reads each structure's live spacing (after Structurify) and sets

`radius = clamp(ceil(max_distance_blocks / 16 / spacing), 1, 32)`

For a multi-structure target the smallest spacing is used. Concentric-ring placements count as spacing 32.

## Config

| Key | Meaning |
|---|---|
| `max_distance_blocks` | Distance cap that sets the radius, `6400` |
| `attempts` | Targets tried per level before giving up, `2` |
| `tiers[].level` | Cartographer level, 1-5 |
| `tiers[].emeralds`, `max_uses`, `xp`, `price_multiplier` | Offer terms |
| `tiers[].decoration` | `MapDecoration.Type` name, lower case |
| `tiers[].targets[].name` | Map name, `<name> Map` |
| `tiers[].targets[].structures` | Structure ids. Several ids make one target, nearest wins |

Read once at server start. Edit the repo file, copy it to `config/`, restart. Unknown ids and structures that do not generate in the Overworld are dropped with a `WARN`.

## Build

```
./gradlew build
```

Output `build/libs/explorer_maps-0.1.0.jar`, into the server's `mods/` only.
