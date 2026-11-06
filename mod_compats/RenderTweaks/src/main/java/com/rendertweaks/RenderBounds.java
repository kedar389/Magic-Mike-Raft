package com.rendertweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

/**
 * Forge's default {@link IForgeBlockEntity#getRenderBoundingBox()}, copied as-is, plus the rule for
 * when its result can be reused.
 */
public final class RenderBounds {

    private RenderBounds() {}

    /**
     * Same result as Forge's default. Returns {@code null} when the collision shape threw, which
     * Forge answers with a 1-block fallback box that must not be cached.
     */
    public static AABB compute(BlockEntity blockEntity, BlockState state) {
        Block block = state.getBlock();
        BlockPos pos = blockEntity.getBlockPos();
        if (block == Blocks.ENCHANTING_TABLE) {
            return new AABB(pos, pos.offset(1, 1, 1));
        }
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
        }
        if (block == Blocks.STRUCTURE_BLOCK || block == Blocks.BEACON) {
            return IForgeBlockEntity.INFINITE_EXTENT_AABB;
        }
        try {
            VoxelShape shape = state.getCollisionShape(blockEntity.getLevel(), pos);
            return shape.isEmpty() ? IForgeBlockEntity.INFINITE_EXTENT_AABB : shape.bounds().move(pos);
        } catch (Exception e) {
            return null;
        }
    }

    /** Forge's fallback when the collision shape throws. */
    public static AABB fallback(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }

    /**
     * Vanilla precomputes the collision shape per block state unless the block has a dynamic shape
     * (shulker boxes, moving pistons). With the precomputed shape the box depends only on the state
     * and the fixed position, so it is safe to reuse until the state changes.
     */
    public static boolean isCacheable(BlockState state) {
        return !state.getBlock().hasDynamicShape();
    }
}
