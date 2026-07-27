package com.github.no_name_provided.nnp_fluidlogging.mixins.update_shape_fixes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.github.no_name_provided.nnp_fluidlogging.common.helpers.MiscHelpers.fixScheduledFluidTick;

/**
 * Important mixin - this is what stops water from flowing when a slab is logged with a different fluid. It does not
 * currently cause the correct fluid to flow, or stop adjacent fluids from being cooled.
 * <p>
 * We'll need one of these mixins for each SimpleWaterloggedBlock block, since vanilla hardcodes the water reference.
 * </p>
 */
@Mixin(StairBlock.class)
public class FFluidlogging_StairBlock extends Block implements SimpleWaterloggedBlock {
    public FFluidlogging_StairBlock(Properties props) {
        super(props);
    }
    
    /**
     * Force updateShape to trigger a tick with a fluid from our attachment (when present).
     * <p>
     * By the time we get here, we've already passed a WATERLOGGED property check, so we know not finding an entry in
     * our map means the fluid is water (rather than no fluid).
     * </p>
     *
     * @param level     The level the block is in.
     * @param pos       The position the block is in.
     * @param fluid     The fluid that should be ticked (usually the fluid contents of the block).
     * @param tickDelay The tick delay (usually fully specified by the fluid).
     */
    @Redirect(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void nnp_f_fluidlogging_updateShape(LevelAccessor level, BlockPos pos, Fluid fluid, int tickDelay) {
        fixScheduledFluidTick(level, pos);
    }
}
