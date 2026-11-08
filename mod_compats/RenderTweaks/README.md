# RenderTweaks

Client-only Forge mod. Fixes render-thread hotspots found in spark client profiles (`/sparkc profiler start --timeout 60`). Servers and other players do not need it (`displayTest = IGNORE_ALL_VERSION`).

Each tweak is skipped by `RenderTweaksMixinPlugin` when its target mod is not installed.

## Tweaks

| Tweak | Target | Cost before | What changes |
|---|---|---|---|
| Block-entity render bounds cache | `BlockEntity` (Forge `IForgeBlockEntity#getRenderBoundingBox` default) | ~2.5% of frame in a village | Embeddium asks every visible block entity for its render box every frame, and Forge's default recomputes the collision shape bounds each time. The box is now cached per block entity until its block state or level changes. Blocks with dynamic shapes (shulker boxes, moving pistons) are never cached. Block entities that override the method are unaffected |
| Block-entity light cache | `BlockEntityRenderDispatcher#setupAndRender` → `LevelRenderer#getLightColor` | ~4.2% in a busy base | Every block entity drawn re-read its block, block light and sky light from chunk storage every frame. Now each block entity keeps its packed light and re-reads it every 8 frames, staggered by position so refreshes never pile into one frame. A light change reaches it within 8 frames (~40 ms at 200 FPS). Time of day is unaffected: that brightness comes from the lightmap, not this value |
| Batched Lionfish models | Lionfish API 3.0 `AdvancedModelBox#doRender` (Cataclysm mobs) | ~6.5% with 8 Cataclysm mobs in view | Every vertex went through `VertexConsumer.vertex`, one call at a time through Oculus' extended format. Each box is now written in one batch through Embeddium's `VertexBufferWriter`, the path Embeddium already uses for vanilla models. Falls back to Lionfish's own code when the buffer is not one Embeddium can write to |
| Chloride entity limit | Chloride 1.8.1 `EntityDistanceCullingMixin$LevelRendererEntityMixin.redirect$entitiesForRendering` | ~2.2% with 160 entities loaded | `entityLimit` copies every loaded entity into a list and sorts it with a comparator that reads both entities' positions per comparison, which jumps around memory. Now each squared distance is read once into a primitive array and that array is sorted. Same entities, nearest first. With no more entities loaded than the limit, the level's own list is returned without measuring or sorting. Reached through MixinSquared. (The first version only cached the distance per entity; the profile showed the sort itself was the cost, so it was replaced.) |
| Apoli layer, render and glow checks | Apoli 2.9.2 (Fabric, via Connector) `LivingEntityRendererMixin.preventFeatureRendering`, `EntityRenderDispatcherMixin.preventRenderingEntities`, `MinecraftClientMixin.makeEntitiesGlow` | ~2.3% in a village, ~5.8% with 312 entities and an Origins player | Apoli wraps every render layer of every mob and, per layer, fetches two power lists into fresh `LinkedList`s and streams over them, almost always empty. Now looked up once per mob per frame. A mob with no Invisibility or Prevent Feature Render power gets its layer drawn directly; one with either goes through Apoli unchanged. The per-entity "should it render" check only asks whether the local player has a Prevent Entity Render power, so that is now answered once per frame; with none, Apoli's check is skipped. The glow check (per entity: does the player have an Entity Glow power, does the entity have a Self Glow power) is answered once per frame for the player and once per entity per frame; with neither, Apoli's check is skipped. Needs MixinSquared (shipped inside Medieval Origins, AsyncParticles and others) to reach Apoli's handler |
| Bagus Lib pose reset | Bagus Lib 5.6.1 `ClientEventHandler.animationEvent(BagusModelEvent.Init)` | ~1.2% in a village | For every supported mob model, every frame, it reset every part's pose through a Java stream over the whole part tree. Now the tree is flattened once per model and looped over. Models from Entity Model Features (Fresh Animations) keep Bagus Lib's own code, since EMF can change parts at runtime; caches reset on every resource reload |
| Pehkui scale cache | Pehkui 3.8.2 `ScaleData.getScale(float)` | ~4.6% with 300 entities | On the client every scale lookup recomputed: base-scale lerp, then every modifier in a sorted set, each possibly looking up another scale. Pehkui caches only on the server. Mob rendering, entity-search bounding boxes and the shadow pass ask for the same values many times a frame. Now the last result per scale and delta is kept on the render thread and dropped wherever Pehkui drops its own cache (every tick, every update). A dependent scale (width on base) can trail one frame when only the base changes mid-tick, the same window Pehkui's server cache accepts |
| KnightLib hitbox picker | KnightLib 2.0.5 `BoneHitboxPicker#pick` | ~2% with 460 entities loaded | Walked every entity in the level twice a frame. Now gets only entities near the look ray from the level's spatial index. Its own filters still run, so the picked target is the same |

