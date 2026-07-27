package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;
import static com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS;
import static com.github.no_name_provided.nnp_fluidlogging.common.helpers.MiscHelpers.safeSyncChunkAttachment;
import static com.github.no_name_provided.nnp_fluidlogging.common.helpers.MiscHelpers.updateClientLightLevels;

@Mixin(FlowingFluid.class)
abstract class FFluidlogging_FlowingFluid extends Fluid {
    
    //region #getSpread patches ---------------------------------------------------------------------
    
    // Using "name=[NAME]" here causes an issue when using build artifacts in launcher - verified mixin grabs correct variable in dev and no crash in launcher
    @ModifyArg(method = "getSpread(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Map;",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/shorts/Short2ObjectMap;computeIfAbsent(SLit/unimi/dsi/fastutil/shorts/Short2ObjectFunction;)Ljava/lang/Object;"),
            index = 1)
    private Short2ObjectFunction<? extends Pair<BlockState, FluidState>> nnp_f_fluidlogging_getSpread_fixStateMap(Short2ObjectFunction<? extends Pair<BlockState, FluidState>> mappingFunction, @Local(argsOnly = true) Level level, @Local(ordinal = 1) BlockPos blockpos) {
        
        return myShort -> Pair.of(level.getBlockState(blockpos), level.getFluidState(blockpos));
    }
    
    //endregion ---------------------------------------------------------------------
    
    //region getNewLiquid patches ---------------------------------------------------------------------
    
    @ModifyVariable(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "STORE"), name = "fluidstate")
    private FluidState nnp_f_fluidlogging_getNewLiquid_fixFluidState(FluidState fluidstate, Level level, @Local(name = "blockpos") BlockPos blockpos) {
        
        return level.getFluidState(blockpos);
    }
    
    /**
     * Tricks the Neo event into thinking we're looking at a liquid block, so vanillaResult calculates the correct
     * value.
     */
    @ModifyArg(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;canCreateFluidSource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
            index = 2)
    private BlockState nnp_f_fluidlogging_getNewLiquid_hackNeoEvent(BlockState state, @Local(name = "fluidstate") FluidState fluidstate) {
        
        return fluidstate.createLegacyBlock().trySetValue(BlockStateProperties.LEVEL, fluidstate.getAmount());
    }
    
    @ModifyVariable(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "STORE"), name = "fluidstate1")
    private FluidState nnp_f_fluidlogging_getNewLiquid_fixFluidState1(FluidState fluidstate, Level level, BlockPos pos) {
        
        return level.getFluidState(pos.below());
    }
    
    @ModifyVariable(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "STORE"), name = "fluidstate2")
    private FluidState nnp_f_fluidlogging_getNewLiquid_fixFluidState2(FluidState fluidstate, Level level, @Local(name = "blockpos1") BlockPos blockpos1) {
        
        return level.getFluidState(blockpos1);
    }
    
    @ModifyReturnValue(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "RETURN", target = "Lnet/neoforged/neoforge/event/EventHooks;canCreateFluidSource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private FluidState nnp_f_fluidlogging_getNewLiquid_respectLevelLimits(FluidState original, Level level, BlockPos pos, BlockState blockState) {
        if (blockState.getBlock() instanceof SimpleWaterloggedBlock) {
            @SuppressWarnings("deprecation") // Probably more efficient than a registry lookup
            BlockStateFluidLevelLimits levelLimits = blockState.getBlock().builtInRegistryHolder().getData(BLOCKSTATE_FLUID_LEVEL_LIMITS);
            if (levelLimits != null) {
                if (original.getAmount() < levelLimits.getMinLevel(blockState, original.getFluidType())) {
                    // If the level is too low, we prevent flow the same way vanilla does - by returning the empty fluid.
                    original = Fluids.EMPTY.defaultFluidState();
                } else if (original.getAmount() > levelLimits.getMaxLevel(blockState, original.getFluidType())) {
                    // Makes sure the level isn't too high - only supports flowing fluids
                    if (original.isSource() && original.getType() instanceof FlowingFluid flowingFluid) {
                        original = flowingFluid.getFlowing(levelLimits.getMaxLevel(blockState, original.getFluidType()), false);
                    } else {
                        original = original.trySetValue(BlockStateProperties.LEVEL_FLOWING, levelLimits.getMaxLevel(blockState, original.getFluidType()));
                    }
                }
            }
        }
        
        return original;
    }
    
    //endregion ---------------------------------------------------------------------
    
    //region #tick patches
    
    /**
     * Respect our level limits - consider refactoring to use some kind of unified fluid placement helper.
     */
    @ModifyVariable(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "STORE", target = "Lnet/minecraft/world/level/material/FlowingFluid;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)V"),
            name = "fluidstate")
    private FluidState nnp_f_fluidlogging_tick_injectLevelLimitChecks(FluidState fluidstate, Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        @SuppressWarnings("deprecation") // Probably more efficient than a registry lookup
        BlockStateFluidLevelLimits levelLimits = blockState.getBlock().builtInRegistryHolder().getData(BLOCKSTATE_FLUID_LEVEL_LIMITS);
        if (levelLimits != null && fluidstate.getAmount() < levelLimits.getMinLevel(blockState, fluidstate.getFluidType())) {
            // If the level is too low, we prevent flow the same way vanilla does - by returning the empty fluid.
            // Consider moving this into #getNewFluid, to augment the vanilla check
            fluidstate = Fluids.EMPTY.defaultFluidState();
        } else if (levelLimits != null && fluidstate.getAmount() > levelLimits.getMaxLevel(blockState, fluidstate.getFluidType())) {
            // Makes sure the level isn't too high
            fluidstate = fluidstate.trySetValue(BlockStateProperties.LEVEL_FLOWING, levelLimits.getMaxLevel(blockState, fluidstate.getFluidType()));
        }
        
        return fluidstate;
    }
    
    /**
     * We want to update our FluidStates and light levels whenever we change a BlockState, so we just wrap the relevant
     * operation.
     * <p>
     * This also lets us suppress the vanilla block update, if we're interacting with a logged block (otherwise, they'd
     * be replaced with the corresponding LiquidBlock).
     * </p>
     */
    @WrapOperation(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean nnp_f_fluidlogging_tick_updateDataStructureAndPreventBlockVoiding(Level level, BlockPos pos, BlockState newState, int flags, Operation<Boolean> original) {
        boolean flag = false;
        BlockState oldState = level.getBlockState(pos);
        if (oldState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            if (oldState.getValue(BlockStateProperties.WATERLOGGED)) {
                flag = original.call(level, pos, oldState.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE), Block.UPDATE_ALL);
            }
            ChunkAccess chunk = level.getChunkAt(pos);
            chunk.getData(FLUID_STATES).remove(pos.immutable());
            safeSyncChunkAttachment(chunk, FLUID_STATES);
            int oldLightLevel = 0;
            if (level.getAuxLightManager(pos.immutable()) instanceof AuxiliaryLightManager lManager) {
                oldLightLevel = lManager.getLightAt(pos.immutable());
                lManager.removeLightAt(pos.immutable());
            }
            if (ServerConfig.considerFluidLightLevel && level instanceof ServerLevel sLevel) {
                // Suppress redundant packets
                if (oldLightLevel != 0) {
                    updateClientLightLevels(
                            pos,
                            0,
                            sLevel,
                            true
                    );
                }
            }
            chunk.setUnsaved(true);
        } else {
            flag = original.call(level, pos, newState, flags);
        }
        
        // Not currently used, in vanilla or this mod.
        // Currently returns whether we updated a BlockState, not whether we spread
        return flag;
    }
    
    //endregion
}
