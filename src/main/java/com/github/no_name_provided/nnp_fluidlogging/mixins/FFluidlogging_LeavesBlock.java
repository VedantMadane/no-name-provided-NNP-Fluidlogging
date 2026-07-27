package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.helpers.MiscHelpers.fixScheduledFluidTick;

@Mixin(LeavesBlock.class)
abstract class FFluidlogging_LeavesBlock extends Block {
    protected FFluidlogging_LeavesBlock(Properties props) {
        super(props);
    }
    
    @Redirect(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void nnp_f_fluidlogging_getFluidState(LevelAccessor level, BlockPos pos, Fluid fluid, int tickDelay) {
        fixScheduledFluidTick(level, pos);
    }
    
    /**
     * Fails to fix weird failure to update lights, but at least allows fluid logged leaves to block light? Might be
     * better without this.
     */
    @ModifyReturnValue(method = "getLightBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"))
    private int nnp_f_fluidlogging_getLightBlock(int original, BlockState state, BlockGetter getter, BlockPos pos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
        
            return super.getLightBlock(state, getter, pos);
        }
        
        return original;
    }
    
    @Inject(method = "getFluidState(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
        // The return value from this directly controls almost everything that depends on fluid contents
        // (including visuals and ticking). Unfortunately, it isn't called with positional context,
        // so we need to look upstream.
        
        // We're leaving this here so we can "toggle it on" and compare differences in behavior. Makes it
        // easier to see what we missed.
        
//        cir.setReturnValue(Fluids.LAVA.defaultFluidState());
    }
}
