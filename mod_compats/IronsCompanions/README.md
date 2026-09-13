# Iron's Companions

Forge 1.20.1 compat mod (`irons_companions`) bridging **Iron's Spells 'n Spellbooks** (`irons_spellbooks`
3.16.2) and **Companions!** (`companions` 1.2.3, GPL v3, <https://github.com/Xylonity/Companions>).

Companions ships its magic as consumable books with hardcoded damage, outside Iron's Spells. This mod puts
all of it on IS's terms.

## 1. What it does

- **Four magic books become IS spells** — Ice Tornado (ice), Stone Spikes (geo), Heal Ring (nature),
  Naginata (holy). Each spawns Companions' own projectile, so visuals are unchanged, but damage and healing
  go through IS (5A).
- **The books leave circulation** — eight ids stripped from recipes, chest loot, Minion rewards and JEI.
  Minions hand out a scroll instead (5B).
- **`book_naginata` stays as His Holiness Scripture** — a craftable IS spellbook, 10 slots, Naginata locked
  in the first (5A-i).
- **Companion ability damage gets a school** — Pontiff holy, Shade blood, Dinamo lightning, and so on (5D, 5E).
- **Three armour sets grant IS attributes** — Holy Robe, Crystallized Blood, Mage (5F).
- **Crystallized Blood runs on Blood Vials**, and IS recipes calling for a vial take Crystallized Blood (6A).
- **The Soul Mage casts IS spells** from scrolls in its book slots (5C).
- **The Mage Staff casts like an IS staff** and carries spell power, summon damage and mana (5H).

Sibling of `mod_compats/OriginSpellSchools`, same build layout.

## 2. Build

```
cd mod_compats/IronsCompanions
# one-time: copy the deps named in build.gradle out of the live mods folder
cp "../../servers_folder/magic server/mods/"{companions-forge-1.20.1-1.2.3,gtbcs_spell_lib-1.5.0-1.20.1,gtbcs_geomancy_plus-2.0.0-1.20.1,geckolib-forge-1.20.1-4.8.4,knightlib-forge-1.20.1-1.6.1,curios-forge-5.14.1+1.20.1}.jar libs/
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.20.8-hotspot" ./gradlew build --no-daemon
```

Produces `build/libs/irons_companions-0.1.0.jar`. Gradle 8.1.1, Forge 1.20.1-47.4.0 to match the live
server. `libs/*.jar` is gitignored.

`build` also runs Checkstyle (`config/checkstyle/checkstyle.xml`, `maxWarnings = 0`) and Error Prone 2.42.0,
the last release that runs on JDK 17. Both are tuned for bugs, not layout.

- `ReferenceEquality` is off: Minecraft compares entities and items by identity, and `Entity#equals`
  compares by network id.
- The four injector mixins carry `@SuppressWarnings({"UnusedMethod", "UnusedVariable"})` — injector
  signatures are fixed by the target method.
- Six `[removal]` warnings remain, all Forge/Mojang APIs with no 1.20.1 replacement
  (`new ResourceLocation(String, String)`, `FMLJavaModLoadingContext.get()`).

## 3. Dependencies

| Mod | Why | Required |
|---|---|---|
| `irons_spellbooks` 3.16.2 | Spell registry, `AbstractSpell`, schools, scrolls | yes, after |
| `companions` 1.2.3 | Its projectile entities and ability damage | yes, after |
| `gtbcs_spell_lib` 1.5.0 | Helper API the IS addons build on | yes |
| `gtbcs_geomancy_plus` 2.0.0 | Owns the `geo` school used by Stone Spikes | yes, after |
| `apothic_attributes` | Spell power / resist attributes | yes |
| `curios` 5.14+ | Spellbook slot, `ICurioItem`, worn bonuses | yes, after |
| `jei` 15.20 | Hides the removed books (client, API only) | optional |

## 4. Architecture

One Forge mod. Each package owns one bridge; `IronsCompanions` only registers them. Handlers are stateless,
so classes are registered rather than instances.

| Package | Owns |
|---|---|
| *(root)* | `IronsCompanions`, `ModIds` (other mods' namespaces), `ItemIds` (shared ids, deferred id → `Item`) |
| `spell` | the four spells, `CompanionsProjectiles`, `SpellTag`, `NaginataShockwave`, `SpellScrolls` |
| `damage` | `CompanionsDamageBridge` (one `LivingAttackEvent` handler), `CompanionsSchools` (entity → school) |
| `attribute` | `CompanionArmorBonuses`, `NaginataBookBonuses`, `MageStaffBonuses`, on `AttributeBonus` |
| `books` | `RemovedBooks` (the list of eight ids), `RemovedBookDrops`, `RemovedBookLoot` |
| `recipe` | `RecipeRewrites` (one sweep) and its rules: `RemovedBookRecipes`, `BloodChainRecipes` |
| `soulmage` | `SoulMageSpellcasting`, `SoulMageIronsSpellGoal`, `SoulMageLineUpGoal`, `SpellRoles`, `SummonSpells` |
| `staff` | `MageStaffCasting`, the delegate the staff's merged methods forward to |
| `mixin` | six mixins; must stay one package, that is what `irons_companions.mixins.json` names |
| `client` | `NaginataBookTooltip`, `SoulMageScreenNote` (`Dist.CLIENT` subscribers) |
| `compat` | `IronsCompanionsJeiPlugin`, loaded by JEI's plugin scan |

Four things to know before changing any of it:

**Spells reuse Companions' entities** rather than porting assets, so visuals are pixel-identical. Each spell
ships a 16x16 icon at `assets/irons_companions/textures/gui/spell_icons/<spell>.png`, the path
`AbstractSpell` derives from the spell id.

**Six mixins**, each where no event reaches:

| Mixin | Target | Does |
|---|---|---|
| `SoulMageBookSlotMixin` | Companions | widens the Soul Mage's book slots to accept IS spell containers |
| `HolinessNaginataMixin` | Companions | adds the naginata's impact shockwave |
| `NaginataBookSpellbookMixin` | Companions | turns `book_naginata` into His Holiness Scripture |
| `MageStaffCastingMixin` | Companions | makes the Mage Staff cast like an IS staff |
| `HealRingScalingMixin` | Companions | scales the heal ring with the scroll that cast it |
| `ImbuedSpellTooltipMixin` | Iron's Spells | keeps the imbued-weapon tooltip off the Scripture (client only) |

All are written with SRG method names and `remap = false`, because the targets are mod classes compiled
that way and this mod cannot be tested in dev (8).

**Every recipe change goes through one sweep.** `RecipeRewrites` walks the loaded `RecipeManager` once per
reload and chains the rules, instead of one listener per feature. There is no companion datapack.

**Registry lookups resolve once.** Ids are declared as source strings, then resolved through
`Lazy.concurrentOf` into `EntityType`/`Item` keys and compared by identity. `CompanionsDamageBridge` sees
every hit in the world and `CompanionArmorBonuses` every `getAttributeModifiers` call. For the same reason
`SpellTag` checks the entity type before touching `getPersistentData`, which allocates saved NBT on first
call.

Removed books keep their registry entries and are made unobtainable rather than forked out
(decision 2026-07-27).

## 5. Features

### 5A. Books → spells

Each spell's `onCast` spawns the original Companions projectile, owned by the caster, retyped to an IS
school. All four are inscribable; three are lootable as scrolls (6).

| Book | Spell | School | Cast | Power | Effect |
|---|---|---|---|---|---|
| `book_ice_tornado` | Ice Tornado | ice | instant, 10s | 3 +1/lvl | one tornado a block ahead |
| `book_stone_spikes` | Stone Spikes | geo | 30t, 7s | 4 +1/lvl | 10 spikes in a 60° cone, ground-snapped |
| `book_heal_ring` | Heal Ring | nature | 25t, 20s | 4 +1/lvl | heals in a ring at the caster |
| `book_naginata` | Naginata | holy | 45t, 7s | 8 +1/lvl | 8 blades rain on the aimed point, each with a shockwave |

**Damage goes through IS, not the Companions config.** `BaseProjectile` has no damage setter and each
projectile hardcodes its own, so `SpellTag` marks the entity with the firing spell and level
(`irons_companions_spell` / `_spell_level` in persistent data). `CompanionsDamageBridge` cancels the
projectile's hit and re-deals it with `DamageSources.applyDamage(target, spell.getSpellPower(level, caster),
spell.getDamageSource(projectile, caster))` — the call stock IS spells make. That buys spell power scaling,
school resistance, `SpellDamageEvent`, knockback, friendly fire and the spell's death message.

**Healing goes the same way.** `HealRingProjectile` reads `CompanionsConfig.HEAL_RING_HEALING` twice per
tick, to heal the living and hurt the undead. `HealRingScalingMixin` redirects that field read: a ring
carrying a `SpellTag` answers with `getSpellPower`, one cast by a Companions mob keeps the config amount.

Mana and cooldown apply only when cast from a spellbook — stock IS behaviour, since
`CastSource.consumesMana()` is true only for `SPELLBOOK` and `SWORD`. A scroll is consumed instead.

Assets are reused, never copied. If a projectile constructor is not public, add an Access Transformer
rather than copying the model.

### 5A-i. His Holiness Scripture

`NaginataBookSpellbookMixin` renames `book_naginata` and makes it an IS spellbook: 10 slots, Naginata
locked in the first at the spell's max level, nine free. Companions' own recipe (four Relic Gold around a
book) is unchanged, which is why the book is absent from `RemovedBooks`.

How a Companions item can be a spellbook:

| Concern | How |
|---|---|
| Carrying the spell | `IPresetSpellContainer`; IS's `ItemStackMixin` calls `initializeSpellContainer` on every stack built from such an item, so crafted, creative and JEI copies all arrive inscribed |
| Casting | everything downstream is NBT-driven — `SpellSelectionManager` takes any stack where `ISpellContainer.isSpellContainer` holds. Flags match a stock book (`spellWheel`, `mustEquip`), so it casts through `CastSource.SPELLBOOK` |
| The name | overrides `Item#getName`, which `TooltipItem` declares and `NaginataBook` does not. A lang override would depend on pack order and fail silently |
| Equipping | implements Curios' `ICurioItem` + `canEquipFromUse`; `data/curios/tags/items/spellbook.json` is the only slot tag it is in. Hence Curios is required, not optional |
| Old right-click | `NaginataBook.use` cancelled at HEAD — it spawned naginatas free of mana and school. Only reached when the spellbook slot is full |
| No imbuement | implements `UniqueItem`, the first thing `Utils.canImbue` checks |
| Worn bonus | `NaginataBookBonuses`: +200 max mana, +10% holy spell power, matching `SpellBook.withSpellbookAttributes`, applied through `CurioAttributeModifierEvent` |
| Tooltip | `NaginataBookTooltip` rebuilds the block `SpellBook#appendHoverText` writes; `ImbuedSpellTooltipMixin` suppresses IS's imbued-weapon block, which IS picks by item class with no way to opt out |

`NaginataSpell#allowLooting()` is false, so the crafted book is the spell's only source.

### 5B. Removed books

`book_ice_shard`, `book_magic_ray`, `book_fire_mark`, `book_brace`, `book_black_hole`, plus the three
converted ones. `books/RemovedBooks` holds the list; four handlers close every source Companions has:

| Source in Companions | Closed by |
|---|---|
| Chest loot — `CompanionsLootTables` appends a `coin_pool` to every `minecraft:chests/*` table, 4.5% each | `RemovedBookLoot` removes that pool and re-adds a coin-only copy at `LOWEST` priority. A pool's entries are a final package-private array, so it cannot be filtered in place |
| Minion reward — `MinionEntity#mobInteract` pops a book via `Block.popResource` | `RemovedBookDrops` swaps the stack on `EntityJoinLevelEvent` for a random common scroll. No recipe strip can reach `popResource` |
| Crafting | `RemovedBookRecipes`, matching on output so it catches any path or type, re-run on `/reload` |
| JEI listing | `IronsCompanionsJeiPlugin`, `removeIngredientsAtRuntime` |

### 5C. Soul Mage casts IS spells

`SoulMageSpellcasting` adds two goals at priority 3, where Companions puts its own book goals:
`SoulMageIronsSpellGoal` casts the scrolls in the mage's three slots, `SoulMageLineUpGoal` walks it into a
clear line when it has none. Slots accept scrolls through `SoulMageBookSlotMixin`. Casts use
`CastSource.MOB` and a throwaway `MagicData`, since a mob has no mana pool.

What it casts, first match wins (`chooseSpell`):

| When | Casts |
|---|---|
| target is mid-cast | counterspell |
| hit for ≥20% max HP within 60t, and below 60% HP | escape spell |
| below 40% HP | self-heal |
| carrying a harmful effect | cleanse |
| ally below 60% HP within 8 blocks | ally heal, aimed at the worst of them if the spell takes a target |
| first cast against a new target | one buff |
| clear line to target | attack rotation |
| nothing to fight, sheep within 8 blocks | wololo |

Every spell in the pack has a role in `SpellRoles`; anything unlisted is an attack with reach. The rotation
skips summons, the wasted list, anything on cooldown, short-range spells beyond 8 blocks, and anything cast
this round. 8-28 ticks between casts.

The **wasted** list is spells that do nothing in a mob's hands: player utility (`portal`, `recall`,
`pocket_dimension`, `summon_ender_chest`, `touch_dig`, `spectral_hammer`, `planar_sight`, `throw`,
`gluttony`), travel a walking mob cannot use (`angel_wing`, `ascension`, `seismic_surf`), and buffs to
things it never does (`echoing_strikes` buffs non-magical attacks, `sacrifice` detonates summons it has
none of). `invisibility` is an escape instead — mobs lose aggro, and it breaks on the mage's next hit.

