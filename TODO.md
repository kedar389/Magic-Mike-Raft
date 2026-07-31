# TODO

## Mods to add
- [] add lets do bakery, needs checking the farm and charm that is being pulled ind
- [X] add ascended arcana
- [x] Add feur extension jungle
- [x] Add colorful hearts

## Modpack Management
- [ ] Create a script that copies config and datapacks over to the server

## Balance
- [x] Deduplicate curio slots
- [x] Merge duplicate attributes — Iron's Lib set (crit damage, dodge chance, arrow damage, armor pierce, experience gained, healing received, mining speed) folded into Apothic Attributes via [`datapacks/Attribute_Merge/`](datapacks/Attribute_Merge/)
- [ ] Merge remaining duplicate attributes: Tinkers' (Critical Damage, Experience, Mining Speed), Cataclysm (`additional_critical_damage`), Obscure API/Aquamirae (crit damage, crit hit, dodge, penetration) — blocked on confirming their value scales (remap does not rescale amounts); jump height has only ONE attribute (Tinkers'), other mods do it in code — see [`Attribute_Merge/README.md`](datapacks/Attribute_Merge/README.md)

## Biomes
- [ ] Use the new woods from Cluttered and add them to the biomes in William Wythers
- [ ] Change where flowers spawn
- [ ] Make desert biomes bigger by 0.5
- [ ] Make other biomes bigger by 0.3

## Structures
- [ ] Make structures spawn on flat terrain
- [x] Add waystones — remove the option to mine them, make them hard to craft, make them sparse like 500 blocks, give travel a reasonable XP-based scaling cost (usable by endgame, harder early game — not crazy). Waystone recipe uses warp stone; disable warp stone crafting (later added to expensive trader trades). For now warp stone should be unobtainable.

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
- [ ] Add compat between Iron's Spells and Origins
- [ ] Add compat between Companions and Iron's Spells
- [ ] Add "Let's Do: Bakery" and check cross-compatibility with other recipes (Farmer's Delight, etc.)

## Visual
- [x] Remove fire fissures from the effects
- [X] Remove water bubble fissures from the effects

## UI
- [] Fix hearts in the UI to not overlap on each other
- [x] Add support for obscuria tips, [`resourcepacks/Obscure_Tooltips_Styles/`](resourcepacks/Obscure_Tooltips_Styles/) gives Relics, More Relics, Unique Accessories, Artifacts and Iron's Spellbooks their own Obscure Tooltips label and panel style, instead of everything reading "Rare"/"Uncommon" in the same grey box. Iron's Spellbooks is coloured by SCHOOL (8 schools, armour mapping read out of `ExtendedArmorMaterials`). Needs enabling in Options → Resource Packs after deploy
- [x] Enable the built-in **Vibrant Tooltips** pack (it was off, so no rarity panels, enchant glint or cursed glint rendered at all) and the new styles pack, both switched on in [`client_configuration/options.txt`](client_configuration/options.txt), the new repo home for the vanilla client options file. Deploy: copy `resourcepacks/Obscure_Tooltips_Styles/` into the instance FIRST, then the `resourcePacks:` line, Minecraft silently drops pack ids it cannot resolve