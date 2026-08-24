package com.github.no_name_provided.nnp_fluidlogging.common.commands;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

import javax.annotation.Nonnull;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;
import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * In-game command to clear saved FluidStates. Intended for debugging.
 */
public class ClearFluidStates {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(MODID)
                        .then(Commands.literal("clearFluidStates")
                                .requires(stack -> stack.hasPermission(Commands.LEVEL_ADMINS))
                                // Current chunk is default
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player != null) {
                                        
                                        return ClearFluidStates.clearFluidStates(
                                                context,
                                                new ChunkPos(player.getOnPos()).x,
                                                new ChunkPos(player.getOnPos()).z
                                        );
                                    } else {
                                        
                                        return 1;
                                    }
                                })
                                // But we can specify any chunk
                                .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                                        .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                                                .executes(context -> ClearFluidStates.clearFluidStates(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "chunkX"),
                                                        IntegerArgumentType.getInteger(context, "chunkZ")))))
                        ));
    }
    
    /**
     * Unpack command context and handle errors.
     */
    private static int clearFluidStates(CommandContext<CommandSourceStack> context, int chunkX, int chunkY) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            
            execute(player, player.level().getChunk(chunkX, chunkY));
        } else {
            LogUtils.getLogger().error("GetFluidStates command called by null player - unable to write states to chat.");
            
            return 1;
        }
        
        return 0;
    }
    
    /**
     * The code that actually does the work - removes and syncs the FluidStates.
     */
    private static void execute(@Nonnull ServerPlayer player, ChunkAccess chunk) {
        player.sendSystemMessage(Component.literal("Clearing fluids on the " + (player.level().isClientSide() ? "client" : "server") + " side.").withStyle(ChatFormatting.BLUE));
        AuxiliaryLightManager lManager = chunk.getAuxLightManager(chunk.getPos());
        // Make sure our attachment mutates on the main thread
        player.server.execute(() -> {
            FluidStates states = chunk.getData(FLUID_STATES);
            states.map().forEach((pos, state) -> {
                // Handle side effects
                if (state.getFluidType().getLightLevel() != 0) {
                    if (lManager != null) {
                        lManager.removeLightAt(pos);
                    } else {
                        LogUtils.getLogger().error("Unable to clear (server) light level at {}. Make sure the chunk is fully loaded.", pos);
                    }
                    player.connection.send(new AuxLightManagerUpdatePayload(0, pos.asLong()));
                }
                // Queue client updates
                states.unsyncedUpdates().put(pos, Fluids.EMPTY.defaultFluidState());
            });
            states.map().clear();
            chunk.setUnsaved(true);
            player.sendSystemMessage(Component.literal("Syncing attachment").withStyle(ChatFormatting.BLUE));
            chunk.syncData(FLUID_STATES);
        });
    }
}