How it casts, by `CastType`:

| | |
|---|---|
| `INSTANT` | `onCast` after a 6t wind-up |
| `LONG` | charge for `getEffectiveCastTime`, `onCast` at the end |
| `CONTINUOUS` | `onCast` at the start, `onServerCastTick` every tick |

`LONG` and `CONTINUOUS` run through `MagicData` (`initiateCast` → `handleCastDuration` →
`onServerCastComplete`), aborted by `shouldAIStopCasting`. The mage strafes while channelling.

Constraints behind the design, all read from the IS jar:

- `RayOfSiphoningSpell.onCast` is empty; its work is in `onServerCastTick`. One `onCast` call is only
  correct for `INSTANT`.
- `HealSpell.onCast` heals the caster, so allies can only be healed by area spells or by the few that read
  a `TargetEntityCastData` (`blessing_of_life`, `haste`).
- `AbstractSpell` publishes no range. `SpellRoles.SHORT_RANGE` is judged from what each spell does.
- Cooldowns are the spell's own, times `COOLDOWN_SCALE`. IS tunes them for a player with a full book
  (Heal 30s, Blaze Storm 20s), so a mage carrying heavy scrolls will have quiet stretches.
- `WizardAttackGoal` cannot be reused: it takes an `IMagicEntity`.

