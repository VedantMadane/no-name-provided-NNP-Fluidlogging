package com.github.no_name_provided.nnp_fluidlogging.common.data_maps.entries;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps;
import com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Define (and statically expose) our default {@link FDataMaps#BLOCKSTATE_FLUID_LEVEL_LIMITS} entries.
 */
public class FluidLevelCallbacks {
    public static DeferredRegister<BiFunction<BlockState, FluidType, Integer>> FLUID_LEVEL_CALLBACKS = DeferredRegister.create(
            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY,
            MODID
    );
    /**
     * Static reference for constant fluid level limit callbacks.
     */
    public static List<Supplier<BiFunction<BlockState, FluidType, Integer>>> CONSTANTS = new ArrayList<>();
    
    /**
     * Define and register minimum fluid level for slabs.
     */
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> SLAB_MIN = FLUID_LEVEL_CALLBACKS.register(
            "slab_min",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
                
                return isBottom ? 5 : 0;
            }
    );
    
    /**
     * Define and register maximum fluid level for slabs.
     */
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> SLAB_MAX = FLUID_LEVEL_CALLBACKS.register(
            "slab_max",
            () -> (blockState, unusedType) -> {
                if (blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {

                    return 0;
                }
                boolean isBottom = blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
                
                return isBottom ? 8 : 4;
            }
    );
    
    /**
     * Define and register minimum fluid level for stairs.
     */
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> STAIR_MIN = FLUID_LEVEL_CALLBACKS.register(
            "stair_min",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(StairBlock.HALF) == Half.BOTTOM;
                
                return isBottom ? 5 : 0;
            }
    );
    
    /**
     * Define and register maximum fluid level for stairs.
     */
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> STAIR_MAX = FLUID_LEVEL_CALLBACKS.register(
            "stair_max",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(StairBlock.HALF) == Half.BOTTOM;
                
                return isBottom ? 8 : 4;
            }
    );
    
    /**
     * Utility method to define constant fluid level limits.
     */
    public static BiFunction<BlockState, FluidType, Integer> generateConstant(Integer constant) {
        
        return (unusedState, unusedType) -> constant;
    }
    
    /**
     * Define and register constant limit callbacks.
     */
    public static void populateConstants() {
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            CONSTANTS.add(
                    FLUID_LEVEL_CALLBACKS.register(
                            "constant_" + i,
                            () -> generateConstant(finalI)
                    )
            );
        }
    }
    
    /**
     * Queue up our deferred register for registration... because there's a reason we would want to make a deferred
     * register and then <i>not</i> do anything with it. #BlameTheNeoForgedTeam.
     */
    public static void register(IEventBus modBus) {
        populateConstants();
        
        FLUID_LEVEL_CALLBACKS.register(modBus);
    }
}
