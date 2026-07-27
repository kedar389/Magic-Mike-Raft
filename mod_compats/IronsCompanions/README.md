# Iron's Companions — Compatibility / Content Mod

> **Provisional mod id:** `irons_companions` · **Loader:** Forge 1.20.1 (47.4.0) · **Status:** design locked, not yet built
>
> Bridges **Iron's Spells 'n Spellbooks** (`irons_spellbooks` 3.16.2) with the **Companions!** mod
> (`companions` 1.2.3, by Xylonity/ModderG — GPL v3, source: <https://github.com/Xylonity/Companions>).
>
> Sibling of the existing `mod_compats/OriginSpellSchools` mod and follows the same build layout.

---

## 1. Purpose

Two goals:

1. **Convert Companions' "magic book" spells into first-class Iron's Spells (IS) spells** — castable from a
   Scroll *and* inscribable into any spellbook/grimoire — recreating each effect, animation, and behaviour
   **exactly** (they reuse the original Companions projectile entities, so visuals are pixel-identical).
2. **Weave the two mods together**: correct IS damage schools on companion abilities, armour-set bridges,
   ally/faction rules, and a Soul Mage that casts real IS spells.

## 2. Environment constraints

- **No JDK/Gradle in the dev environment** (see `.wolf/cerebrum.md`). This mod will be **scaffolded here and
  built elsewhere** — same "scaffold now, verify later" approach used for OriginSpellSchools. Datapack-side
  changes (loot/recipe/JEI-hide) work immediately without a build.
- Target Forge **1.20.1-47.4.0** to match the live server.

## 3. Dependencies (mods.toml)

| Mod | Why | Required |
|---|---|---|
| `irons_spellbooks` (3.16.2) | Spell registry, `AbstractSpell`, SchoolType, events, Scroll item | ✅ after |
| `companions` (1.2.3) | Reuse its projectile entities + retype its ability damage | ✅ after |
| `gtbcs_spell_lib` (1.5.0) | Helper API the IS addons build on (spell damage/crit attributes) | ✅ |
| `gtbcs_geomancy_plus` (2.0.0) | Owns the **`geo`** school used by Stone Spikes | ✅ after |
| `apothic_attributes` / `attributeslib` | Spell-power / resist attributes for armour bridges | ✅ |
| `curios` | Reads worn armour for set bonuses (equipment events) | optional |

## 4. Architecture

One Forge mod, four surfaces:

- **Content** — register new IS spells (`AbstractSpell` subclasses) that spawn the existing Companions
  projectile entities.
- **Forge events** (clean, external) — armour-set bonuses, mage-set IS-damage reduction, holy-robe holy bonus,
  ally healing, no-friendly-fire, and companion-ability damage retyping.
- **Mixins** (invasive) — Soul Mage AI (cast IS spells from inventory scrolls).
- **Datapacks** — disable/hide removed books, add the Naginata scroll to the Sacred Pontiff loot table.

> **Item removal is external, not a fork.** Removed books are stripped of recipes/loot and JEI-hidden; their
> registry entries technically remain but become unobtainable. (Decision 2026-07-27.)

---

## 5. Feature spec

### 5A. Magic Books → Iron's Spells

All conversions use **Option B (exact recreation)**: a new IS spell whose `onCast` spawns the original
Companions projectile entity, owned by the caster, with damage retyped to the listed IS school and scaled by
the caster's spell power. Every one is available as a **Scroll** and **inscribable into a spellbook**.

| Book (removed) | New IS spell | School | Base value | Cooldown | Rarity / tiers | Effect entity | Acquisition |
|---|---|---|---|---|---|---|---|
| `book_ice_tornado` | Ice Tornado | **ice** | 3.0 dmg/tick | 200t | tiered scrolls (see 5A-i) | `TornadoProjectile` | loot/trade |
| `book_stone_spikes` | Stone Spikes | **geo** (Geomancy Plus) | 4.0 dmg | 140t | Rare | `StoneSpikeProjectile` | loot/trade |
| `book_heal_ring` | Heal Ring | **nature** | +6.0 heal | 200t | Uncommon–Rare | `HealRingProjectile` | loot/trade |
| `book_naginata` | Naginata | **holy** | TBD (from `HolinessNaginataProjectile`) | 140t | **Legendary** | `HolinessNaginataProjectile` | **drops from Sacred Pontiff** |

**5A-i · Ice Tornado tiers.** IS spells are level-scalable (1–N). Model "different tiers" as one spell whose
damage scales per level off the 3.0 base, distributed as scrolls at **Uncommon → Rare → Epic** (higher tier =
higher inscribed level). Exact level curve tunable at build time.

**5A-ii · Assets are reused, not copied.** Because we spawn the original entities, their GeckoLib
models/animations/textures/sounds (e.g. `ice_tornado.geo.json`, `stone_spike0-6.png`,
`spell_release_tornado.ogg`) render unchanged. If a projectile constructor isn't public, add an Access
Transformer rather than copying assets.

### 5B. Removed books (unobtainable + JEI-hidden)

`book_ice_shard`, `book_magic_ray`, `book_fire_mark`, `book_brace`, `book_black_hole`, **and** the four
converted books above (players get the IS spell instead of the book).

Removal steps (datapack, no build):
- Remove/override crafting recipes (e.g. `data/companions/recipes/book_naginata.json` — the relic-gold recipe).
- Strip them from any Companions loot/trade tables.
- Hide all `companions:book_*` items in JEI (`data`/JEI blacklist tag).

### 5C. Soul Mage casts Iron's Spells

Replace the Soul Mage's book-based attack goals (`SoulMageTornadoGoal`, `SoulMageStoneSpikesGoal`,
`SoulMageHealRingGoal`, `SoulMageNaginataGoal`) so it **casts real IS spells chosen from scrolls in its
inventory**, pool = the full IS spell registry. IS already supports non-player casters (`MagicData` /
`IMagicEntity`); implement via Mixin into the Soul Mage entity/goal set. **Hardest item — build last.**

