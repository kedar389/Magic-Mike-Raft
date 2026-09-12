# Iron's Companions, Compatibility / Content Mod

> **Mod id:** `irons_companions` · **Loader:** Forge 1.20.1 (47.4.0)
>
> Bridges **Iron's Spells 'n Spellbooks** (`irons_spellbooks` 3.16.2) with the **Companions!** mod
> (`companions` 1.2.3, by Xylonity/ModderG, GPL v3, source: <https://github.com/Xylonity/Companions>).

---

## 1. Purpose

Companions ships its magic as consumable "magic books" with hardcoded damage, outside Iron's Spells (IS)
entirely. This mod puts everything Companions does that is magic onto IS's terms:

1. **Four magic books become real IS spells**: Ice Tornado (ice), Stone Spikes (geo), Heal Ring
   (nature), Naginata (holy). Each spawns Companions' own projectile entity, so the models, animations
   and sounds are the originals, but the hit is re-dealt through IS, so spell power, school resistance,
   `SpellDamageEvent`, mana and cooldowns all apply. Scrolls arrive through IS's own loot (5A, 6).
2. **The books go out of circulation**: the three converted ones and five more are stripped from
   recipes, chest loot, Minion handouts and JEI. Minions hand out a common scroll instead (5B).
3. **`book_naginata` stays, renamed to His Holiness Scripture**, a craftable IS spellbook on Companions'
   own recipe: 10 spell slots, Naginata locked at max level in the first, right-click equipped to the
   Curios spellbook slot, and the worn mana and spell-power bonus every IS unique spellbook carries
   (5A-iii).
4. **Companion ability damage gets a school**: Pontiff holy, Shade blood, Dinamo lightning, Ember Pole
   and the fire frogs fire, Ender Frog ender, plus the Pontiff's projectiles and both Crystallized Blood
   weapons. Same re-deal path as the spells, so the owner's spell power and the target's resistance both
   count (5D, 5E).
5. **Two armour sets bridge into IS attributes**: Holy Robe grants holy spell power per piece,
   Crystallized Blood grants blood magic resistance per piece (5F).
6. **Crystallized Blood runs on Blood Vials**: the Soul Furnace recipe takes an IS Blood Vial rather
   than rotten flesh, and IS recipes calling for a vial take Crystallized Blood, so the two mods' blood
   lines feed each other (6A).
7. **The Soul Mage casts IS spells** from scrolls in its book slots, in place of Companions' book goals:
   one every 1-2s, cycling its whole rotation, never a summon, and never without a clear line (5C).

Sibling of `mod_compats/OriginSpellSchools`, same build layout.

## 2. Build

```
cd mod_compats/IronsCompanions
# one-time: copy the deps named in build.gradle out of the live mods folder
cp "../../servers_folder/magic server/mods/"{companions-forge-1.20.1-1.2.3,gtbcs_spell_lib-1.5.0-1.20.1,gtbcs_geomancy_plus-2.0.0-1.20.1,geckolib-forge-1.20.1-4.8.4,knightlib-forge-1.20.1-1.6.1,curios-forge-5.14.1+1.20.1}.jar libs/
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.20.8-hotspot" ./gradlew build --no-daemon
```

Produces `build/libs/irons_companions-0.1.0.jar`. Gradle 8.1.1 wrapper, Forge **1.20.1-47.4.0** to
match the live server. `libs/*.jar` is gitignored.

`build` also runs **Checkstyle** (`config/checkstyle/checkstyle.xml`, shared with the OriginSpellSchools
sibling, `maxWarnings = 0`) and **Error Prone** (2.42.0, the last release that runs on JDK 17). Both are
tuned for bugs rather than layout, so the build fails on a real defect and stays quiet about formatting:

- `ReferenceEquality` is disabled: Minecraft compares entities and items by identity everywhere, and
  `Entity#equals` compares by network id, which is the surprising choice here rather than the safe one.
- The four mixins carry `@SuppressWarnings({"UnusedMethod", "UnusedVariable"})`: injectors are called by
  injected bytecode and their signatures are fixed by the target method, so unused handlers and unread
  parameters are expected there. Suppressed per class, so both checks stay live everywhere else.

The six remaining `[removal]` warnings are Forge/Mojang APIs with no 1.20.1 replacement
(`new ResourceLocation(String, String)`, `FMLJavaModLoadingContext.get()`).

## 3. Dependencies (mods.toml)

| Mod | Why | Required |
|---|---|---|
| `irons_spellbooks` (3.16.2) | Spell registry, `AbstractSpell`, SchoolType, events, Scroll item | ✅ after |
| `companions` (1.2.3) | Reuse its projectile entities + retype its ability damage | ✅ after |
| `gtbcs_spell_lib` (1.5.0) | Helper API the IS addons build on (spell damage/crit attributes) | ✅ |
| `gtbcs_geomancy_plus` (2.0.0) | Owns the **`geo`** school used by Stone Spikes | ✅ after |
| `apothic_attributes` / `attributeslib` | Spell-power / resist attributes for armour bridges | ✅ |
| `curios` | Spellbook slot; `ICurioItem` for the Scripture's right-click equip, `CurioAttributeModifierEvent` for its worn bonus | ✅ after |
| `jei` (15.20.0.112) | Hides the removed magic books from the item list (API only, client) | optional |

## 4. Architecture

One Forge mod. Each package owns one bridge, and `IronsCompanions` does nothing but register them; every
handler is stateless, so the classes themselves are registered rather than instances of them.

| Package | Owns |
|---|---|
| *(root)* | `IronsCompanions` (entry point), `ModIds` (every other mod's namespace and its id factories), `ItemIds` (the ids named by more than one feature, and the one deferred way to resolve an id to an `Item`) |
| `spell` | the four converted spells, `CompanionsProjectiles` (spawns Companions' own entities), `SpellTag` (which spell fired a projectile), `NaginataShockwave`, `SpellScrolls` |
| `damage` | `CompanionsDamageBridge` (one `LivingAttackEvent` handler, two jobs) and `CompanionsSchools` (the entity → school table) |
| `attribute` | `CompanionArmorBonuses` and `NaginataBookBonuses`, both built on `AttributeBonus` |
| `books` | `RemovedBooks` (the one list of eight ids), `RemovedBookDrops`, `RemovedBookLoot` |
| `recipe` | `RecipeRewrites` (one reload listener, one sweep) and its `RecipeRule`s: `RemovedBookRecipes`, `BloodChainRecipes` |
| `soulmage` | `SoulMageSpellcasting` (injects the goals), `SoulMageIronsSpellGoal`, `SoulMageLineUpGoal`, `SummonSpells` |
| `mixin` | the four mixins; must stay one package, that is what `irons_companions.mixins.json` names, which lists `ImbuedSpellTooltipMixin` under `client` |
| `client` | `NaginataBookTooltip` (the Scripture's spellbook tooltip) and `SoulMageScreenNote`, both subscribed through `@Mod.EventBusSubscriber(Dist.CLIENT)` |
| `client` | `NaginataBookTooltip` (the Scripture's spellbook tooltip), subscribed through `@Mod.EventBusSubscriber(Dist.CLIENT)` |
| `compat` | `IronsCompanionsJeiPlugin`, loaded only by JEI's own plugin scan |

Four things are worth knowing before changing any of it:

- **Spells reuse Companions' projectile entities** rather than porting assets, so the visuals are
  pixel-identical. Each ships a 16x16 icon at
  `assets/irons_companions/textures/gui/spell_icons/<spell>.png`, the path `AbstractSpell` builds from the
  spell id; IS draws it on the spell bar, the wheel and the scroll.
- **Four mixins**, each where no event reaches. Three are into Companions classes: `SoulMageBookSlotMixin`
  widens the Soul Mage's book slots (`mayPlace` is `instanceof AbstractMagicBook`) to accept IS spell
  containers, `HolinessNaginataMixin` hooks the naginata's own hit methods to add the impact shockwave,
  and `NaginataBookSpellbookMixin` turns `book_naginata` into His Holiness Scripture. The fourth,
  `ImbuedSpellTooltipMixin`, is into Iron's Spells and client-only: it keeps the imbued-weapon tooltip
  off the Scripture, which IS picks by item class with no way to opt out (5A-iii). The Soul Mage AI
  itself needs no mixin (see phase 7).
- **Every recipe change goes through one sweep.** `RecipeRewrites` walks the loaded `RecipeManager` once
  per reload and chains the rules per recipe, instead of one listener per feature each walking several
  thousand recipes and rebuilding the type index. All recipe changes live in this mod; there is no
  companion datapack to keep in sync.
- **Registry lookups are resolved once.** Ids are what the source declares, because that is what can be
  checked against the other mod's jar, but every table is resolved through `Lazy.concurrentOf` on first
  use into `EntityType`/`Item` keys and compared by identity from then on. That matters because
  `CompanionsDamageBridge` sees every hit dealt in the world and `CompanionArmorBonuses` sees every
  `getAttributeModifiers` query. For the same reason `SpellTag` checks the entity *type* before touching
  `getPersistentData`, which allocates a saved NBT compound on first call for whatever it is asked about.

> **Item removal is external, not a fork.** Removed books are stripped of recipes/loot and JEI-hidden; their
> registry entries technically remain but become unobtainable. (Decision 2026-07-27.)

---

## 5. Feature spec

### 5A. Magic Books → Iron's Spells

All conversions use **Option B (exact recreation)**: a new IS spell whose `onCast` spawns the original
Companions projectile entity, owned by the caster, with damage retyped to the listed IS school and scaled by
the caster's spell power. Every one is available as a **Scroll** and **inscribable into a spellbook**.

| Book (removed) | Spell | School | Cast | Effect | Sound |
|---|---|---|---|---|---|
| `book_ice_tornado` | Ice Tornado | **ice** | instant, cd 10s | 1 tornado a block ahead | `SPELL_RELEASE_TORNADO` |
| `book_stone_spikes` | Stone Spikes | **geo** (Geomancy Plus) | 30t channel, cd 7s | 10 spikes in a 60° forward cone, ground-snapped | school default |
| `book_heal_ring` | Heal Ring | **nature** | 25t channel, cd 10s | ring at the caster | `SPELL_RELEASE_HEAL` |
| `book_naginata` (kept, becomes His Holiness Scripture) | Naginata | **holy** | 45t channel, cd 7s | 8 blades rain on the aimed point, each landing with a shockwave | `SPELL_RELEASE_SPEARS` |

**Damage goes through Iron's Spells, not the Companions config.** `BaseProjectile` exposes no damage
setter and each projectile hardcodes its own damage, so `SpellTag` marks the entity with the
firing spell and the scroll's level (`irons_companions_spell` / `_spell_level` in its persistent data).
`CompanionsDamageBridge` then cancels the projectile's own hit and re-deals it with
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

**Heal Ring is the exception.** `HealRingProjectile` reads `CompanionsConfig.HEAL_RING_HEALING` itself
and calls `heal` on everything in range, so there is no hit to intercept and the amount does not follow
spell power or spell level. `HealRingSpell#getUniqueInfo` therefore reports that config value rather than
`getSpellPower`, so the tooltip matches what the ring applies.

**5A-i · Ice Tornado tiers.** IS spells are level-scalable (1–N). Model "different tiers" as one spell whose
damage scales per level off the 3.0 base, distributed as scrolls at **Uncommon → Rare → Epic** (higher tier =
higher inscribed level). Exact level curve tunable at build time.

**5A-ii · Assets are reused, not copied.** Because we spawn the original entities, their GeckoLib
models/animations/textures/sounds (e.g. `ice_tornado.geo.json`, `stone_spike0-6.png`,
`spell_release_tornado.ogg`) render unchanged. If a projectile constructor isn't public, add an Access
Transformer rather than copying assets.

### 5A-iii · His Holiness Scripture, a craftable spellbook

`companions:book_naginata` is the one book that stays. `mixin/NaginataBookSpellbookMixin` renames it to
**His Holiness Scripture** and makes it an IS spellbook: **10 spell slots**, the first holding **Naginata**
locked at the spell's max level, the other nine free to scribe. It keeps **Companions' own recipe** (four
Relic Gold in a plus around a book, `companions:recipes/book_naginata.json`), so it is absent from
`books/RemovedBooks`, which is what keeps the recipe strip, the Minion swap and the JEI hide off it.

How a Companions item can be a spellbook at all, given IS's spellbooks are its own item class:

- **Carrying the spell.** Implementing `IPresetSpellContainer` is enough. IS's own `ItemStackMixin` calls
  `initializeSpellContainer` on every `ItemStack` built from an item that implements it, so the crafted
  book, the creative copy and the JEI entry all come with the spell already inscribed, with no recipe or
  crafting event of ours. The level is read from `AbstractSpell#getMaxLevel`, so it follows the spell
  config rather than a number hardcoded here.
- **Casting from it.** Everything downstream is NBT-driven, not item-class driven.
  `SpellSelectionManager.initItem` takes any stack where `ISpellContainer.isSpellContainer` holds. The
  container is created with the same flags as a stock spellbook (`spellWheel` and `mustEquip` both true),
  so the book is used from the Curios **spellbook** slot, shows on the spell wheel, and casts through
  `CastSource.SPELLBOOK` with normal mana and cooldown. `ISpellbook` is a bare marker interface that
  nothing else reads; the mixin implements it anyway.
- **The name.** The mixin overrides `Item#getName`, which Companions' `TooltipItem` declares and
  `NaginataBook` does not, so it merges in as an override and renames the item everywhere: inventory,
  creative tab, JEI, anvil. A lang override of `item.companions.book_naginata` would instead depend on
  this mod's resource pack being read after Companions', and would fail silently if it were not.
- **Equipping it.** Right-click equips it into the spellbook slot, and only that slot: the mixin
  implements Curios' `ICurioItem` with `canEquipFromUse`, which is both what gives the item a curio
  capability (Curios attaches one to every stack whose item implements the interface) and what its
  `RightClickItem` handler checks. `data/curios/tags/items/spellbook.json` in this mod is the only slot
  tag the book is in, and Curios routes items by that tag alone. The equip sound is IS's own
  `EQUIP_SPELL_BOOK`. Curios is therefore a **required** dependency, not an optional one.
- **Its old right-click ability is gone.** `NaginataBook.use` spawned Companions' naginatas directly, on
  an item cooldown, with damage hardcoded in the projectile. Left alone that would be a free duplicate of
  the spell with no mana cost and no school, so the mixin cancels it at HEAD. That path is only reached
  when the spellbook slot is already full: `RightClickItem` fires first, and equipping cancels it.
- **It does not offer imbuement.** `Utils.canImbue` is true for any spell container that is neither a
  `Scroll` nor a `SpellBook`, which put a "Can be Imbued (1/1)" line on the book that no IS spellbook
  shows. Its first check is `item instanceof UniqueItem`, an empty marker interface, so the mixin
  implements that too.
- **Worn bonus.** `attribute/NaginataBookBonuses` grants **+200 max mana** and **+10% holy spell power**
  in the spellbook slot, the same pair every IS themed and unique spellbook carries
  (`SpellBook.withSpellbookAttributes`). IS attaches those through `CurioBaseItem`, which a Companions
  item is not, so they go on through `CurioAttributeModifierEvent`: Curios posts it for every stack in a
  curio slot regardless of `ICurio` capability, and returns `event.getModifiers()`.
- **Tooltip.** The book reads like a stock spellbook: the "Unique Spell Book" heading, its slot count, the
  cast prompt, and its inscribed spells listed by name and level, with nothing about damage, mana or
  cooldown. That block is what `SpellBook#appendHoverText` writes, rebuilt in
  `client/NaginataBookTooltip` because the item cannot inherit it, and inserted after the item name so it
  lands where a spellbook's does. Two other blocks are taken off the tooltip to make room: Companions'
  two lines about the old right-click ability, dropped by translation key, and IS's imbued-weapon block,
  which is the one that prints per-spell damage. IS picks that block by item class
  (`isSpellContainer && !(item instanceof SpellBook)`) with no way to opt out, so
  `mixin/ImbuedSpellTooltipMixin` cancels its private handler for this one item.

The spell is not lootable (`NaginataSpell#allowLooting()` is false), so the crafted book is its only
source.

### 5B. Removed books (unobtainable + JEI-hidden)

`book_ice_shard`, `book_magic_ray`, `book_fire_mark`, `book_brace`, `book_black_hole`, **and** the three
converted books above (players get the IS spell instead of the book).

**How Companions handed the books out** (read from the 1.2.3 jar, 2026-08-03). Recorded because it is
what the removal has to cover, not because the replacements copy it: the scrolls use IS's own
distribution instead (section 6).

| Source | Books |
|---|---|
| Crafting recipe (`recipes/book_naginata.json`, 4x relic gold + book), the only book recipe in the mod | naginata, kept (5A-iii) |
| Chest loot, `CompanionsLootTables#onLootTableLoad` appends a `coin_pool` to every `minecraft:chests/*` table, 4.5% each (the coin in that pool is stock content and is preserved) | black_hole, magic_ray |
| Minion reward, `MinionEntity#mobInteract` pops one via `Block.popResource` | ice_shard, ice_tornado, brace, fire_mark, heal_ring, stone_spikes |

Removal steps, all inside this mod (`books/RemovedBooks` holds the one list of eight ids):
- `recipe/RemovedBookRecipes`, a rule in the recipe sweep (section 4) that drops every recipe whose
  output is one of the books. Matching on output catches the recipe whatever its path or type, and it
  re-runs on `/reload`.
- `books/RemovedBookDrops`, catches the Minion's popped reward (and any legacy book a player drops) on
  `EntityJoinLevelEvent` and swaps the stack for a random common scroll. No recipe strip can reach
  `popResource`, so this is the only way to close that source.
- `books/RemovedBookLoot`, removes Companions' `coin_pool` from every
  `minecraft:chests/*` table and re-adds a coin-only copy. That pool is where `book_black_hole` and
  `book_magic_ray` enter chest loot at 4.5% each, and a pool's entries are a final package-private
  array, so the books cannot be filtered out of it in place. Runs at `LOWEST` priority, after
  Companions has added the pool.
- `compat/IronsCompanionsJeiPlugin`, a `@JeiPlugin` calling `removeIngredientsAtRuntime` so the books
  vanish from the item list. Client only, loaded by JEI's own plugin scan.

### 5C. Soul Mage casts Iron's Spells

The Soul Mage casts real IS spells from whatever scrolls and spellbooks sit in its three book slots,
which `mixin/SoulMageBookSlotMixin` widens to accept them. `soulmage/SoulMageSpellcasting` adds two goals
to every Soul Mage as it joins the world, both at **priority 3**, where Companions puts its own book
goals: `SoulMageIronsSpellGoal` casts through `CastSource.MOB` with a throwaway `MagicData`, since a mob
has no mana pool of its own, and `SoulMageLineUpGoal` walks the mage into a clear line on its target.

Three rules shape what comes out:

| Rule | Why | Where |
|---|---|---|
| **No summons** | a mage that fills the field with minions of its own is not what the slots are for | `soulmage/SummonSpells` |
| **Every spell before any repeat** | picking uniformly at random lands on the same scroll for casts on end | `SoulMageIronsSpellGoal#pickSpell` |
| **Never without a clear line** | spells leave along the mage's look vector, so cover ate the cast | `SoulMageIronsSpellGoal#canSeeLivingTarget` |

**Cadence** is 8-28 ticks between casts plus the goal's own 12-tick cast: a spell every 1-2 seconds.

**Identifying a summon** takes a list, because Iron's Spells marks them in no way: every summon spell is a
plain `AbstractSpell`, and the machinery they share (`SummonManager`, `IMagicSummon`) is only reached at
cast time. The list in `SummonSpells` comes from a bytecode scan of every spell mod in the pack for
references to that machinery, which found `raise_dead`, `summon_vex`, `summon_horse`, `summon_polar_bear`
and `summon_swords` in Iron's Spells, and `solar_chorus` in Geomancy Plus. That last one is why the list
is explicit rather than a name match: it summons two Umvuthana but is not called `summon_` anything.
`summon_ender_chest` is on the list too, summoning no minion but being no use to a mob either. Name hints
(`summon`, `conjure`, `minion`, `raise_dead`) cover what gets added later.

**Lining up** exists because Companions gives the mage no goal that approaches a target at all: only
follow-owner and stroll, both at priority 4. Without it, a target that stepped behind a tree would end the
fight, since the casting goal will not fire without line of sight. It chases up to 24 blocks and stops the
moment the line is clear. Holding `Flag.MOVE` is also what keeps a sitting mage seated, since vanilla's
`SitWhenOrderedToGoal` holds the same flag at priority 1.

The player-facing note lives in `client/SoulMageScreenNote`, which writes "Summoning spells will not be
cast" under Companions' own book screen through `ScreenEvent.Render.Post`.
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

Both bridges live in `attribute/CompanionArmorBonuses`, one table keyed by item, applied through
`ItemAttributeModifierEvent` so vanilla adds and removes the modifier with the piece. Both use
`MULTIPLY_BASE` on an IS `MagicPercentAttribute` whose base is exactly 1.0, which sums like `ADDITION`
but renders as a percentage in the tooltip.

- **Holy Robe set** (`holy_robe_mask`/`coat`/`leggings`): **+10% holy spell power per piece**, +30% on the
  full three-piece set.
- **Crystallized Blood set** (`helmet`/`chestplate`/`leggings`/`boots`): **+5% blood magic resistance per
  piece** (`irons_spellbooks:blood_magic_resist`), +20% on the full four-piece set. IS resistance is a
  straight multiplier, `DamageSources.getResist` takes `2 - softCapFormula(value)` and `softCapFormula` is
  the identity up to 1.5, so the full set means blood magic lands at 80% damage. The set's own low-health
  damage reduction is untouched.
- **Mage set**: untouched, exactly as Companions ships it.

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
- **Naginata** → not looted at all. It comes from the crafted `book_naginata` spellbook (5A-iii), on
  Companions' own recipe. `NaginataSpell` overrides `allowLooting()` to **false**, which is what keeps the
  legendary out of the generic scroll rolls, so the book is its only source.
- **Remove recipes** for the eight removed `companions:book_*` → `recipe/RemovedBookRecipes`.
- **Swap Minion book rewards** for a random **common** scroll → `books/RemovedBookDrops`. Not a
  1:1 book-to-its-spell swap: Companions rolls a fixed pair per Minion variant (overworld = ice_shard or
  ice_tornado, 50/50), so 1:1 handed out the same scroll half the time.
- **JEI-hide** the eight removed `companions:book_*` → `compat/IronsCompanionsJeiPlugin`, which reads the
  same `books/RemovedBooks` list, so `book_naginata` stays visible with its recipe.

### 6A. Crystallized Blood runs on Blood Vials

`recipe/BloodChainRecipes` is a rule in the recipe sweep (section 4). It rewrites two things in the
loaded `RecipeManager`:

| Recipe | Before | After |
|---|---|---|
| Soul Furnace, whichever recipe outputs `companions:crystallized_blood` | 1 Rotten Flesh | 1 `irons_spellbooks:blood_vial` |
| Every `irons_spellbooks` shaped/shapeless recipe asking for a Blood Vial (today: Netherite Spell Book, 2 of them) | Blood Vial | `companions:crystallized_blood` |

Charge cost and process time are read off the existing Soul Furnace recipe, so only the input changes.

That Soul Furnace recipe is Crystallized Blood's **only** source (nothing in Companions drops it, no loot
table contains it), so the whole Crystallized Blood line now runs on Blood Vials: the armour and both
weapons, the Shade Sword and Shade Maw altars, the Shadow Bell, and the +5%/piece blood resistance bridge
in 5F. Blood Vials themselves are unaffected, the alchemist cauldron fill/empty pair and Create's filling
variant are neither shaped nor shapeless, so the way to get one is untouched and the chain holds.

**Why code rather than a recipe file.** Overriding another mod's recipe id from our own datapack depends on
mod resource-pack ordering, and a silent miss would leave the change off with nothing to show it. Editing
the loaded recipes is order-independent, and expressing the swap as a rule also catches any blood-vial
recipe a later Iron's Spells version adds. Ingredients that merely *contain* the vial through a tag (like
`irons_spellbooks:blood_focus`) are left alone.

## 7. Implementation phases

1. ✅ **Scaffold**, `build.gradle`, `mods.toml`, main class (mirrors OriginSpellSchools). Uses local
   deobf jars from `libs/`.
2. ✅ **Spells**, 4 `AbstractSpell`s (`spell/`) spawning the Companions projectiles; Naginata reached
   through the crafted spellbook (`mixin/NaginataBookSpellbookMixin`), the rest through IS's own scroll
   loot.
3. ✅ **Damage retype + Blood weapons** (5D/5E), `damage/CompanionsDamageBridge` (re-deals under IS
   `<school>_magic` damage type).
4. ✅ **Armour bridges** (5F), `attribute/CompanionArmorBonuses` (holy robe +holy spell power, Crystallized
   Blood +blood resistance).
5. **Ally/faction** (5G), dropped, see 5G.
6. ✅ **Removals**, `books/RemovedBooks` (the single list of eight ids) + `recipe/RemovedBookRecipes`
   (recipe strip), `books/RemovedBookDrops` (Minion-reward swap), `books/RemovedBookLoot` (chest loot)
   and `compat/IronsCompanionsJeiPlugin` (JEI hide). Plus `recipe/BloodChainRecipes` for the blood-vial
   chain (6A).
7. ✅ **Soul Mage AI** (5C), `soulmage/SoulMageSpellcasting` adds `SoulMageIronsSpellGoal` and
   `SoulMageLineUpGoal` (`EntityJoinLevelEvent` + reflection on `goalSelector`; **no mixin needed**).
   The old book goals go inert once the books are gone. Scrolls reach its slots via
   `mixin/SoulMageBookSlotMixin`, and `soulmage/SummonSpells` keeps summons out of the rotation.

## 8. Open items (need a running game, not a compiler)

- Exact Ice Tornado per-level damage curve, and whether looted scrolls should be tiered above level 1.
- Final companion→school mapping sign-off (5D).
- A Soul Mage handed a `book_naginata` still runs Companions' own naginata goal, at the projectile's
  hardcoded damage. Only the player's right-click was cancelled; decide whether the mob path should be
  retyped too.
- Heal Ring heals a flat `CompanionsConfig.HEAL_RING_HEALING` and ignores nature spell power, unlike the
  three damage spells. Making it scale means intercepting the projectile's own `heal` call with a mixin.

## 9. TODO

### 9C. Further books worth converting

The five cut books are the cheapest candidates: their projectiles, models, animations and sounds all still
exist in the jar, and their Soul Mage goals prove each effect works on its own.

| Book | Effect entities | Suggested school |
|---|---|---|
| `book_ice_shard` | `small_ice_shard_projectile`, `big_ice_shard_projectile` | ice |
| `book_magic_ray` | `magic_ray_circle_projectile`, `magic_ray_piece_projectile` | evocation |
| `book_fire_mark` | `fire_mark_projectile`, `fire_mark_ring_projectile` | fire |
| `book_black_hole` | `black_hole_projectile` | ender or eldritch |
| `book_brace` | `brace_projectile` | evocation (it is protective, not offensive) |

Beyond the books, these abilities already have their own projectiles and could become spells:
`pontiff_fire_ring` and `holiness_star` (holy), `blood_slash_projectile` and `blood_tornado_projectile`
(blood, currently just retyped weapon abilities), `antlion_sand_projectile`, `needle_projectile`,
`stake_projectile`, `fire_geiser_projectile`, `laser` / `laser_ring`, `frog_heal_projectile`,
`frog_levitate_projectile`, `frog_egg_projectile`, `respawn_totem_ring_projectile`, and the
`blue_star_explosion` / `red_star_explosion` pair.

### 9D. Frog Bonanza is not a spell candidate

`companions:frog_bonanza_block` is a block, not an ability: a GeckoLib block entity whose inner classes are
`BonanzaTnt`, `SkullDropEntities` and `TeddyDrop`, crafted from stone bricks and planks, tooltip "Frog
bonanza why not". No projectile, no targeted effect, nothing to convert. Left alone.

## 10. References

- Companions source (GPL v3): <https://github.com/Xylonity/Companions>
- IS core schools: fire, ice, lightning, holy, ender, blood, evocation, void, **nature**, eldritch.
  Rarities: common, uncommon, rare, epic, **legendary**.
- Earth magic school id = `gtbcs_geomancy_plus:geo` (there is no "teramancy" school).
- Locked decisions: `.wolf/cerebrum.md` (Decision Log, 2026-07-27).
