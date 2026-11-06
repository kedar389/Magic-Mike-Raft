package com.rendertweaks.mixin;

import com.rendertweaks.RenderBounds;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches Forge's default render bounding box. Embeddium asks for it for every block entity every
 * frame, and the default recomputes the collision shape's bounds each time (2.5% of the frame in a
 * village with signs, banners and chests). Block entities that override the method are unaffected.
 */
@Mixin(value = BlockEntity.class, remap = false)
public abstract class BlockEntityRenderBoundsMixin implements IForgeBlockEntity {

    @Unique
    private AABB renderTweaks$bounds;

    @Unique
    private BlockState renderTweaks$boundsState;

    @Unique
    private Level renderTweaks$boundsLevel;

    @Override
    public AABB getRenderBoundingBox() {
        BlockEntity self = (BlockEntity) (Object) this;
        BlockState state = self.getBlockState();
        Level level = self.getLevel();
        if (renderTweaks$bounds != null && state == renderTweaks$boundsState && level == renderTweaks$boundsLevel) {
            return renderTweaks$bounds;
        }
        AABB bounds = RenderBounds.compute(self, state);
        if (bounds == null) {
            return RenderBounds.fallback(self);
        }
        if (RenderBounds.isCacheable(state)) {
            renderTweaks$bounds = bounds;
            renderTweaks$boundsState = state;
            renderTweaks$boundsLevel = level;
        }
        return bounds;
    }
}