Costs are from profiles `JXp9IJFor9` (Create village, 8 Cataclysm mobs) and `zEL7XkVDgF` (village with 459 entities, 165 item frames).

## Switching fixes in game

Every fix starts on and can be switched live, for A/B testing in one spot without restarting. Not saved, a restart turns all back on.

| Command | Effect |
|---|---|
| `/rendertweaks` | Lists each fix and whether it is on |
| `/rendertweaks <bounds|light|cataclysm|chloride|knightlib|apoli|bagus|pehkui> <true|false>` | Switches one fix |
| `/rendertweaks all <true|false>` | Switches all |

Off runs the original code path: Forge's uncached default, a light read every frame, Lionfish's own vertex loop, Chloride's original sort, KnightLib's full entity walk, Apoli's per-layer check, Bagus Lib's stream over all parts, Pehkui recomputing every scale.

## Files

| Path | Purpose |
|---|---|
| `Tweak` + `RenderTweaksCommand` | The on/off switches and the `/rendertweaks` client command |
| `FrameCounter` + `mixin/LevelRendererFrameMixin` | Frame number, bumped at the start of each world render, for once-per-frame caches |
| `apoli/ApoliPowers` + `mixin/apoli/*` | MixinSquared `@TargetHandler` in front of Apoli's handler, per-frame power check on `Entity`, Apoli called by reflection (its jar is Fabric-named). `require = 0`, so an Apoli rename skips the tweak instead of failing startup |
| `bagus/PoseReset` + `mixin/bagus/*`, `RenderTweaksClientEvents` | Flat part array on `ModelPart`, invalidated by a client reload listener |
| `mixin/pehkui/ScaleDataCacheMixin` | Last scale per `ScaleData`, render thread and client-side entities only, cleared in `invalidateCachedScales` |
| `RenderTweaksMixinPlugin` | Applies `mixin/apoli/*` (needs `apoli` + `mixinsquared`), `mixin/bagus/*` (needs `bagus_lib`), `mixin/pehkui/*` (needs `pehkui`), `mixin/chloride/*` (needs `chloride` + `mixinsquared`), `mixin/knightlib/*`, `mixin/lionfish/*` only when their mods are present. Lionfish also needs Embeddium |
| `light/BlockEntityLight` + `mixin/BlockEntityRenderLightMixin`, `mixin/BlockEntityLightCacheMixin` | `@Redirect` of the `getLightColor` call in `setupAndRender` only, so mods patching inside `getLightColor` still apply on refresh |
| `RenderBounds` + `mixin/BlockEntityRenderBoundsMixin` | Forge's default render box, copied as-is, plus the cache |
| `lionfish/LionfishBoxData` | Flattens a Lionfish `ModelBox` into a float array once per box. Its quad and vertex classes are package-private, so they are read by reflection, once |
| `lionfish/LionfishFastRender` + `mixin/lionfish/*` | The batched writer. If reflection fails it logs one warning and turns itself off for the session |
| `chloride/NearestEntities` + `mixin/chloride/*` | MixinSquared `@TargetHandler` in front of Chloride's handler. Reads Chloride's live `entityLimit` by reflection; a limit of 0 or 512+ is left to Chloride. `require = 0` |
| `mixin/knightlib/BoneHitboxPickerMixin` | `@Redirect` of `entitiesForRendering()` inside `pick` |

## Build

Copy `embeddium-0.3.31+mc1.20.1.jar` from the client instance's `mods/` into `libs/`, `bagus_lib-1.20.1-5.6.1.jar` from the same folder, and `MixinSquared-0.3.7-beta.3.jar` (nested at `META-INF/jarjar/mixinsquared-forge-0.3.7-beta.3.jar` → `META-INF/jars/` inside the Medieval Origins jar), then `./gradlew build`. Output: `build/libs/render_tweaks-0.1.0.jar`, client `mods/` only.

The Chloride, KnightLib, Lionfish, Apoli, Bagus Lib and Pehkui targets are pinned to the versions above by method name. After updating one of those mods, check the log for a mixin apply error.

## Related, not fixed here

- Chloride `entityLimit = 70` renders only the 70 entities nearest the player in the whole level, including ones behind you. With 165 item frames in a village, mobs in front of you can be dropped. `512` turns the limit off.
- Shader shadow pass is ~11–12% of the frame. Lower shadow distance or turn off entity shadows in the shaderpack settings.
