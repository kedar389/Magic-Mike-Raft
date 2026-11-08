# RenderTweaks

Client-only Forge mod. Fixes render-thread hotspots found in spark client profiles (`/sparkc profiler start --timeout 60`). Servers and other players do not need it (`displayTest = IGNORE_ALL_VERSION`).

Each tweak is skipped by `RenderTweaksMixinPlugin` when its target mod is not installed.

## Tweaks

| Tweak | Target | Cost before | What changes |
|---|---|---|---|
| Block-entity render bounds cache | `BlockEntity` (Forge `IForgeBlockEntity#getRenderBoundingBox` default) | ~2.5% of frame in a village | Embeddium asks every visible block entity for its render box every frame, and Forge's default recomputes the collision shape bounds each time. The box is now cached per block entity until its block state or level changes. Blocks with dynamic shapes (shulker boxes, moving pistons) are never cached. Block entities that override the method are unaffected |
| Batched Lionfish models | Lionfish API 3.0 `AdvancedModelBox#doRender` (Cataclysm mobs) | ~6.5% with 8 Cataclysm mobs in view | Every vertex went through `VertexConsumer.vertex`, one call at a time through Oculus' extended format. Each box is now written in one batch through Embeddium's `VertexBufferWriter`, the path Embeddium already uses for vanilla models. Falls back to Lionfish's own code when the buffer is not one Embeddium can write to |
| Chloride entity limit | Chloride 1.8.1 `EntityDistanceCullingMixin$LevelRendererEntityMixin.redirect$entitiesForRendering` | ~2.2% with 160 entities loaded | `entityLimit` copies every loaded entity into a list and sorts it with a comparator that reads both entities' positions per comparison, which jumps around memory. Now each squared distance is read once into a primitive array and that array is sorted. Same entities, nearest first. Reached through MixinSquared. (The first version only cached the distance per entity; the profile showed the sort itself was the cost, so it was replaced.) |
| Apoli layer check | Apoli 2.9.2 (Fabric, via Connector) `LivingEntityRendererMixin.preventFeatureRendering` | ~2% in a village | Apoli wraps every render layer of every mob and, per layer, fetches two power lists into fresh `LinkedList`s and streams over them, almost always empty. Now looked up once per mob per frame. A mob with no Invisibility or Prevent Feature Render power gets its layer drawn directly; one with either goes through Apoli unchanged. Needs MixinSquared (shipped inside Medieval Origins, AsyncParticles and others) to reach Apoli's handler |
| KnightLib hitbox picker | KnightLib 2.0.5 `BoneHitboxPicker#pick` | ~2% with 460 entities loaded | Walked every entity in the level twice a frame. Now gets only entities near the look ray from the level's spatial index. Its own filters still run, so the picked target is the same |

Costs are from profiles `JXp9IJFor9` (Create village, 8 Cataclysm mobs) and `zEL7XkVDgF` (village with 459 entities, 165 item frames).

## Switching fixes in game

Every fix starts on and can be switched live, for A/B testing in one spot without restarting. Not saved, a restart turns all back on.

| Command | Effect |
|---|---|
| `/rendertweaks` | Lists each fix and whether it is on |
| `/rendertweaks <bounds|cataclysm|chloride|knightlib|apoli> <true|false>` | Switches one fix |
| `/rendertweaks all <true|false>` | Switches all |

Off runs the original code path: Forge's uncached default, Lionfish's own vertex loop, Chloride's original sort, KnightLib's full entity walk, Apoli's per-layer check.

## Files

| Path | Purpose |
|---|---|
| `Tweak` + `RenderTweaksCommand` | The on/off switches and the `/rendertweaks` client command |
| `FrameCounter` + `mixin/LevelRendererFrameMixin` | Frame number, bumped at the start of each world render, for once-per-frame caches |
| `apoli/ApoliPowers` + `mixin/apoli/*` | MixinSquared `@TargetHandler` in front of Apoli's handler, per-frame power check on `Entity`, Apoli called by reflection (its jar is Fabric-named). `require = 0`, so an Apoli rename skips the tweak instead of failing startup |
| `RenderTweaksMixinPlugin` | Applies `mixin/apoli/*` (needs `apoli` + `mixinsquared`), `mixin/chloride/*` (needs `chloride` + `mixinsquared`), `mixin/knightlib/*`, `mixin/lionfish/*` only when their mods are present. Lionfish also needs Embeddium |
| `RenderBounds` + `mixin/BlockEntityRenderBoundsMixin` | Forge's default render box, copied as-is, plus the cache |
| `lionfish/LionfishBoxData` | Flattens a Lionfish `ModelBox` into a float array once per box. Its quad and vertex classes are package-private, so they are read by reflection, once |
| `lionfish/LionfishFastRender` + `mixin/lionfish/*` | The batched writer. If reflection fails it logs one warning and turns itself off for the session |
| `chloride/NearestEntities` + `mixin/chloride/*` | MixinSquared `@TargetHandler` in front of Chloride's handler. Reads Chloride's live `entityLimit` by reflection; a limit of 0 or 512+ is left to Chloride. `require = 0` |
| `mixin/knightlib/BoneHitboxPickerMixin` | `@Redirect` of `entitiesForRendering()` inside `pick` |

## Build

Copy `embeddium-0.3.31+mc1.20.1.jar` from the client instance's `mods/` into `libs/`, and `MixinSquared-0.3.7-beta.3.jar` (nested at `META-INF/jarjar/mixinsquared-forge-0.3.7-beta.3.jar` → `META-INF/jars/` inside the Medieval Origins jar), then `./gradlew build`. Output: `build/libs/render_tweaks-0.1.0.jar`, client `mods/` only.

The Chloride, KnightLib, Lionfish and Apoli targets are pinned to the versions above by method name. After updating one of those mods, check the log for a mixin apply error.

## Related, not fixed here

- Chloride `entityLimit = 70` renders only the 70 entities nearest the player in the whole level, including ones behind you. With 165 item frames in a village, mobs in front of you can be dropped. `512` turns the limit off.
- Shader shadow pass is ~11–12% of the frame. Lower shadow distance or turn off entity shadows in the shaderpack settings.
