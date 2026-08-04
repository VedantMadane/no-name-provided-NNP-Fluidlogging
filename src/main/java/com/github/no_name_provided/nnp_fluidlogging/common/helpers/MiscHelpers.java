package com.github.no_name_provided.nnp_fluidlogging.common.helpers;

import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;
import static com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig.explicitlyDoNotSupportWorldgen;

public class MiscHelpers {
    
    /**
     * (Failed) attempt to prevent our attachments from syncing during worldgen/too early in loading. Consider
     * removing.
     */
    public static <T> void safeSyncChunkAttachment(ChunkAccess chunk, DeferredHolder<AttachmentType<?>, AttachmentType<T>> type) {
        if (
                chunk instanceof LevelChunk levelChunk &&
                        levelChunk.getLevel().getServer() instanceof MinecraftServer server &&
                        server.isSameThread()
        ) {
            levelChunk.syncData(type);
        }
    }
    
    /**
     * This shouldn't be necessary on the client, unless the client is for some reason requesting information about
     * unloaded chunks... in which case the #isInLevel check will be insufficient, as its always true on the client, and
     * we'll need to check chunk status.
     */
    public static <T> Optional<T> safeGetChunkAttachment(AttachmentType<T> type, ChunkAccess chunk) {
        if (chunk instanceof LevelChunk levelChunk && levelChunk.isInLevel()) {
            
            return Optional.of(chunk.getData(type));
        } else {
            
            return Optional.empty();
        }
    }
    
    public static void updateClientLightLevels(BlockPos pos, int lightLevel, ServerLevel sLevel, boolean requireMainThread) {
        // Filter out updates that are sent too early
        if (requireMainThread && !sLevel.getServer().isSameThread()) {
            
            return;
        }
        
        ChunkPos chunkPos = new ChunkPos(pos);
        sLevel.getPlayers(player ->
                player.level().equals(sLevel) &&
                        sLevel.getChunkSource().chunkMap.isChunkTracked(player, chunkPos.x, chunkPos.z)
        ).forEach(player ->
                player.connection.send(new AuxLightManagerUpdatePayload(
                        lightLevel,
                        pos.asLong()))
        );
    }
    
    public static void fixScheduledFluidTick(LevelAccessor level, BlockPos pos) {
        if (!(explicitlyDoNotSupportWorldgen && level instanceof WorldGenRegion)) {
            // Use our attachment when available
            Fluid trueFluid = level.getChunk(pos).getData(FLUID_STATES).getOrDefault(
                    pos,
                    level.getBlockState(pos).getFluidState()).getType();
            level.scheduleTick(pos, trueFluid, trueFluid.getTickDelay(level));
        } else {
            // vanilla call
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }
}