### 5D. Companion ability damage → IS schools

Retype companion ability damage to the correct IS school (via `SpellDamageEvent` / `LivingHurtEvent`),
scaling off the owner's matching spell power where the companion is owned. Proposed mapping (tunable):

| Companion / ability | IS school |
|---|---|
| Sacred Pontiff / His Holiness (fire ring, holy attacks) | **holy** |
| Shade Sword / Shade Maw | **blood** |
| Dinamo / Tesla network / Illager Tesla Golem | **lightning** |
| Ember Pole / Living Candle / Firework Toad / Nether Bullfrog | **fire** |
| Ender Frog | **ender** |
| Others (Minion arms, Antlion sand, Croissant Dragon, Puppet) | physical / no retype |

### 5E. Crystallized Blood weapons

The **Crystallized Blood Sword** ("Blood Slash") and **Axe** ("Blood Vortex", `BloodTornadoProjectile`)
ability damage → **IS blood** magic damage. (Scythe lifesteal left as-is unless requested.)

### 5F. Armour-set bridges

- **Mage set** (`mage_hat`/`mage_coat`/`mage_leggings`): its "Mystic Reflection" reduction must **also reduce
  incoming IS spell damage** (hook `SpellDamageEvent`/`LivingHurtEvent`; reduce when victim wears the set and
  source is IS magic). Its now-defunct "-cooldown on Magic Books" bonus → **repurpose to IS cooldown
  reduction / mana** (books no longer exist).
- **Holy Robe set** (`holy_robe_mask`/`coat`/`leggings`): **+10% holy spell power per piece** (3 pieces =
  +30%) via an AttributeModifier on the IS holy spell-power attribute, applied on equip change.
- **Crystallized Blood set**: unchanged (its low-health damage reduction stays).

### 5G. Ally / faction

- IS heal/buff spells (Heal, Greater Heal, Healing Circle, Cloud of Regeneration, Blessing of Life, Haste,
  etc.) treat owned companions as **valid ally targets**.
- **No friendly fire** between owner ↔ IS summons ↔ Companions (shared ally check keyed on owner UUID).

---

## 6. Data / loot changes (datapack)

- **Add** the Naginata Legendary scroll to the **Sacred Pontiff** loot table (and/or His Holiness).
- **Remove** `companions:book_naginata` crafting recipe; strip removed books from loot/trades.
- **JEI-hide** all `companions:book_*` items.
- Distribute Ice Tornado / Stone Spikes / Heal Ring scrolls via loot/trades (tables TBD).

## 7. Implementation phases

1. **Scaffold** — folder, `build.gradle`, `mods.toml`, main class, mixin config (mirror OriginSpellSchools).
2. **Spells** — register 4 IS spells (schools/rarities/cooldowns above) spawning the Companions projectiles.
3. **Damage retype + Blood weapons** (5D/5E) via damage events.
4. **Armour bridges** (5F) via equipment/damage events + attributes.
5. **Ally/faction** (5G).
6. **Datapack** — Pontiff loot, recipe/loot removal, JEI-hide.
7. **Soul Mage AI** (5C) via Mixin — last, hardest.

## 8. Open items (confirm from source at build time)

- Naginata projectile damage (not in config; read `HolinessNaginataProjectile`).
- Exact Ice Tornado per-level damage curve.
- Whether Companions projectile constructors are public (else add AccessTransformer).
- Final companion→school mapping sign-off (5D).

## 9. References

- Companions source (GPL v3): <https://github.com/Xylonity/Companions>
- IS core schools: fire, ice, lightning, holy, ender, blood, evocation, void, **nature**, eldritch.
  Rarities: common, uncommon, rare, epic, **legendary**.
- Earth magic school id = `gtbcs_geomancy_plus:geo` (there is no "teramancy" school).
- Locked decisions: `.wolf/cerebrum.md` (Decision Log, 2026-07-27).
