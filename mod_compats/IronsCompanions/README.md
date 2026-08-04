# Iron's Companions, Compatibility / Content Mod

> **Mod id:** `irons_companions` · **Loader:** Forge 1.20.1 (47.4.0) · **Status:** compiles clean, never run in game
>
> Bridges **Iron's Spells 'n Spellbooks** (`irons_spellbooks` 3.16.2) with the **Companions!** mod
> (`companions` 1.2.3, by Xylonity/ModderG, GPL v3, source: <https://github.com/Xylonity/Companions>).
>
> Sibling of the existing `mod_compats/OriginSpellSchools` mod and follows the same build layout.

---

## 1. Purpose

Two goals:

1. **Convert Companions' "magic book" spells into first-class Iron's Spells (IS) spells**, castable from a
   Scroll *and* inscribable into any spellbook/grimoire, recreating each effect, animation, and behaviour
   **exactly** (they reuse the original Companions projectile entities, so visuals are pixel-identical).
2. **Weave the two mods together**: correct IS damage schools on companion abilities, a Holy Robe spell
   power bridge, and a Soul Mage that casts real IS spells.

## 2. Build

```
cd mod_compats/IronsCompanions
# one-time: copy the deps named in build.gradle out of the live mods folder
cp "../../servers_folder/magic server/mods/"{companions-forge-1.20.1-1.2.3,gtbcs_spell_lib-1.5.0-1.20.1,gtbcs_geomancy_plus-2.0.0-1.20.1,geckolib-forge-1.20.1-4.8.4,knightlib-forge-1.20.1-1.6.1,curios-forge-5.14.1+1.20.1}.jar libs/
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.20.8-hotspot" ./gradlew build --no-daemon
```

Produces `build/libs/irons_companions-0.1.0.jar`. Gradle 8.1.1 wrapper, Forge **1.20.1-47.4.0** to
match the live server. `libs/*.jar` is gitignored.

## 3. Dependencies (mods.toml)

| Mod | Why | Required |
|---|---|---|
| `irons_spellbooks` (3.16.2) | Spell registry, `AbstractSpell`, SchoolType, events, Scroll item | ✅ after |
| `companions` (1.2.3) | Reuse its projectile entities + retype its ability damage | ✅ after |
| `gtbcs_spell_lib` (1.5.0) | Helper API the IS addons build on (spell damage/crit attributes) | ✅ |
| `gtbcs_geomancy_plus` (2.0.0) | Owns the **`geo`** school used by Stone Spikes | ✅ after |
| `apothic_attributes` / `attributeslib` | Spell-power / resist attributes for armour bridges | ✅ |
| `curios` | Reads worn armour for set bonuses (equipment events) | optional |
| `jei` (15.20.0.112) | Hides the removed magic books from the item list (API only, client) | optional |

## 4. Architecture

One Forge mod, four surfaces:

- **Content**, register new IS spells (`AbstractSpell` subclasses) that spawn the existing Companions
  projectile entities.
- **Forge events** (clean, external), holy-robe spell power, book removal, loot, and damage retyping.
- **Mixin** (one), `SoulMageBookSlotMixin` widens the Soul Mage's book slots to accept IS spell
  containers. Its `mayPlace` is `instanceof AbstractMagicBook`, which no event can widen. The Soul Mage
  AI itself needs no mixin (see phase 7).
- **Removals**, strip the recipes of the removed books (reload listener) and hide them via a JEI plugin.

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
| Book (removed) | Spell | School | Cast | Effect | Sound |
|---|---|---|---|---|---|
| `book_ice_tornado` | Ice Tornado | **ice** | instant, cd 10s | 1 tornado a block ahead | `SPELL_RELEASE_TORNADO` |
| `book_stone_spikes` | Stone Spikes | **geo** (Geomancy Plus) | 30t channel, cd 7s | 10 spikes in a 60° forward cone, ground-snapped | school default |
| `book_heal_ring` | Heal Ring | **nature** | 25t channel, cd 10s | ring at the caster | `SPELL_RELEASE_HEAL` |
| `book_naginata` | Naginata | **holy** | 45t channel, cd 7s | 6 blades rain on the aimed point | `SPELL_RELEASE_SPEARS` |

**Damage goes through Iron's Spells, not the Companions config.** `BaseProjectile` exposes no damage
setter and each projectile hardcodes its own damage, so `CompanionsProjectiles` tags the entity with the
firing spell and the scroll's level (`irons_companions_spell` / `_spell_level` in its persistent data).
`CompanionDamageEvents` then cancels the projectile's own hit and re-deals it with
`DamageSources.applyDamage(target, spell.getSpellPower(level, caster), spell.getDamageSource(projectile, caster))`,
the same call stock IS spells make. That is what makes the hit scale like a normal spell:

- `getSpellPower` = base + per-level, times the config `POWER_MULTIPLIER`, the generic
  `irons_spellbooks:spell_power` attribute and the school's own power attribute;
- the target's school resistance applies (`DamageSources.getResist`);
- `SpellDamageEvent` fires, so other IS addons see it;
- knockback, friendly fire and the spell's death message all behave as IS expects.

Cooldown and mana only apply when cast from a spellbook, which is stock IS behaviour for every spell
(`CastSource.consumesMana()` / `respectsCooldown()` are true only for `SPELLBOOK` and `SWORD`; a scroll is
consumed instead).

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

**How Companions handed the books out** (read from the 1.2.3 jar, 2026-08-03). Recorded because it is
what the removal has to cover, not because the replacements copy it: the scrolls use IS's own
distribution instead (section 6).

| Source | Books |
|---|---|
| Crafting recipe (`recipes/book_naginata.json`, 4x relic gold + book) | naginata (the only book recipe in the mod) |
| Chest loot, `CompanionsLootTables#onLootTableLoad` appends a `coin_pool` to every `minecraft:chests/*` table, 4.5% each | black_hole, magic_ray |
| Minion reward, `MinionEntity#mobInteract` pops one via `Block.popResource` | ice_shard, ice_tornado, brace, fire_mark, heal_ring, stone_spikes |

Removal steps, all inside this mod (`CompanionBooks` holds the one list of nine ids and the
book → spell mapping):
- `event/CompanionBookRemovalEvents`, an `AddReloadListenerEvent` listener that filters the
  `RecipeManager` and drops every recipe whose output is one of the books. Matching on output catches
  the recipe whatever its path or type, and it re-runs on `/reload`.
- `event/CompanionBookRemovalEvents#onEntityJoinLevel`, catches the Minion's popped reward (and any
  legacy book a player drops): converted book → its scroll, cut book → discarded. No recipe strip can
  reach `popResource`, so this is the only way to close that source.