### 5D. Companion damage → IS schools

`CompanionsSchools` maps 16 entity types to a school; `CompanionsDamageBridge` re-deals their hits under it,
so the owner's spell power and the target's resistance both count.

| Companion / projectile | School |
|---|---|
| Sacred Pontiff, `pontiff_fire_ring`, `holiness_star` | holy |
| Shade Sword, Shade Maw, `shade_sword_impact_projectile` | blood |
| Dinamo, Broken Dinamo, Illager Golem | lightning |
| Ember Pole, Living Candle, Firework Toad, Nether Bullfrog | fire |
| Ender Frog | ender |

Minion arms, Antlion sand, Croissant Dragon and Puppet stay physical.

### 5E. Crystallized Blood weapons

The Sword's Blood Slash and the Axe's Blood Vortex (`blood_slash_projectile`, `blood_tornado_projectile`)
re-deal as blood magic through the same bridge. The Scythe's lifesteal is untouched.

### 5F. Armour sets

`CompanionArmorBonuses` is one table keyed by item, applied through `ItemAttributeModifierEvent`, so vanilla
adds and drops each modifier with the piece. All use `MULTIPLY_BASE` on an IS `MagicPercentAttribute` whose
base is 1.0 — sums like `ADDITION`, renders as a percentage.

| Set | Per piece | Full set |
|---|---|---|
| Holy Robe (mask, coat, leggings) | +10% holy spell power | +30% |
| Crystallized Blood (helmet, chestplate, leggings, boots) | +5% blood magic resist | +20% |
| Mage (hat, coat, leggings) | +5% `spell_resist` | 15% off every school |

