# TODO

## Mods to add
- [] add lets do bakery
- [] add ascended arcana
- [] Add feur extension jungle


## Modpack Management
- [ ] Create a script that copies config and datapacks over to the server

## Balance
- [x] Deduplicate curio slots
- [x] Merge duplicate attributes — Iron's Lib set (crit damage, dodge chance, arrow damage, armor pierce, experience gained, healing received, mining speed) folded into Apothic Attributes via [`datapacks/Attribute_Merge/`](datapacks/Attribute_Merge/)
- [ ] Merge remaining duplicate attributes: Tinkers' (Critical Damage, Experience, Mining Speed), Cataclysm (`additional_critical_damage`), Obscure API/Aquamirae (crit damage, crit hit, dodge, penetration) — blocked on confirming their value scales (remap does not rescale amounts); jump height has only ONE attribute (Tinkers'), other mods do it in code — see [`Attribute_Merge/README.md`](datapacks/Attribute_Merge/README.md)

## Biomes

- [ ] Change where flowers spawn
- [ ] Make desert biomes bigger by 0.5
- [ ] Make other biomes bigger by 0.3

- [ ] Use the new woods from Cluttered and add them to the biomes in William Wythers
- Cluttered:
    - [ ] Replace red mushroom block with red mushroom cap and mushroom stem (only of red mushrooms if possible) with red mushroom stem 
    - [ ] Add red mushrooms to: phantasmal forest, phantasmal_swamp, mushroom_caves, lush_fungous_dripstone_caves, lush_shroom_caves (small (sapling) and big), fungous_dripstone_caves, nether_wastes, birch_swamp (only small)
    - [ ] Add blue mushrooms to: phantasmal forest, phantasmal_swamp, mushroom_caves, lush_fungous_dripstone_caves, lush_shroom_caves (small (sapling) and big), fungous_dripstone_caves (only small)
    - [ ] Add poplar to: pantanal, flowering_pantanal, cool_forest_edge, forest_edge, spring_flower_forest (sprinkled in), boreal_forest_yellow (same shape as the other trees)
    - [ ] Add sycamore to: ancient_moss_forest, cool_forest, highland forest, forest, old_growth_birch_forest, birch_forest (sprinkled in)
    - [ ] Add willow to: bayou, dripleaf_swamp, waterlily_swamp, swamp (sprinkled in), sapling in swamp chests + wandering traders
    - [ ] Add crabapple to: subtropical_grassland (sparse), lapacho_plains (sprinkled in), sell from wandering traders
    - [ ] Remove fluorescent maple entirely 

- Fruits Delight

    - [ ] Add Pear Tree: warm_birch_forest, spring_flower_forest, spring_flower_fields, highland_plains
    - [ ] Add Hawberry Tree: birch_taiga, boreal_forest_red, boreal_forest_yellow (in the shape of the trees w azalea leaves), old_growth_taiga_swamp 
    - [ ] Add Persimmon Tree: deep_snowy_taiga, cool_forest
    - [ ] Add Lychee Tree / Mango Tree: tropical_forest, tropical_rainforest, subtropical_forest
    - [ ] Add Peach Tree: sparse_bamboo_jungle, dry_tropical_forest, tropical_forest
    - [ ] Add Orange Tree / Apple Tree / Kiwi Tree / Fig Tree: forest_edge, spring_flower_forest, temperate_rainforest, harvest_fields
    - [ ] Add Blueberry Bush: cold_plains, birch_taiga, larch_taiga, boreal_forest_red, birch_swamp, pantanal
    - [ ] Add Hamimelon: cactus_desert, deep_desert, guelta, desert_island, dry_savanna
    - [ ] Add Pineapple: tropical_island, desert_island, sand_dunes, desert_beach, tropical_beach
    - [ ] Add Mangosteen: tropical_forest, tropical_rainforest, coastal_mangroves
    - [ ] Add Cranberry: tropical_rainforest, bamboo_swamp, flooded_rainforest, coastal_mangroves
    - [ ] Add Bayberry: coastal_mangroves, pantanal, bayou, dripleaf_swamp
    - [ ] Add Durian (RARE) flooded_temperate_rainforest, bamboo_jungle_swamp
    
## Structures
- [ ] Make structures spawn on flat terrain
- [ ] Add waystones — remove the option to mine them, make them hard to craft

## Mobs
- [x] Ban Born in Chaos mobs: Door Knight, Corpse fly, Bloody Gadfly
- [x] Disable fly on eating rotten meat rule, disable maggots spawning rule
- [ ] Allow the stalker from Born in chaos to spawn only a full moon
- [x] Ban 19 mobs from Alex's Mobs (via alexsmobs.toml spawn weights) — see [Modpack Wiki](docs/modpack-wiki.md)
- [x] Reduce the spawn chance/count of catfish (weight 4 → 2)
- [ ] Change mob spawns and which biomes they spawn in
- [ ] Update illager caravans with new loot and different variants

## Loot Tables
- [ ] Update loot tables for structures; add loot integrations for new buildings
- [ ] Update loot tables for traders, ribbits, goblin traders, and wandering traders
- [ ] Add loot to caravans

## Mob Drops
- [ ] Remove soul drops from Companions; turn souls into another recipe instead (e.g. convert XP into souls)

## Compats
- [ ] Decide which origins to keep
- [ ] Test origins skills + debuffs
- [ ] Test origin diet with other mod foods

## Compats
- [ ] Add compat between Iron's Spells and Origins
- [ ] Add compat between Companions and Iron's Spells
- [ ] Add "Let's Do: Bakery" and check cross-compatibility with other recipes (Farmer's Delight, etc.)

## Visual
- [x] Remove fire fissures from the effects
- [X] Remove water bubble fissures from the effects

## UI
- [] Fix hearts in the UI to not overlap on each other