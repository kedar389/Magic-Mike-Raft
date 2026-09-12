# Technical Notes

Problems, profiling and pack-wide facts. Player-facing gameplay changes are in [`modpack-wiki.md`](modpack-wiki.md). Per-artifact implementation detail is in that artifact's own README, listed in the [index](#implementation-index).

## Contents

1. [Known Issues](#known-issues)
2. [Performance & Profiling](#performance--profiling)
3. [Attribute audit](#attribute-audit)
4. [Implementation index](#implementation-index)
5. [Resolved](#resolved)

---

# Known Issues

From the 31 Jul 2026 session logs (server 11:29-12:17, client 11:29-13:52). Ordered by impact.

Status: **open**, **testing**, **fixed**, **wontfix** (understood, no action worth taking).

## Server tick debt, open

Five `Can't keep up!` events in a 48-minute session:

| Time | Behind | Ticks |
|---|---|---|
| 11:31:27 | 2,019 ms | 40 |
| 11:38:50 | 2,102 ms | 42 |
| 11:55:14 | 6,674 ms | 133 |
| 12:01:28 | 13,313 ms | 266 |
| 12:02:16 | 32,680 ms | 653 |

This produces the "steady FPS, stuttering image" symptom: the client renders at full framerate while the server sends no entity or position updates.

`Running Nms behind` is cumulative tick debt against wall clock, not a single stall. 6.6 s, 13.3 s, 32.7 s is one continuous degradation from ~11:55 that never recovered. The first event, 16 s after `Done`, is startup.

AllTheLeaks was investigated and ruled out. Its leak scan runs on the Server thread every `logIntervalInMinutes` (10) and its output landed 0.8 s before the 32-second event. Against that:

- Three of four scans (11:41:20, 11:51:20, 12:12:13) produced no lag.
- The scan near the freeze fired 55 s late, while every other interval is 9m58s to 10m00s. The task is scheduled in ticks (`interval × 60 × 20`), so a server behind on ticks fires it late in wall-clock.
- A periodic 10-minute task cannot produce a 7-minute continuous ramp.

`logIntervalInMinutes: 0` is not a safe off switch. The period is `interval × 60 × 20` ticks with no zero-guard on that path, so `0` may schedule the scan every tick. Rename the jar to `.jar.disabled` to disable it.

The 11:55-12:02 window contains: DH Update Propagator threads active at 11:55:23; C2ME workers running `JigsawPlacement`, ~21 warnings inside 0.1 s at 11:58:41; `Kedar420 moved too quickly` at 11:56:27, which is a symptom of the lag.

Current theory is chunk generation: C2ME placing structures plus DH's server-side distant generation, both generating terrain while the player explores. Unconfirmed.

Next step: run `/spark profiler --timeout 300` while the lag is happening.

## Ring of the Grudge grants nothing, testing

`cataclysm:ring_of_grudged` gives no crit damage and shows no stat row. Not an attribute-merge gap.

Fixed by a Mixin in `attribute_merge`, built and statically verified, not yet confirmed in game. See [the fix](#the-fix) below.

Cataclysm gates every curio's attributes on the slot the curio sits in:

```java
if (slotContext.identifier().equals(this.attributeSlot) && this.attributes != null) { ... }
```

The ring's `attributeSlot` is hardcoded `"rings"` (`ModItems.lambda$static$150`, `ADDITIONAL_CRITICAL_DAMAGE`, `ADDITION 10.0`). [`mod_configs_server/curios-common.toml`](../server_configuration/mod_configs_server/curios-common.toml) sets `id=rings;size=0` and [`datapacks/Curios_Slot_Tweaks`](../datapacks/Curios_Slot_Tweaks/) routes the item into the standard `ring` slot, so the identifier is `ring`, the check fails, and the item contributes nothing at all.

Reverting the reroute is not acceptable: every other ring in the pack uses the standard `ring` slot and that has to stay consistent.

Fixing it from our side is blocked on Cataclysm discarding listener output. `CuriosItem.getAttributeModifiers` builds a `LinkedHashMultimap`, posts `CurioAttributeModifierEvent` with it, then returns **its own local map**, while `CurioAttributeModifierEvent.getModifiableMap()` lazily copies via `LinkedHashMultimap.create(originalModifiers)`. So `addModifier` and `removeAttribute` both write to a copy that is thrown away. Curios' own `CuriosImplMixinHooks.getAttributeModifiers` returns `event.getModifiers()` correctly, but Cataclysm overrides that path.

Consequence beyond the ring: no event listener can modify any Cataclysm curio's attributes. In this pack that costs nothing else, because the only attributes Cataclysm curios grant are `cataclysm:nature_heal` (Vitality Ankh, no Apothic counterpart), `cataclysm:additional_critical_damage` (this ring), and vanilla or Forge ones. `withSlotModifier`, which the belts use for their extra talisman slots, is not slot-gated and survives a reroute.

### The fix

`mod_compats/AttributeMerge/src/main/java/com/attributemerge/mixin/CataclysmCuriosMixin.java`, two injections into `CuriosItem.getAttributeModifiers`, both `require = 1` so a Cataclysm update fails at load instead of silently reverting the ring to granting nothing:

| Injection | Fixes |
|---|---|
| `@Redirect` on the method's only `String.equals` call | Makes `ring` satisfy `rings`, and nothing else, so the gate passes and Cataclysm populates its own map |
| `@Inject` at `RETURN`, mutating `cir.getReturnValue()` | Runs `MergeEngine.mergeInPlace` on the map that is actually returned, so the `ADDITION 10.0` becomes `attributeslib:crit_damage +0.10` |

Both were checked against the installed jar's bytecode: the method contains exactly one `String.equals` (offset 13, the gate) and exactly one `areturn` (offset 109), so neither injection is ambiguous.

The mixin targets Cataclysm by name string (`@Mixin(targets = "...")`) and captures no target method arguments, which keeps both Cataclysm and Curios' `SlotContext` off the compile classpath. `AttributeMergeMixinPlugin` skips the config when Cataclysm is absent, checking `LoadingModList` rather than `ModList` because mixin configs are queried during class loading.

Fixing only the gate would have been worse than the current state: the ring would grant raw `cataclysm:additional_critical_damage`, which Cataclysm's own handler reads as a working +10% crit, but the row is in `Hidden Attributes` and the value never reaches `attributeslib:crit_damage`, so it would be an invisible buff. Both injections are needed together.

**Verify in game:** equip the ring in a `ring` slot. The Attributes GUI should show +10% on Crit Damage (`1.5` to `1.6`), and no `% Critical Damage` row should appear. Startup should log no mixin errors for `attribute_merge.mixins.json`.

## Distant Horizons LOD database, open

`world/data/DistantHorizons.sqlite` is 5.3 GiB for the overworld alone, with separate databases for the Nether, End and the Iron's Spellbooks pocket dimension.

It still holds Tinkers' Construct blocks that no longer exist. The client logs `Unable to find BlockState for Block [tconstruct:...]` on DH render-loader threads and takes the fallback path for every affected LOD chunk. Clearing the caches drops the dead references and the bulk, at the cost of regeneration time.

## illageandspillage entities have no attributes, open

690 server lines (345 × `illageandspillage:bone`, 345 × `illageandspillage:axe`) and 1,150 client lines. Both entities are registered without an attribute supplier. Cosmetic, but it dominates both logs.

## medievalorigins missing dependencies, open

11 origins reference content from mods that are not installed, so they load with those powers skipped and are weaker than intended. Affected: `high_elf`, `plague_victim`, `revenant`, `banshee`, `gorgon`, `moon_elf`, `yeti`, `siren`, `incubus`, `ogre`, `troll`.

- Powers: `origins_survive:*`, `thermoo-patches:*`, `origins_toughasnails:*`, `origins_env_deh:*`, `mermod:fabulous_fins`, `survive:cold_blooded`
- Attributes: `ars_nouveau:ars_nouveau.perk.max_mana`, `ars_nouveau:*.perk.mana_regen`, `combatroll:count`, `spell_power:healing`
- Entities/blocks: `iceandfire:siren`, `tradingpost:trading_post`

This is the only issue in this list that changes gameplay.

## generic.attack_damage on mobs that lack it, open

47 client warnings, all the same attribute, across mob types that have none by default: Horse 11, Parrot 9, Chicken 8, Villager 7, Pig 6, SkeletonHorse 3, WanderingTrader 1.

The server sends it in `ClientboundUpdateAttributesPacket` and the client's copy of the entity has no slot for it. A mod present on both sides would register the attribute on both, so the asymmetry means a server-only mod is attaching it.

Suspect: MobControl, which is server-only and whose rule schema has `set.damage`, `set.health`, `set.knockback`. Unconfirmed, because `config/mobcontrol.toml` holds only the shipped documentation example, whose `name="mod:mob"` matches nothing. The client logs the warning and drops the attribute.

## Broken assets and data, open

| Source | Count | Problem |
|---|---|---|
| cluttered | 224 | `Exception loading blockstate definition`, those blocks render as missing models |
| farmersrespite | 180 + 8 | `Unknown recipe category` (kettle_pouring, brewing), missing kettle textures |
| aquamirae_delight | 36 | Missing textures in model |
| netherdepthsupgrade | 32 | Missing textures in model |
| cataclysm | 24 | `Unknown recipe category` (weapon_fusion) |
| oceansdelight | 20 | Missing textures in model |
| companions | 12 | `Unknown recipe category` (soul_furnace) |

Plus 69 loot-table problems server-side: 33 unparseable data files, 29 validation problems, 7 Forge element errors. Three are `Unknown enchantment` (`minecraft:efficiency` ×2, `minecraft:sharpness` ×1) affecting `minecraft:chests/illager_mansion/wool`, `betterarcheology:archeology/sword_loot` and `raidsenhanced:entities/raid_drill`. These also fire on the client in the singleplayer world-list screen, so they are unrelated to the server world or the FML registry wipe. Those tables do not load.

## EMF model-creation cap, wontfix

Entity Model Features stops handling a model after 64 creation attempts, which means something rebuilds it per-render instead of caching. Seven models across four mods hit the cap: `born_in_chaos_v1:modelspiritguidesombrero`, `cataclysm:aptrgangr_head_model`, `cataclysm:draugr_head_model`, `cataclysm:kobolediator_head_model`, `irons_spellbooks:pyrium_staff_head`, `irons_spellbooks:pyrium_staff_orb`, `minecraft:player_head`. Those models lose EMF support for the session. The repeated allocation is minor GC pressure.

## Structurify config sync exceeds packet limit, wontfix

`/structurify config sync fromServer` throws `EncoderException: String too big (was 40849 characters, max 32767)`. Structurify serialises its whole structure config into one string field, and this pack exceeds Forge's `FriendlyByteBuf` string cap by ~25%. Worldgen is server-side so structures still generate, only the client-side config view is lost. No config knob avoids it.

## Stale JVM crash log, wontfix

`hs_err_pid20432.log` and `replay_pid20432.log` in the server folder are an `EXCEPTION_ACCESS_VIOLATION` from 23 Jul 2026, unrelated to anything above. Safe to delete.

---

# Performance & Profiling

The two performance settings that belong to no datapack or compat mod.

## spark, background profiler off

spark enables its background profiler by default on dedicated servers, sampling continuously from boot. On Windows the low-overhead engine is unavailable: the log confirms `The async-profiler engine is not supported for your os/arch (windows10/amd64), so the built-in Java engine will be used instead`, leaving the JVM thread-dump sampler, which costs more.

[`mod_configs_server/spark.json`](../server_configuration/mod_configs_server/spark.json) sets `backgroundProfiler: false`. On-demand `/spark profiler` still works.

The key must be present and explicitly `false`. spark treats a missing key as enabled on servers (`didEnableByDefault`), so deleting the line re-enables it. There is no JVM flag equivalent.

## Distant Horizons, server-side distant generation

The server runs DH with `enableDistantGeneration = true`, generating LOD chunks off-thread. Threading in [`mod_configs_server/DistantHorizons.toml`](../server_configuration/mod_configs_server/DistantHorizons.toml):

| Setting | Value | Effect |
|---|---|---|
| `common.multiThreading.numberOfThreads` | `4` | Was `12`. 12 full-duty world-gen threads starved the server tick thread and C2ME |
| `common.multiThreading.threadRunTimeRatio` | `0.5` | Was `1.0`. Each thread idles half the time, so gen bursts stop causing tick stutter |

Set `enableDistantGeneration = false` to stop the server pre-generating LODs. Clients then build their own as they explore.

---

# Attribute audit

Pack-wide findings. How each layer is implemented is in the READMEs in the [index](#implementation-index).

Apothic Attributes (`attributeslib:*`) is the canonical set. It owns the Attributes GUI, the tooltips and the combat formulas in `config/attributeslib.cfg`.

## How attributes are treated

Five layers, in the order a stat passes through them.

### 1. Merge everything onto Apothic Attributes

One concept, one attribute. A duplicate is drained at the point the item declares it and re-added on the Apothic twin, so the value stacks with every other source of that stat and the owning mod's own handler sees its base value and contributes nothing. No double-dipping, no cancelling modifier.

A stat is only merged when an Apothic counterpart exists. Where none does, the attribute stays live and visible on its own.

### 2. Hide the leftovers, via the Apothic config

Merging moves values, it does not unregister attributes, so a merged source still renders a row at its base value. [`mod_configs_client/attributeslib.cfg`](../client_configuration/mod_configs_client/attributeslib.cfg) lists those in `Hidden Attributes`.

The list affects **the Attributes GUI only**. Item tooltips still show whatever the item declares.

Currently hidden: the 7 `irons_lib:*`, 5 `obscure_api:*`, `cataclysm:additional_critical_damage`, and 3 `additionalentityattributes:*`, plus the 4 ApothicAttributes defaults.

### 3. Label the source, via a resource pack

With several mods shipping identically named attributes, the GUI alone cannot tell them apart. [`resourcepacks/Attribute_Mod_Labels/`](../resourcepacks/Attribute_Mod_Labels/) appends the source mod to every attribute name, so `Crit Damage (Apotheosis)` and `Crit Damage (Origins)` are distinguishable. 107 attributes, 12 sources.

The override is global, not GUI-only: the same string appears on gear tooltips and in JEI.

### 4. The merge mod, for item-declared attributes

[`mod_compats/AttributeMerge/`](../mod_compats/AttributeMerge/) handles the families the datapack cannot, because it can rescale an amount and force an operation. It listens to `ItemAttributeModifierEvent` at `LOW` priority and rewrites duplicate modifiers onto the canonical attribute.

Covers Cataclysm, Obscure API and Additional Entity Attributes. Zero compile dependencies: every attribute is resolved by `ResourceLocation` at runtime and a rule whose mod is absent is skipped with a debug line.

The same listener also runs the reverse case, a stat that is not an attribute anywhere. `item/EpicKnightsPiercing` reads `WeaponType.armorPiercing` (an `int` percent, config-driven, nonzero on ten melee weapons) off the item and grants `attributeslib:armor_shred` at `amount / 100` on `MAINHAND`. Epic Knights' own mechanic is a *second* damage instance, not an attribute, so nothing gets drained and it has to be switched off separately: `disableArmorPiercing: true` in `general.json5`, on both sides, server for the damage and client for its tooltip line. Repo copies live in [`mod_configs_server/epicknights/`](../server_configuration/mod_configs_server/epicknights/) and [`mod_configs_client/epicknights/`](../client_configuration/mod_configs_client/epicknights/).

### 5. The datapack, for Iron's Lib

[`datapacks/Attribute_Merge/`](../datapacks/Attribute_Merge/) uses Iron's Lib's own `irons_lib_attribute_remap` registry for its 7 attributes. Iron's Lib intercepts both `ItemAttributeModifierEvent` and Curios' `CurioAttributeModifierEvent`, but transfers the amount as-is, which is why only Iron's Lib can use it.

### Not covered by any layer

- Stats granted by potions, spells or per-tick code instead of item modifiers.
- Stats that are not attributes at all, unless layer 4 is taught to read them off the item, as it is for Epic Knights piercing. Companions has zero `AttributeModifier` references, so Crystallized Blood Scythe and Life Stealer implement their effects in code, and L_Ender's Delight runs its own capability stat system.
- Cataclysm curios, except through a Mixin. `CuriosItem.getAttributeModifiers` posts `CurioAttributeModifierEvent` but returns its own map rather than the event's, and the event's mutator methods write to a `LinkedHashMultimap` copy, so every listener mutation is discarded. Layer 4 reaches them with a Mixin instead, layer 5 cannot reach them at all. See [Ring of the Grudge](#ring-of-the-grudge-grants-nothing-testing).

## Every attribute registrar

Read out of the installed jars, server and client:

| Mod | Note |
|---|---|
| Apothic Attributes | canonical |
| Iron's Lib | 7, merged by the datapack |
| Obscure API | 12, 6 merged |
| Cataclysm | 4, 1 merged |
| Additional Entity Attributes | 13, 3 merged. Jar-in-jar inside Origins -> Apoli, loaded by Sinytra Connector |
| Iron's Spellbooks, GTBC's SpellLib, GTBC's Geomancy Plus | per-school spell stats, not duplicates |
| Botania | `pixieSpawnChance` |
| Caelus | elytra flight |
| Ascendant Arcana, Medieval Origins | not yet inventoried |
| Dungeon Now Loading | `MaxHealthAttributeMixin` only, raises the vanilla `max_health` cap, registers nothing |
| Connector | Sinytra, hosts the Fabric-side attributes |

Tinkers' Construct is no longer installed, so its 12 `tconstruct:*` attributes are gone. `additionalentityattributes:generic.jump_height` is now the only working jump attribute in the pack. Nothing grants it.

## Every "critical damage" attribute

Registered base / min / max, read out of the installed jars:

| GUI label | Attribute | Base | Min | Max | Status |
|---|---|---|---|---|---|
| Crit Damage | `attributeslib:crit_damage` | 1.5 | 1.0 | 100 | canonical |
| Critical Damage | `irons_lib:crit_damage` | 1.0 | 0.0 | 1024 | remapped by datapack, hidden |
| Critical Damage | `obscure_api:critical_damage` | 2.0 | 0.0 | 100 | merged by the mod, hidden |
| % Critical Damage | `cataclysm:additional_critical_damage` | 0.0 | −512 | 512 | merged by the mod ×0.01, hidden |
| Crit Damage | `additionalentityattributes:player.critical_bonus_damage` | 0.5 | −1.0 | 1024 | merged by the mod ×1, hidden |
| Spell Critical Damage | `gtbcs_spell_lib:spell_crit_damage` | 1.0 | 1.0 | 10 | spell crits only |

The AEA row needs no rescale despite its 0.5 base: it counts the bonus above 1.0 while Apothic counts the multiplier itself, so `1.0 + 0.5` and `1.5` describe the same crit and an `ADDITION` transfers unchanged. Only `ADDITION` transfers exactly, since the two bases differ.

## Which mods grant these stats

| Attribute family | Granted by |
|---|---|
| Apothic `attributeslib:*` | Unique Accessories: armor pierce, arrow damage, arrow velocity, crit chance, crit damage, dodge chance, draw speed, mining speed. Already canonical |
| Obscure API `obscure_api:*` | Aquamirae (accuracy, critical damage, critical hit, penetration), Aquamirae Delight (critical hit) |
| Cataclysm `cataclysm:*` | Cataclysm itself: The Annihilator (`ADDITION 75.0`) and The Immolator (`ADDITION 60.0`) for crit damage, `nature_heal` via armor tiers and a food mixin |
| Iron's Lib `irons_lib:*` | Nothing. No installed mod grants them. Iron's Spellbooks' only `irons_lib:` references are a particle texture and a worldgen processor. Hidden, with the datapack remap kept as a safety net |
| AEA `additionalentityattributes:*` | Two powers only, out of 257 scanned across Origins and Medieval Origins. `origins:swim_speed` (Merling) grants `generic.water_speed`; `medievalorigins:goblin/greedy` grants `player.bonus_rare_loot_rolls` and `player.bonus_loot_count_rolls`. Nothing grants the three merged duplicates |
| L_Ender's Delight | Its own capability stat system, not attributes of its own. `StatConsumableItem` increments a persistent `IPlayerStat`, and `PlayerStatEvents.onTick` applies transient modifiers for movement speed, attack speed, attack damage, armor, knockback resistance and swim speed, all vanilla or Forge attributes. Nothing to merge |

## Dodge chance is not one attribute

Player-facing numbers are in [the wiki](modpack-wiki.md#dodge-chance). Medieval Origins' Moon Elf "Nimble" is not an attribute but `origins:modify_damage_taken` (`multiply_total_multiplicative −2.0`) gated on predicate `medievalorigins:dodge_chance` = `random_chance 0.2`, so it is invisible to the GUI and rolls independently of the Apothic stat.

Folding it into `attributeslib:dodge_chance` as an `origins:attribute` power would reduce its coverage: Apothic's dodge only applies to melee damage from entities in range, while the power negates any damage type.

Unique Accessories belt amounts are configurable in `config/uniqueaccessories-common.toml` (stock values).

`irons_lib:dodge_chance` and `obscure_api:dodge` are registered and their handlers work, but nothing in the pack grants them.

---

# Implementation index

Each of these is documented once, in its own README.

| Change | Documented in |
|---|---|
| Iron's Lib attribute remap, 7 stats, datapack | [`datapacks/Attribute_Merge/README.md`](../datapacks/Attribute_Merge/README.md) |
| Cataclysm and Obscure API merge, and why a code hook was needed | [`mod_compats/AttributeMerge/README.md`](../mod_compats/AttributeMerge/README.md) |
| Source-mod labels on attribute names | [`resourcepacks/Attribute_Mod_Labels/README.md`](../resourcepacks/Attribute_Mod_Labels/README.md) |
| RealmRPG jigsaw pools resolving without their namespace | [`datapacks/RealmRPG_Pool_Namespace_Fix/README.md`](../datapacks/RealmRPG_Pool_Namespace_Fix/README.md) |
| Eyes of Ender redistributed to bosses and chests | [`datapacks/Eyes_Of_Ender_Drops/README.md`](../datapacks/Eyes_Of_Ender_Drops/README.md) |
| Per-mod game rules | [`datapacks/Server_Game_Rules/README.md`](../datapacks/Server_Game_Rules/README.md) |
| Curios slot changes | [`datapacks/Curios_Slot_Tweaks/README.md`](../datapacks/Curios_Slot_Tweaks/README.md) |
| KubeJS scripts and deploy | [`kubejs/README.md`](../kubejs/README.md) |
| Iron's Companions content/compat mod | [`mod_compats/IronsCompanions/README.md`](../mod_compats/IronsCompanions/README.md) |
| Adding and updating mods, packwiz workflow | [`COMMANDS.md`](COMMANDS.md) |

JDK 17 is available at `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot`, and JDK 21 at `C:\Program Files\Java\jdk-21.0.10`. `attribute_merge-0.1.0.jar` is built and deployed to the client instance. `irons_companions` and `originspellschools` are unbuilt.

---

# Resolved

## Server crash on every mob death, fixed 30 Jul 2026

`NullPointerException: Registry Object not present: majruszsenchantments:immortality`, thrown from Majrusz's Immortality death listener on every mob death.

Forge RegistryObjects were left unbound by the registry remap that ran on the first world load after Tinkers' Construct was removed (1,304 missing entries deleted, 20 `ascendant_arcana` enchantments logged `Object did not get ID it asked for`). Majrusz Library caches Forge's `RegistryObject::get` as its value supplier, so an unbound handle throws on every `EnchantmentHelper.has()` call.

Fixed by stripping the root-level `fml` compound from `world/level.dat`, forcing Forge to rebuild registry IDs from the current modlist with no remap. Verified after reboot: all 24 majrusz enchantments present, zero NPEs, zero missing-entry warnings, zero ID remaps.

Inspect `level.dat` by walking the NBT, not by string-grepping the decompressed bytes. Registry names live inside `{K:name, V:id}` compounds and anchored greps miss entries at random. That mistake produced a wrong "7 enchantments are missing" diagnosis during this investigation.
