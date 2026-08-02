# TODO

## Mods to add
- [] add lets do bakery, needs checking the farm and charm that is being pulled ind
- [X] add ascended arcana
- [x] Add feur extension jungle
- [x] Add colorful hearts
- [ ] Add **[Async Locator](https://modrinth.com/mod/async-locator)** (server-side only). `/locate` and villager explorer map trades share one code path, `ServerLevel.findNearestMapStructure(tag, pos, radius, skipExistingChunks)`; the trade version runs a **100-chunk** radius search with `skipExistingChunks=true` on the main thread whenever a villager levels up or restocks, a real stutter source on 172 mods. Covers the `/locate` command, villager map trade offers, dolphin treasure finding and chest exploration maps. Caveat: it patches **vanilla paths only** ("other mods may need to implement usage of this mod themselves"), so the datapack tag route under Structures benefits but MoreJS-authored trades likely do not, which is an argument for the datapack route

## Modpack Management
- [ ] Create a script that copies config and datapacks over to the server

## Balance
- [x] Deduplicate curio slots
- [x] Merge duplicate attributes, Iron's Lib set (crit damage, dodge chance, arrow damage, armor pierce, experience gained, healing received, mining speed) folded into Apothic Attributes via [`datapacks/Attribute_Merge/`](datapacks/Attribute_Merge/)
- [x] Merge Cataclysm + Obscure API/Aquamirae duplicate attributes (crit damage, crit chance, dodge, penetration→armor shred, healing power) into Apothic, written as the [`attribute_merge`](mod_compats/AttributeMerge/) Forge mod (rewrites the modifier on `ItemAttributeModifierEvent`, nothing ticks). They can't use the datapack remap: Cataclysm is percent points (needs ×0.01), Obscure delivers MULTIPLY_BASE but reads it additively (needs forced ADDITION). Replaced the earlier KubeJS polling bridge
- [ ] **Build `mod_compats/AttributeMerge/`** (`gradle build`, no JDK in this dev env, so it ships uncompiled) and drop `attribute_merge-0.1.0.jar` into each instance's `mods/`
- [x] Hide the duplicate attribute rows from the Apothic Attributes GUI (`Hidden Attributes` in [`mod_configs_client/attributeslib.cfg`](mod_configs_client/attributeslib.cfg)), merging moves values but leaves the rows registered; needs the CLIENT config redeployed
- [ ] In-game sanity check after building: log shows `Merging 6 duplicate attribute(s)`, and The Annihilator (`ADDITION 75.0`) reads **+75% Crit Damage** on its tooltip with the Apothic stat at **225%** (1.5 base + 0.75), appearing **once**

## Biomes
- [ ] Use the new woods from Cluttered and add them to the biomes in William Wythers
- [ ] Change where flowers spawn
- [ ] Make desert biomes bigger by 0.5
- [ ] Make other biomes bigger by 0.3

## Structures
- [ ] Make structures spawn on flat terrain
- [ ] **Buyable explorer maps that lead to modded structures.** Villager trades are hardcoded in `VillagerTrades.TRADES` (static map, class-init), so there is no datapack registry for them on 1.20.1, and KubeJS `2001.6.5` ships only `VillagerProfessionBuilder`/`VillagerTypeBuilder`, no trade event. Three routes:
  - **Datapack, retarget the existing cartographer trades** (preferred): the two vanilla map trades resolve through structure tags, `data/minecraft/tags/worldgen/structure/on_ocean_explorer_maps.json` (vanilla `["minecraft:monument"]`) and `on_woodland_explorer_maps.json` (vanilla `["minecraft:mansion"]`). Add structure IDs with `"replace": false` and the trade retargets. Limits: only 2 trade slots total, pooled nearest-wins so you can't pick which structure, and the map keeps the vanilla name/icon
  - **Datapack, `minecraft:exploration_map` loot function** for chest loot or a `/loot give @s loot <table>` shop, one table per structure, no slot limit. `destination` is a plain tag id with **no `#`** (vanilla `chests/shipwreck_map` omits it and defaults to `on_treasure_maps`). `#dungeonnowloading:temple_of_duality` already exists in the DNL jar and works as-is
  - **[MoreJS](https://modrinth.com/mod/morejs)** (1.20.1 Forge, needs KubeJS, already present) for genuine new cartographer trades, `MoreJS.villagerTrades(e => ...)` + `VillagerUtils.createStructureMapTrade(input, structure)` chained with `.displayName()/.marker()/.scale()/.noPreview()`
  - Decide route before building, see the Async Locator caveat under Mods to add
- [ ] Sanity-check map search cost against the new rarities set in [`mod_configs_server/structurify.json`](mod_configs_server/structurify.json): `temple_of_duality` at spacing 32 resolves fast and close, `air_villages` at spacing 188 will be the slow one and lands thousands of blocks out
- [x] Add waystones, remove the option to mine them, make them hard to craft, make them sparse like 500 blocks, give travel a reasonable XP-based scaling cost (usable by endgame, harder early game, not crazy). Waystone recipe uses warp stone; disable warp stone crafting (later added to expensive trader trades). For now warp stone should be unobtainable.

## Mobs
- [x] Ban Born in Chaos mobs: Door Knight, Corpse fly, Bloody Gadfly
- [x] Disable fly on eating rotten meat rule, disable maggots spawning rule
- [ ] Allow the stalker from Born in chaos to spawn only a full moon
- [x] Ban 19 mobs from Alex's Mobs (via alexsmobs.toml spawn weights), see [Modpack Wiki](docs/modpack-wiki.md)
- [x] Reduce the spawn chance/count of catfish (weight 4 → 2)
- [ ] Change mob spawns and which biomes they spawn in
- [ ] Update illager caravans with new loot and different variants

## Loot Tables
- [ ] Update loot tables for structures; add loot integrations for new buildings
- [ ] Update loot tables for traders, ribbits, goblin traders, and wandering traders, include **warp stone** as an expensive trade (this is the only intended source of warp stone; its crafting recipe is disabled, and warp stone gates the waystone recipe)
- [ ] Add loot to caravans

## Mob Drops
- [ ] Remove soul drops from Companions; turn souls into another recipe instead (e.g. convert XP into souls)


## Compats
- [ ] Add compat between Iron's Spells and Origins
- [ ] Add compat between Companions and Iron's Spells
- [ ] Add "Let's Do: Bakery" and check cross-compatibility with other recipes (Farmer's Delight, etc.)
- [ ] Make enchantment compatibilities
- [ ] **Restorine ↔ Durite/Mendstone repair compat**, two parallel repair economies overlap: Ascendant Arcana's **Restorine** (`ascendant_arcana:restorine`, universal repair ingredient, geode worldgen from budding restorine + injected into chests with amethyst/diamonds) and Dungeon Now Loading's **Durite** (`dungeonnowloading:durite`, geode from the Durite Queller, or diamond ore mined with a Mendstone Pickaxe) / **Mendstone** (`dungeonnowloading:mendstone`), which feed the Mending Table. Intended split:
  - **Durite / Mendstone = full repair**, fixes the whole item in one step (keeps the Mending Table meaningful as the "expensive, complete" repair).
  - **Restorine stays as-is**, the incremental universal repair ingredient, unchanged behaviour and unchanged acquisition.
  - Consider a **combined Durite + Restorine recipe** as the bridge between the two systems (single crafted repair item).
  - `ascendant_compatibility-1.1.0` already ships DNL support, but only Arcana **enchantment recipes** (`break_protection`, `durable`, and disabled `nullification`/`amplification`), nothing for the repair materials, so this is ours to add (KubeJS or a datapack).


## Visual
- [x] Remove fire fissures from the effects
- [X] Remove water bubble fissures from the effects

## UI
- [] Fix hearts in the UI to not overlap on each other
- [x] Add support for obscuria tips, [`resourcepacks/Obscure_Tooltips_Styles/`](resourcepacks/Obscure_Tooltips_Styles/) gives Relics, More Relics, Unique Accessories, Artifacts and Iron's Spellbooks their own Obscure Tooltips label and panel style, instead of everything reading "Rare"/"Uncommon" in the same grey box. Iron's Spellbooks is coloured by SCHOOL (8 schools, armour mapping read out of `ExtendedArmorMaterials`). Needs enabling in Options → Resource Packs after deploy
- [x] Enable the built-in **Vibrant Tooltips** pack (it was off, so no rarity panels, enchant glint or cursed glint rendered at all) and the new styles pack, both switched on in [`client_configuration/options.txt`](client_configuration/options.txt), the new repo home for the vanilla client options file. Deploy: copy `resourcepacks/Obscure_Tooltips_Styles/` into the instance FIRST, then the `resourcePacks:` line, Minecraft silently drops pack ids it cannot resolve