- `compat/IronsCompanionsJeiPlugin`, a `@JeiPlugin` calling `removeIngredientsAtRuntime` so the books
  vanish from the item list. Client only, loaded by JEI's own plugin scan.

### 5C. Soul Mage casts Iron's Spells

Replace the Soul Mage's book-based attack goals (`SoulMageTornadoGoal`, `SoulMageStoneSpikesGoal`,
`SoulMageHealRingGoal`, `SoulMageNaginataGoal`) so it **casts real IS spells chosen from scrolls in its
inventory**, pool = the full IS spell registry. IS already supports non-player casters (`MagicData` /
`IMagicEntity`); implement via Mixin into the Soul Mage entity/goal set. **Hardest item, build last.**

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

- **Holy Robe set** (`holy_robe_mask`/`coat`/`leggings`): **+0.10 holy spell power per piece** (3 pieces =
  +0.30) added through `ItemAttributeModifierEvent`, so the line renders on the item tooltip and vanilla
  handles applying and removing it.
- **Mage set**, **Crystallized Blood set**: untouched, exactly as Companions ships them.

### 5G. Ally / faction

Not part of this mod. Friendly fire and ally targeting between the player, their IS summons and their
Companions behave exactly as the two mods ship them. (Decision 2026-08-04.)

---

## 6. Data / loot changes

