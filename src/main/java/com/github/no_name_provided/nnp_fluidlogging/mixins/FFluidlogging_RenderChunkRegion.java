package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * Important mixin - Does most of the heavy lifting of making visual fluidstate checks prefer our data structure. Some
 * other mixins ultimately boil down to replacing hardcoded BlockState#getFluidState with calls to this.
 */
@Mixin(RenderChunkRegion.class)
abstract class FFluidlogging_RenderChunkRegion implements BlockAndTintGetter {
    
    @Shadow
    protected abstract RenderChunk getChunk(int x, int z);
    
    /**
     * Does most of the heavy lifting of making visual fluidstate checks prefer our attachment over the cached,
     * hardcoded blockstate default.
     * <p>
     * Heavily borrows from vanilla.
     * </p>
     *
     * @param pos Location of the block space in the level.
     */
    @WrapMethod(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    private FluidState nnp_f_fluidlogging_getFluidState(BlockPos pos, Operation<FluidState> original) {
        RenderChunk chunk = this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        
        // We only want to query our attachment on the client if the client has the chunk ready (neo syncing fails gracelessy)
        // May not be necessary, as most problems are serverside, but this is fairly harmless and may improve performance during worldgen
        // TODO: benchmark with/without status check while worldgenning kelp`n stuff
        return chunk.wrapped.getPersistedStatus() == ChunkStatus.FULL ?
                (chunk.wrapped.getData(FLUID_STATES).getOrDefault(pos, chunk.getBlockState(pos).getFluidState())) :
                original.call(pos);
    }
}
