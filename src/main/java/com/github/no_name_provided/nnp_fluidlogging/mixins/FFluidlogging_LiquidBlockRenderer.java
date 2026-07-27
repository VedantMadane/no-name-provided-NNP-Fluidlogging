package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * These mixins cause adjacent fluid logged blocks are treated as sources for (already) flowing fluids. They also stop
 * you from seeing the "face" of a fluid that should be "hidden" behind an adjacent block of the same fluid (this is
 * only a problem for mods that add transparent fluids, but it's a big graphical eyesore for those).
 */
@Mixin(LiquidBlockRenderer.class)
abstract class FFluidlogging_LiquidBlockRenderer {
    @Shadow @Final
    protected abstract float getHeight(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState);
    
    /**
     * Allows modded, transparent, logged fluids to hide both their own back face and the faces of adjacent fluids of
     * the same type (preventing an annoying chessboard pattern of incorrectly rendered faces).
     * <p>
     * We ignore the original return value here because we actually want a slightly weaker, but otherwise redundant,
     * check.
     * </p>
     */
    @ModifyReturnValue(method = "shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("RETURN"))
    private static boolean nnp_f_fluidlogging_shouldRenderFace(boolean original, BlockAndTintGetter level, BlockPos pos, FluidState fluidState, BlockState selfState, Direction direction, BlockState otherState) {
        
        return !LiquidBlockRenderer.isFaceOccludedBySelf(level, pos, selfState, direction) && !fluidState.getType().isSame(level.getFluidState(pos.relative(direction)).getType());
    }
    
    //region #tesselate patches ------------------------------------------------------------
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.DOWN), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate1"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate1(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.UP), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate2"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate2(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.NORTH), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate3"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate3(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.SOUTH), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate4"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate4(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.WEST), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate5"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate5(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.EAST), level);
    }
    
    //endregion ------------------------------------------------------------
    
    /**
     * This is actually a mixin for a wrapper for the "real" getHeight function. We make it pass the correct FluidState
     * as the last parameter to the wrapped call. Fixes glitch where fluid blocks diagonal to fluidlogged blocks treat
     * those blocks as having the incorrect fluidstate, and render with one corner too low (creating a visual gap in the
     * LiquidBlocks rendered).
     */
    @Redirect(method = "getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F"))
    private float nnp_f_fluidlogging_getHeight_fixReturn(LiquidBlockRenderer instance, BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        
        return getHeight(level, fluid, pos, blockState, level.getFluidState(pos));
    }
    
    /**
     * Forces fluids to prefer our data structure when falling down off of a logged block. Prevents gap between source
     * and first flowing block.
     */
    @Redirect(method = "getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z", ordinal = 1)
    )
    private boolean nnp_f_fluidlogging_getHeight_isSame(Fluid blockStateBasedFluidState, Fluid fluid, BlockAndTintGetter level, Fluid thisFluid, BlockPos pos) {
        
        return thisFluid.isSame(NNPFluidlogging$checkFluidState(pos.above(), level).getType());
    }
    
    /**
     * Convenience method for checking fluid states during rendering with some degree of efficiency.
     *
     * @param pos   Location of block space.
     * @param level BlockAndTintGetter. This isn't necessarily going to be a level.
     * @return The fluid state to be used for rendering.
     */
    @Unique
    private static FluidState NNPFluidlogging$checkFluidState(BlockPos pos, BlockAndTintGetter level) {
        BlockState state = level.getBlockState(pos);
        // This first check is just an efficiency thing, borrowed from vanilla
        if (!state.isAir()) {
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                
                return level.getFluidState(pos);
            }
            
            // We can't assume this is empty, as it could be a LiquidBlockContainer that doesn't
            // implement SimpleWaterloggedBlock, like KelpBlock, or a LiquidBlock.
            return state.getFluidState();
        }
        
        return Fluids.EMPTY.defaultFluidState();
    }
}
