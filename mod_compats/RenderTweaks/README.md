# RenderTweaks

Client-only Forge mod. Fixes render-thread hotspots found in spark client profiles (`/sparkc profiler start --timeout 60`). Servers and other players do not need it (`displayTest = IGNORE_ALL_VERSION`).

Each tweak is skipped by `RenderTweaksMixinPlugin` when its target mod is not installed.

## Tweaks

| Tweak | Target | Cost before | What changes |
|---|---|---|---|
| Block-entity render bounds cache | `BlockEntity` (Forge `IForgeBlockEntity#getRenderBoundingBox` default) | ~2.5% of frame in a village | Embeddium asks every visible block entity for its render box every frame, and Forge's default recomputes the collision shape bounds each time. The box is now cached per block entity until its block state or level changes. Blocks with dynamic shapes (shulker boxes, moving pistons) are never cached. Block entities that override the method are unaffected |
| Batched Lionfish models | Lionfish API 3.0 `AdvancedModelBox#doRender` (Cataclysm mobs) | ~6.5% with 8 Cataclysm mobs in view | Every vertex went through `VertexConsumer.vertex`, one call at a time through Oculus' extended format. Each box is now written in one batch through Embeddium's `VertexBufferWriter`, the path Embeddium already uses for vanilla models. Falls back to Lionfish's own code when the buffer is not one Embeddium can write to |
| Chloride entity-limit sort | Chloride 1.8.1 `EntityCulling.DISTANCE_COMPARATOR` | ~3.5% with 460 entities loaded | `entityLimit` sorts every loaded entity by distance each frame, and the comparator recomputed both square-rooted distances per comparison. Each entity's squared distance is now computed once per frame. Same order, same result |
| KnightLib hitbox picker | KnightLib 2.0.5 `BoneHitboxPicker#pick` | ~2% with 460 entities loaded | Walked every entity in the level twice a frame. Now gets only entities near the look ray from the level's spatial index. Its own filters still run, so the picked target is the same |

Costs are from profiles `JXp9IJFor9` (Create village, 8 Cataclysm mobs) and `zEL7XkVDgF` (village with 459 entities, 165 item frames).

## Files

| Path | Purpose |
|---|---|
| `RenderTweaksMixinPlugin` | Applies `mixin/chloride/*`, `mixin/knightlib/*`, `mixin/lionfish/*` only when their mods are present. Lionfish also needs Embeddium |
| `RenderBounds` + `mixin/BlockEntityRenderBoundsMixin` | Forge's default render box, copied as-is, plus the cache |
| `lionfish/LionfishBoxData` | Flattens a Lionfish `ModelBox` into a float array once per box. Its quad and vertex classes are package-private, so they are read by reflection, once |
| `lionfish/LionfishFastRender` + `mixin/lionfish/*` | The batched writer. If reflection fails it logs one warning and turns itself off for the session |
| `chloride/EntitySortKeys` + `mixin/chloride/*` | Per-frame distance key on `Entity`, frame counter at the start of `LevelRenderer#renderLevel`, `@Overwrite` of Chloride's comparator lambda |
| `mixin/knightlib/BoneHitboxPickerMixin` | `@Redirect` of `entitiesForRendering()` inside `pick` |

## Build

Copy `embeddium-0.3.31+mc1.20.1.jar` from the client instance's `mods/` into `libs/`, then `./gradlew build`. Output: `build/libs/render_tweaks-0.1.0.jar`, client `mods/` only.

The Chloride, KnightLib and Lionfish targets are pinned to the versions above by method name. After updating one of those mods, check the log for a mixin apply error.

## Related, not fixed here

- Chloride `entityLimit = 70` renders only the 70 entities nearest the player in the whole level, including ones behind you. With 165 item frames in a village, mobs in front of you can be dropped. `512` turns the limit off.
- Shader shadow pass is ~11–12% of the frame. Lower shadow distance or turn off entity shadows in the shaderpack settings.
