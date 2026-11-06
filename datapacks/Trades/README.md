# Trades

The single datapack for villager and Wandering Trader trades. Moonlight (server side) reads them.

## Layout

| Path | Trades for |
|---|---|
| `data/minecraft/moonlight/villager_trades/<profession>/<name>.json` | vanilla profession (`farmer`, `cartographer`, …) |
| `data/<modid>/moonlight/villager_trades/<profession>/<name>.json` | a mod's profession |
| `data/minecraft/moonlight/villager_trades/wandering_trader/<name>.json` | Wandering Trader |

The folder name is looked up as a profession id first, then as an entity type id.

## Trade file

```json
{
  "type": "simple",
  "max_trades": 16,
  "offer": { "id": "minecraft:emerald", "Count": 1 },
  "price": { "id": "minecraft:cobblestone", "Count": 22 },
  "level": 1
}
```

Moonlight's own example (`data/minecraft/moonlight/villager_trades/farmer/example.json.disabled` in its jar).

| Field | Meaning |
|---|---|
| `type` | `simple` adds a trade; `remove_all_non_data` drops the profession's original trades at `level`; `no_op` does nothing |
| `level` | villager level the trade unlocks at, 1–5 |
| `price` / `price_secondary` | what the player pays |
| `offer` | what the player gets |
| `max_trades` | uses before restock |
| `xp`, `price_multiplier` | villager XP per trade, demand price scaling |

## Wandering Trader levels

`level` 1 adds to the generic pool, `level` 2 to the rare pool. Each trader rolls 5 generic offers and 1 rare offer, so a rare-pool trade appears on a fraction of traders.

## Current trades

| File | Seller | Level | Price | Offer | Uses |
|---|---|---|---|---|---|
| `data/minecraft/moonlight/villager_trades/wandering_trader/warp_stone.json` | Wandering Trader | 2, rare pool | 22 emerald blocks + 2 emeralds (200 emeralds) | `waystones:warp_stone` | 1 |

- Warp Stone has no crafting recipe (`kubejs/server_scripts/items/recipes.js`), so this trade is its only source. It gates the Waystone recipe.
- Priced in blocks because a trade slot caps at 64 items.
- Cartographer explorer maps are not here: a Moonlight trade cannot run a structure search. See [`mod_compats/ExplorerMaps`](../../mod_compats/ExplorerMaps/README.md).

## Not reachable

| Trader | Why |
|---|---|
| Ribbits | Trades are hard-coded in `com.yungnickyoung.minecraft.ribbits.entity.trade`, not `VillagerTrades`, so Moonlight cannot add to them |
