# Obscure Tooltips, Styles

Client resource pack. Gives the pack's accessory mods and Iron's Spellbooks their own tooltip
look and their own header label in **Obscure Tooltips** (`obscure_tooltips-forge-1.20.1-3.10.0`),
instead of everything rendering as the same generic grey box.

Namespace: `magic_tooltips`.

## Why

Every item in Relics, More Relics and Artifacts is registered with vanilla `Rarity.RARE`, and
every Iron's Spellbooks spellbook with `Rarity.UNCOMMON`, so the default rarity label reads the
same word on all of them and they all get the same panel. Unique Accessories is the only mod of
the four that tiers itself, 25 uncommon / 14 rare / 6 epic. Iron's Spellbooks scrolls do not
override `getRarity` at all, they colour their *name* instead, so no rarity styling reaches them.

Rarity carries almost no information in this pack. Mod and school do.

## Accessories

| Items | Label | Panel |
|---|---|---|
| `relics:*`, `morerelics:*` | Relic | violet `arcane`, spinning item icon |
| `#artifacts:artifacts` | Artifact | amber `gilded`, spinning item icon |
| `#uniqueaccessories:accessories` | Accessory | teal `verdant` |
| `#uniqueaccessories:accessory_box_rare` | Accessory · Rare | teal `verdant`, spinning item icon |
| `#uniqueaccessories:accessory_box_epic` | Accessory · Epic | teal→gold `radiant`, burst icon, ray glow + shimmer |

`relics:relic_experience_bottle` and `relics:solid_snowball` are excluded, they are consumables,
not relics. Artifacts and Unique Accessories are matched through the item tags those mods already
ship, so new items are picked up automatically.

## Iron's Spellbooks

Anything without a school gets the indigo `spellcraft` panel and a plain category label:
spellbooks (`#curios:spellbook`), staves (`#irons_spellbooks:staff`), runes
(`#irons_spellbooks:inscribed_rune` + `blank_rune`), `scroll`, `scroll_pouch`, and every
`*_upgrade_orb`.

School items override that with a school panel and a school label:

| School | Panel | Items |
|---|---|---|
| Fire | orange | pyromancer set, `fire_rune`, `fire_upgrade_orb`, `pyrium_staff` |
| Ice | pale blue | cryomancer set, `ice_rune`, `ice_upgrade_orb`, `ice_staff` |
| Lightning | yellow | electromancer set, `lightning_rune`, `lightning_upgrade_orb`, `lightning_rod` |
| Holy | warm white | priest set, `holy_rune`, `holy_upgrade_orb` |
| Ender | purple | shadowwalker set, `ender_rune`, `ender_upgrade_orb` |
| Blood | crimson | **cultist** set, `blood_rune`, `blood_upgrade_orb`, `blood_staff`, `improved_blood_staff` |
| Nature | green | **plagued** set, `nature_rune`, `nature_upgrade_orb` |
| Evocation | jade | archevoker set, `evocation_rune`, `evocation_upgrade_orb` |

The armour to school mapping is read out of `ExtendedArmorMaterials` in the mod jar, not guessed.
Two are not obvious from the name: **cultist grants Blood Power** and **plagued grants Nature
Power**. Tarnished, wandering magician, pumpkin, netherite mage and wizard grant only Max Mana and
generic Base Power, so they stay on `spellcraft`. Paladin, infernal sorcerer and speed boots are
"artifact" sets with no school. There is no eldritch armour set.

Scrolls are one item with the spell in NBT, so they cannot be styled per school. Unique spellbooks
(`ice_spell_book`, `blaze_spell_book`, `druidic_spell_book`, ...) are left on `spellcraft` because
their school was not verified.

## Layering

`definition` priority decides which style wins; the highest matching one is used.

- `0`, `obscure_tooltips:default`
- `100`, Vibrant Tooltips rarity styles (uncommon / rare / epic)
- **`145`, this pack, Iron's Spellbooks generic**
- **`150`, this pack, accessories**
- **`155`, this pack, Iron's Spellbooks per school**
- **`160` / `170`, this pack, Unique Accessories rare / epic**
- `200` / `300`, Vibrant Tooltips enchanted / cursed

So an enchanted or cursed item still gets its glint; everything else gets the family style.
Labels are a separate registry with their own priorities (`100`–`120` for accessories, `105`–`115`
for Iron's Spellbooks; the built-in rarity label is `0`).

Nothing references `vibrant_tooltips:*`, so the pack looks correct whether or not the built-in
Vibrant Tooltips pack is enabled.

## Deploy

Copy the folder into the client instance's `resourcepacks/` and enable it in
Options → Resource Packs. Client-side only, the server never reads it.

Enable the built-in **Vibrant Tooltips** pack underneath it for the enchant and curse glints this
pack deliberately leaves alone. Its options.txt id is `obscure_tooltips/packs/vibrant_tooltips`
(Fragmentum builds built-in pack ids as `<modid>/<directory>`).

Item tags have to be synced from the server, so the tag-matched entries resolve in-world, not on
the title screen.

## Schema reference

Obscure Tooltips reads these directories from any resource pack, under `assets/<ns>/tooltips/`:
`definition/`, `label/`, `style/`, `element/panel/`, `element/frame/`, `element/icon/`,
`element/slot/`, `element/effect/`.

- **definition**, `{ priority, style, filter }`
- **label**, `{ priority, provider, filter }`; providers `blank` / `literal` (`text`) /
  `translatable` (`key`) / `rarity`. All are forced to `ChatFormatting.GRAY`.
- **style**, `{ panel, frame, icon, slot, effects[] }`, every field optional, missing ones fall
  back to `obscure_tooltips:default`.
- **filters**, `always`, `never`, `all_of`/`any_of`/`none_of` (`terms`), `item` (`values`, accepts
  `#tag` entries), `mod` (`mods`), `id` (`target` = `FULL`/`NAMESPACE`/`PATH` + `contains` /
  `starts_with` / `ends_with`), `nbt` (`nbt`, `match_exact`), `rarity` (`rarity`), `enchantment`
  (`enchantments`, `any_enchantment`, `any_curse`), `property` (`has_foil`), `config`
  (`config_value`, `@NAME` of a key in `obscure_tooltips-client.toml`).
- **panels/slots**, `blank` or `color_rect` (`background_palette`/`border_palette`, per-corner
  `#AARRGGBB`). **frames**, `blank` or `nine_sliced` (`texture_sheet`). **icons**, `static`,
  `accent`, `accent_spin`, `accent_burst`, each `{ transform, sound }`; they render the item stack
  itself in the tooltip header. **effects**, `glint`, `shimmer`, `ray_glow`, `rim_light`
  (`inner_palette`/`outer_palette`), `icon_particle`, `inward_particle`.