- **Ice Tornado / Stone Spikes / Heal Ring scrolls** → **no code needed.** They ride Iron's Spells'
  own scroll distribution. IS appends scrolls through the `irons_spellbooks:randomize_spell` loot
  function, and an unfiltered `SpellFilter` resolves to `SpellRegistry.getEnabledSpells()` kept by
  `isEnabled() && allowLooting()`, weighted by `getMinRarity()`. Registering a spell in that registry
  is therefore enough: it appears wherever any other scroll appears (IS's eight `chest_loot`
  modifiers, its five `entity_drops` modifiers, its own structure tables) at a rarity-appropriate
  weight, and it scales with IS's own `quality` ranges. `allowLooting()` defaults to the school's flag,
  which is true for ice, nature and (verified) Geomancy Plus's `geo`.
- **Naginata scroll** → dropped by **Sacred Pontiff / His Holiness** (10%) via a Java loot event
  (`event/CompanionsLootEvents`), so the scroll NBT is built with the IS API. `NaginataSpell` overrides
  `allowLooting()` to **false**, which is what keeps the legendary out of the generic scroll rolls and
  boss-only.
- **Remove recipes** for all nine `companions:book_*` → `event/CompanionBookRemovalEvents`.
- **Swap Minion book rewards** for a random **common** scroll → same class, `onEntityJoinLevel`. Not a
  1:1 book-to-its-spell swap: Companions rolls a fixed pair per Minion variant (overworld = ice_shard or
  ice_tornado, 50/50), so 1:1 handed out the same scroll half the time.
- **JEI-hide** all `companions:book_*` → `compat/IronsCompanionsJeiPlugin`.

## 7. Implementation phases

> Status: all seven phases written and the jar **compiles clean** (2026-08-03). Remaining
> `VERIFY(build)` markers are runtime assumptions the compiler cannot check (entity ids, projectile
> constructors), not API guesses.

1. ✅ **Scaffold**, `build.gradle`, `mods.toml`, main class (mirrors OriginSpellSchools). Uses local
   deobf jars from `libs/`.
2. ✅ **Spells**, 4 `AbstractSpell`s (`spell/`) spawning the Companions projectiles; loot drop in
   `event/CompanionsLootEvents`.
3. ✅ **Damage retype + Blood weapons** (5D/5E), `event/CompanionDamageEvents` (re-deals under IS
   `<school>_magic` damage type).
4. ✅ **Armour bridge** (5F), `event/CompanionArmorEvents` (holy-robe +holy spell power).
5. **Ally/faction** (5G), dropped, see 5G.
6. ✅ **Removals**, `CompanionBooks` (the single list of nine ids + book → spell map) +
   `event/CompanionBookRemovalEvents` (recipe strip, Minion-reward swap) +
   `compat/IronsCompanionsJeiPlugin` (JEI hide).
7. ✅ **Soul Mage AI** (5C), `soulmage/SoulMageIronsSpellGoal` + `SoulMageAiEvents` inject an
   IS-scroll-casting goal (`EntityJoinLevelEvent` + reflection on `goalSelector`; **no mixin needed**).
   The old book goals go inert once the books are gone. Scrolls reach its slots via
   `mixin/SoulMageBookSlotMixin`.

## 8. Open items (need a running game, not a compiler)

- Naginata projectile damage (not in config; read `HolinessNaginataProjectile`). `baseSpellPower` is a
  placeholder 8.
- Exact Ice Tornado per-level damage curve, and whether looted scrolls should be tiered above level 1.
- Final companion→school mapping sign-off (5D).
- Whether the Holy Robe's holy spell power should render on the item tooltip: the modifier is added
  through `ItemAttributeModifierEvent` (the hook behind `ItemStack#getAttributeModifiers`, which is also
  what Apothic Attributes reads), and it applies, but no tooltip line appears yet.

## 9. References

- Companions source (GPL v3): <https://github.com/Xylonity/Companions>
- IS core schools: fire, ice, lightning, holy, ender, blood, evocation, void, **nature**, eldritch.
  Rarities: common, uncommon, rare, epic, **legendary**.
- Earth magic school id = `gtbcs_geomancy_plus:geo` (there is no "teramancy" school).
- Locked decisions: `.wolf/cerebrum.md` (Decision Log, 2026-07-27).
