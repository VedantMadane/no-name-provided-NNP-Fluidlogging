package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

/**
 * Forces random ticks (server side) to use the correct FluidState.
 */
@Mixin(ServerLevel.class)
abstract class FFluidlogging_ServerLevel extends Level implements WorldGenLevel {
    protected FFluidlogging_ServerLevel(WritableLevelData data, ResourceKey<Level> levelKey, RegistryAccess registries, Holder<DimensionType> dimensionHolder, Supplier<ProfilerFiller> profiler, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(data, levelKey, registries, dimensionHolder, profiler, p_270904_, p_270470_, p_270248_, p_270466_);
    }
    
    /**
     * Forces random block ticks (server side) to use the correct FluidState.
     *
     * @param state           The ticking BlockState.
     * @param chunk           The ticking chunk.
     * @param randomTickSpeed the random tick rate.
     * @param pos             The position of the ticking block.
     * @return The correct fluid state.
     */
    @ModifyVariable(method = "tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V",
            at = @At("STORE"),
            name = "fluidstate")
    private FluidState nnp_f_fluidlogging_tickChunk_fixFluidState(FluidState state, LevelChunk chunk, int randomTickSpeed, @Local(ordinal = 0) BlockPos pos) {
        // The block position local capture seems like it should have an ordinal of 1.
        // However, perhaps because the earlier BlockPos is declared in a narrower scope, this is the first
        // valid variable in the table.
        return getFluidState(pos);
    }
}
