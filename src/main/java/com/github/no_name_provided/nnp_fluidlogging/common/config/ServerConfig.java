package com.github.no_name_provided.nnp_fluidlogging.common.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Define our server config, which is synced to (and overwrites) the corresponding config on clients when they join.
 */
@EventBusSubscriber(modid = MODID)
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_FLUIDS =
            BUILDER.comment("Which fluids should be blacklisted? This isn't retroactive.",
                            "Empty by default. Water cannot be blacklisted.")
                    .defineListAllowEmpty(
                            "fluid_blacklist",
                            new ArrayList<>(),
                            ServerConfig::supplyFluid,
                            ServerConfig::validateFluid
                    );
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BLOCKS =
            BUILDER.comment("Which blocks should be blacklisted? This isn't retroactive.",
                            "Only applies to SimpleWaterLoggableBlock variants (stairs, slabs, etc.).",
                            "Remember, flowing fluids and source blocks are blacklisted separately (they're distinct fluids as far as Minecraft is concerned).")
                    .defineListAllowEmpty(
                            "block_blacklist",
                            new ArrayList<>(),
                            () -> BuiltInRegistries.BLOCK.getKey(Blocks.COBBLESTONE_STAIRS).toString(),
                            ServerConfig::validateBlock
                    );
    private static final ModConfigSpec.BooleanValue CONSIDER_FLUID_LIGHT_LEVEL =
            BUILDER.comment("Should fluidlogged blocks emit the higher of blocklight and fluid light?",
                            "Significant performance penalty.")
                    .define("consider_fluid_light", true);
    private static final ModConfigSpec.BooleanValue FLOWING_FLUIDS_CAN_LOG =
            BUILDER.comment("Can partial fluid blocks waterlog")
                    .define("flowing_fluids_log", false);
    private static final ModConfigSpec.BooleanValue EXPLICITLY_DO_NOT_SUPPORT_WORLDGEN =
            BUILDER.comment("Can we assume you aren't trying to worldgen blocks logged with non-vanilla fluids? There may be a significant performance penalty if we can't.")
                    .define("explicitly_do_not_support_worldgen", true);
    private static final ModConfigSpec.BooleanValue FORCE_CHUNK_UPDATES =
            BUILDER.comment("Should we force chunk updates (resolves sync issues, but may cause stability problems)")
                    .define("force_chunk_updates", false);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static List<? extends String> blacklistedFluids;
    public static List<? extends String> blacklistedBlocks;
    public static boolean considerFluidLightLevel;
    public static boolean flowingFluidsCanLog;
    public static boolean explicitlyDoNotSupportWorldgen;
    public static boolean forceChunkUpdates;
    
    protected static String supplyFluid() {
        
        //noinspection OptionalGetWithoutIsPresent - We're literally grabbing statically initialized fields. It'll be fine (TM).
        return BuiltInRegistries.FLUID.getResourceKey(Fluids.LAVA).get().location().toString();
    }
    
    protected static boolean validateFluid(Object element) {
        if (element instanceof String fluidString) {
            // Returns null on failure
            ResourceLocation loc = ResourceLocation.tryParse(fluidString);
            
            return loc != null && BuiltInRegistries.FLUID.containsKey(loc) &&
                    // We can't really blacklist water, since we aren't fiddling with worldgen and we special case vanilla waterlogging in so many spots
                    !fluidString.equals("minecraft:water") &&
                    !fluidString.equals("minecraft:flowing_water");
        }
        
        return false;
    }
    
    protected static boolean validateBlock(Object element) {
        if (element instanceof String blockString) {
            // Returns null on failure
            ResourceLocation loc = ResourceLocation.tryParse(blockString);
            
            return loc != null && BuiltInRegistries.BLOCK.containsKey(loc);
        }
        
        return false;
    }
    
    @SubscribeEvent
    static void onConfigUpdate(final ModConfigEvent event) {
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getType() == ModConfig.Type.SERVER) {
            blacklistedFluids = BLACKLISTED_FLUIDS.get();
            blacklistedBlocks = BLACKLISTED_BLOCKS.get();
            considerFluidLightLevel = CONSIDER_FLUID_LIGHT_LEVEL.get();
            flowingFluidsCanLog = FLOWING_FLUIDS_CAN_LOG.get();
            explicitlyDoNotSupportWorldgen = EXPLICITLY_DO_NOT_SUPPORT_WORLDGEN.get();
            forceChunkUpdates = FORCE_CHUNK_UPDATES.get();
        }
    }
    
    public static void registerExtensionPoint(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
