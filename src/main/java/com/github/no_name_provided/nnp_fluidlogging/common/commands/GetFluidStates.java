package com.github.no_name_provided.nnp_fluidlogging.common.commands;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
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
import net.minecraft.world.level.material.FlowingFluid;

import javax.annotation.Nonnull;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;
import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * In-game command to print saved FluidStates to the chat log. Intended for debugging.
 */
public class GetFluidStates {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(MODID)
                        .then(Commands.literal("getFluidStates")
                                // Current chunk is default
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player != null) {
                                        
                                        return GetFluidStates.reportFluidStates(
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
                                                .executes(context -> GetFluidStates.reportFluidStates(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "chunkX"),
                                                        IntegerArgumentType.getInteger(context, "chunkZ")))))
                        ));
    }
    
    /**
     * Unpack command context and handle errors.
     */
    private static int reportFluidStates(CommandContext<CommandSourceStack> context, int chunkX, int chunkY) {
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
     * The code that actually does the work - grabs and prints data.
     */
    private static void execute(@Nonnull ServerPlayer player, ChunkAccess chunk) {
        player.sendSystemMessage(Component.literal("Checking fluids on the " + (player.level().isClientSide() ? "client" : "server") + " side.").withStyle(ChatFormatting.BLUE));
        FluidStates states = chunk.getData(FLUID_STATES);
        if (states.map().isEmpty()) {
            player.sendSystemMessage(Component.literal("No fluids to report. (Remember, vanilla WATERlogged blocks aren't managed by this mod.)"));
        } else {
            states.map().forEach((pos, state) ->
                    player.sendSystemMessage(Component.literal("Pos: " + pos.toShortString() + "; Fluid: " + state.getFluidType() + ", Level: " + (state.isSource() ? "FULL" : state.getValue(FlowingFluid.LEVEL))))
            );
        }
    }
}
