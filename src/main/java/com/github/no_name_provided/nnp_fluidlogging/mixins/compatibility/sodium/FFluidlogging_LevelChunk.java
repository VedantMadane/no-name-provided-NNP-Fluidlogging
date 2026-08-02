package com.github.no_name_provided.nnp_fluidlogging.mixins.compatibility.sodium;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * Important mixin - Does most of the heavy lifting of making fluidstate checks prefer our data structure. Some other
 * mixins ultimately boil down to replacing hardcoded BlockState#getFluidState with calls to this.
 */
@MethodsReturnNonnullByDefault
@Mixin(LevelChunk.class)
abstract class FFluidlogging_LevelChunk extends ChunkAccess {
    @Final @Shadow
    Level level;
    
    @Shadow
    private boolean loaded;
    
    @Shadow
    public abstract ChunkStatus getPersistedStatus();
    
    private FFluidlogging_LevelChunk(ChunkPos pos, UpgradeData data, LevelHeightAccessor heightGetter, Registry<Biome> biomeRegistry, long rand, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(pos, data, heightGetter, biomeRegistry, rand, levelChunkSections, blendingData);
    }
    
    /**
     * Does most of the heavy lifting of making functional fluidstate checks prefer our attachment over the cached,
     * hardcoded blockstate default.
     * <p>
     * Heavily borrows from vanilla. Large performance impact.
     * </p>
     */
    @WrapMethod(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;")
    private FluidState nnp_f_fluidlogging_getFluidState(int x, int y, int z, Operation<FluidState> original) {
        // This may be a significant source of lag, since we're bypassing the vanilla
        // section-by-section approach, which skips empty sections entirely. However,
        // the majority of the lag was fixed by using correct (section) coordinates
        
        // We need the isClientSide check here for Sodium compat, since the client doesn't bother to set isLoaded
        // to true (tracked chunks are always loaded) and Sodium defers to this method in situations where vanilla doesn't
        if ((loaded || (level.isClientSide() && this.getPersistedStatus() == ChunkStatus.FULL)) && level.hasChunk(chunkPos.x, chunkPos.z)) {
            FluidStates states = getData(FLUID_STATES);
            
            // Might have a recursion issue somewhere with this default value
            return states.getOrDefault(new BlockPos(x, y, z), original.call(x, y, z));
        } else {
            
            return original.call(x, y, z);
        }
    }
}