IS resistance is a straight multiplier: `DamageSources.getResist` takes `2 - softCapFormula(value)` and
`softCapFormula` is the identity up to 1.5, so the full Blood set puts blood magic at 80% damage. Each set's
own Companions bonus, low-health reduction or cooldown reduction, is untouched.

### 5G. Ally / faction

Not part of this mod. Friendly fire and ally targeting behave as the two mods ship them
(decision 2026-08-04).

### 5H. Mage Staff

`companions:mage_staff` casts and carries attributes the way IS's own six staves do.

`MageStaffCastingMixin` merges the five methods that make up IS's `CastingItem` into Companions'
`GenericGeckoItem`, each forwarding to `staff/MageStaffCasting`. Right-click casts the spell selected on the
equipped spellbook, holding channels, releasing ends the cast; mana, cooldowns, the spell wheel and the cast
animation all come from `AbstractSpell#attemptInitiateCast`. The mage staff is the only item registered
under `GenericGeckoItem`, so the mixin reaches nothing else. The delegate is a static field on a normal
class, because a mixin field initialiser has to be transplanted into the target's constructor and there is
no way to prove that worked in dev (7).

`MageStaffBonuses` adds what it carries in the main hand:

| | |
|---|---|
| Attack damage | 4 (a 3.0 `ADDITION` under `Item.BASE_ATTACK_DAMAGE_UUID`) |
| Attack speed | 1 (a -3.0 under `Item.BASE_ATTACK_SPEED_UUID`) |
| Spell power | +10% |
| Summon damage | +15% |
| Max mana | +50 |

