package com.rendertweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rendertweaks.light.BlockEntityLight;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code m_112284_} is {@code BlockEntityRenderDispatcher#setupAndRender}, which reads the light
 * for the block entity it is about to draw; {@code m_109541_} is
 * {@code LevelRenderer#getLightColor(BlockAndTintGetter, BlockPos)}. Only this call site is
 * redirected: mods that patch inside {@code getLightColor} (Supplementaries) still run on every
 * refresh.
 */
@Mixin(value = BlockEntityRenderDispatcher.class, remap = false)
public abstract class BlockEntityRenderLightMixin {

    @Redirect(
            method = "m_112284_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;m_109541_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private static int renderTweaks$cachedLight(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockEntityRenderer<?> renderer,
            BlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        return BlockEntityLight.lightColor(level, pos, blockEntity);
    }
}
