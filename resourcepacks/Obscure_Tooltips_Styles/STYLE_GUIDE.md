# Tooltip style guide

How items in this modpack are supposed to look when you hover them, and why. This is the
decision document. [`README.md`](README.md) covers the file format and the current contents;
this file covers the rules those files are trying to express.

## The problem this is solving

Vanilla rarity is nearly useless in this pack. Every Relics, More Relics and Artifacts item is
hardcoded `Rarity.RARE`; every Iron's Spellbooks spellbook is `Rarity.UNCOMMON`; Iron's Spellbooks
scrolls do not set a rarity at all. Roughly 150 items claim to be "Rare" and mean nothing by it.

So the tooltip does not colour by rarity. It colours by **what the item is**, and reserves the
loud treatments for **how special it actually is**.

## Three independent channels

Obscure Tooltips gives us three things that can vary per item, and each one carries a different
kind of information. Keeping them separate is the whole design.

| Channel | Answers | Set by |
|---|---|---|
| **Panel** (background + border colour) | *What is this?* | `element/panel/` via `style/` |
| **Frame** (corner ornaments) | *How rare is this?* | `element/frame/` via `style/` |
| **Effects** (animated overlay) | *Should I be excited or worried?* | `element/effect/` via `style/` |

A fourth, the **label** (the small grey line under the name), just names the category in words.

### Channel 1: panel colour = category

The default. What kind of thing you are holding.

| Category | Colour | Panel |
|---|---|---|
| Relic (Relics, More Relics) | violet | `arcane` |
| Artifact (Artifacts) | amber | `gilded` |
| Accessory (Unique Accessories) | teal | `verdant` |
| Spellbook / tome | book leather, warm brown | `tome` |
| Staff / wand | polished wood + metal | `wandcraft` |
| Iron's Spellbooks gear, per school | the school's colour | `school_*` |
| Everything else in a supported mod | indigo | `spellcraft` |

School colours: fire orange, ice pale blue, lightning yellow, holy warm white, ender purple,
blood crimson, nature green, evocation jade.

**Two overrides beat the category colour:**

- **Unique → orange.** Not a rarity tier, a statement that there is one of these. In practice that
  means **boss drops** and one-of-a-kind gear. Unique spellbooks are orange. Vertex Bow and
  Scorcher are orange because they come off a boss, not because they are strong.
- **Rare → blue.** Genuinely rare-but-not-unique items. Blue, not cyan. Cyan reads as the
  accessory teal and the two were being confused.

### Channel 2: frame = rarity

Frames are corner ornaments, not a continuous border (see README for the geometry). They are the
quietest channel, which makes them the right one for a tier that applies across every category.

- **Epic** gets one distinctive frame of its own, shared across all categories. If you see it, the
  item is epic, whatever the panel colour says it is.
- **Everything below epic** gets a frame chosen by **category**, so a staff and a tome are
  distinguishable at a glance even before you read the name. Staves get one, tomes get one, and so
  on per category.

This is why epic does not need its own panel colour: the frame already says it, and the panel is
free to keep saying what the item *is*.

### Channel 3: effects = attention

Animated overlays. Expensive to look at, so they are rationed.

- **Unique shimmer** (the enchantment-glint treatment, in orange) marks unique items. **Only on
  gear** though: armour, weapons, curios, spellbooks, staves. Things you equip.
- **Ordinary items** (materials, ingredients, reagents such as a Divine Pearl) get an animated
  background **only if they are already Epic**. A crafting material should not shimmer just
  because it is uncommon.
- **Cursed red glint**, the same evil-looking treatment vanilla curses get, marks items that are
  **strong but carry a real downside**. Not "cursed" in the enchantment sense: powerful with a
  cost. This is the one channel that is a warning rather than a reward.
- **Epic and legendary scrolls** get a background. See the limitation below.

## Precedence

When several rules apply to one item:

```
panel    unique/boss  >  rare  >  category
frame    epic         >  category
effects  cursed-red   +  unique shimmer   (these stack, they are different signals)
```

Mechanically this is the `definition` priority number. The ladder, low to high:

| Priority | What |
|---|---|
| 0 | `obscure_tooltips:default` |
| 100 | Vibrant Tooltips rarity styles |
| 145 | category, generic (spellcraft, tome, wandcraft) |
| 150 | category, per mod (relic, artifact, accessory) |
| 155 | category, per school (Iron's Spellbooks) |
| 160 / 170 | Unique Accessories rare / epic |
| 180 | rare (blue) |
| 190 | unique (orange) |
| 200 / 300 | Vibrant Tooltips enchanted / cursed |

Only one `definition` wins per item, so a style that needs both a colour and an effect has to
declare both. Labels are a separate registry and never conflict with styles.

## Rules for adding a mod

1. **Match by tag if the mod ships one.** `#artifacts:artifacts` and
   `#uniqueaccessories:accessories` mean new items are picked up without touching this pack.
   Fall back to a namespace `mod` filter, and only then to explicit item lists.
2. **Verify the classification, do not infer it from the name.** The Iron's Spellbooks armour to
   school mapping was read out of `ExtendedArmorMaterials`; two sets (cultist is Blood, plagued is
   Nature) would have been wrong if guessed.
3. **Do not style another mod's vanilla items.** Iron's Spellbooks tags `minecraft:blaze_rod` as a
   fire focus. A blaze rod is not a magic item and must not get a fire tooltip.
4. **Leave a category alone rather than guess it.** Unique spellbooks other than the ones verified
   stay on the generic tome panel.
5. **Never copy assets out of a mod jar.** The Obscure Tooltips licence forbids reuse of its
   textures, so every frame in this pack is original art.

## Known limitation: scroll rarity is not reachable

Epic and legendary scrolls cannot be selected. Iron's Spellbooks stores only
`ISB_Spells:{data:[{id, level, locked}]}` on the stack. The rarity is computed at runtime from the
spell **and its level**, and is never written to NBT, so there is no NBT path to match on and the
`rarity` filter sees the item's static rarity, which scrolls do not set.

What is possible instead:

- All scrolls share one background (what the pack does now), or
- Match specific spells by id through the `nbt` filter, which works but has to be enumerated by
  hand and still cannot express "this spell at level 4 and above".

Worth revisiting if Iron's Spellbooks ever writes a rarity tag onto the stack.