`data/irons_spellbooks/tags/items/staff.json` adds it to `#irons_spellbooks:staff`, which the
`Obscure_Tooltips_Styles` pack filters on to label an item "Staff".

Every IS staff specialises in one school; this one has none, so its spell power raises every companion
ability the damage bridge re-deals, whatever school it carries. Summon damage is the headline at 15%
against the Blood Staff's 10%, and no other staff grants max mana. The damage and speed match the Artificer
Cane, putting the staff between the Graybeard and Ice Staffs. The recipe is Companions' own, repeatable and
not boss-gated.

Taming a Golden Allay into a Soul Mage still consumes a mage staff — that is an entity interaction, which
resolves before `Item#use`.

A merged `CastingItem` is not one, so IS's three `instanceof CastingItem` checks miss the staff:

| Check | Effect |
|---|---|
| `ManaBarOverlay#shouldShowManaBar` | the bar does not appear from holding the staff; it still appears whenever mana is below full |
| `ServerPlayerEvents#onLivingEquipmentChangeEvent` | the net that cancels a cast on equipment change does not fire. Hotbar switches still end the cast through vanilla `stopUsingItem`; dropping or inventory-moving mid-channel is uncovered |
| `Utils#canBeUpgraded` | no Arcane Anvil upgrades, where IS's own staves take three |

Each would need a mixin into an IS class to special-case one item id. None is taken.

## 6. Loot and recipes

- **Scrolls need no code.** IS appends scrolls through its `randomize_spell` loot function, and an
  unfiltered `SpellFilter` resolves to `SpellRegistry.getEnabledSpells()` kept by
  `isEnabled() && allowLooting()`, weighted by `getMinRarity()`. Registering the spell is enough: it appears
  wherever any scroll appears, at a rarity-appropriate weight. `allowLooting()` defaults to the school's
  flag, true for ice, nature and geo.
- **Naginata is not looted** (`allowLooting()` false). The crafted Scripture is its only source.
- **Minion rewards** become a random common scroll rather than the book's own spell: Companions rolls a
  fixed pair per variant (overworld = ice_shard or ice_tornado, 50/50), so a 1:1 swap would hand out the
  same scroll half the time.

### 6A. Crystallized Blood runs on Blood Vials

`BloodChainRecipes`, a rule in the recipe sweep, rewrites two things in the loaded `RecipeManager`:

| Recipe | Before | After |
|---|---|---|
| Soul Furnace, whichever recipe outputs `crystallized_blood` | 1 Rotten Flesh | 1 `irons_spellbooks:blood_vial` |
| Every IS shaped/shapeless recipe asking for a Blood Vial (today: Netherite Spell Book, 2 of them) | Blood Vial | `crystallized_blood` |

Charge cost and process time are read off the existing recipe, so only the input changes. That Soul Furnace
recipe is Crystallized Blood's only source, so the whole line now runs on Blood Vials: armour, both weapons,
the Shade altars, the Shadow Bell, and the resistance bridge in 5F. Blood Vials themselves are unaffected —
the cauldron fill/empty pair and Create's filling variant are neither shaped nor shapeless.

Done in code rather than a datapack because overriding another mod's recipe id depends on pack ordering and
fails silently. Ingredients that merely contain the vial through a tag (`blood_focus`) are left alone.

## 7. Open items

Nothing here can be smoke-tested in dev. The dev runtime is Mojang-mapped while IS ships mixins reobfuscated
to SRG names, so `mixins.irons_spellbooks.json:EntityMixin` cannot find `m_7307_` and the server dies before
mod loading. Testing means the real server, which is also why this mod's own mixins use SRG names.

- **Nothing has been run in-game.** The untested pieces are the `MagicData` cast lifecycle, the
  `HEAL_RING_HEALING` redirect, and `m_7626_` / `m_8119_` merging as overrides.
- **Balance is unverified**: Heal Ring at 4 +1/lvl on 20s, the Mage set at 5% a piece, and the Soul Mage's
  cadence, chase distance and full-length spell cooldowns.
- Ice Tornado per-level damage curve, and whether looted scrolls should tier above level 1.
- A Soul Mage handed a `book_naginata` still runs Companions' own naginata goal at hardcoded damage; only
  the player's right-click was cancelled.

## 8. Not done

The five cut books are the cheapest further conversions — projectiles, models, animations and sounds all
still ship in the jar, and their Soul Mage goals prove each effect works.

| Book | Entities | School |
|---|---|---|
| `book_ice_shard` | `small_ice_shard_projectile`, `big_ice_shard_projectile` | ice |
| `book_magic_ray` | `magic_ray_circle_projectile`, `magic_ray_piece_projectile` | evocation |
| `book_fire_mark` | `fire_mark_projectile`, `fire_mark_ring_projectile` | fire |
| `book_black_hole` | `black_hole_projectile` | ender or eldritch |
| `book_brace` | `brace_projectile` | evocation (protective) |

Other abilities with their own projectiles: `antlion_sand_projectile`, `needle_projectile`,
`stake_projectile`, `fire_geiser_projectile`, `laser` / `laser_ring`, `frog_heal_projectile`,
`frog_levitate_projectile`, `frog_egg_projectile`, `respawn_totem_ring_projectile`, and the
`blue_star_explosion` / `red_star_explosion` pair.

`companions:frog_bonanza_block` is not a candidate: a GeckoLib block entity crafted from stone bricks and
planks, with no projectile and no targeted effect.

## 9. References

- Companions source (GPL v3): <https://github.com/Xylonity/Companions>
- IS schools: fire, ice, lightning, holy, ender, blood, evocation, void, nature, eldritch.
  Rarities: common, uncommon, rare, epic, legendary.
- Earth magic is `gtbcs_geomancy_plus:geo`; there is no "teramancy" school.
- Locked decisions: `.wolf/cerebrum.md`.
