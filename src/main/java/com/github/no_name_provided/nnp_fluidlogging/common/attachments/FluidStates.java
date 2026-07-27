package com.github.no_name_provided.nnp_fluidlogging.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Exposes a map from BlockPos to FluidState. Not prepopulated - a position without a fluidlogged block is a missing
 * entry. Not intended for fully waterlogged blocks (vanilla waterlogging).
 * <p>
 * This is stored in a record because it will be used for attachments, and those must be "immutable".
 * </p>
 * <p>
 * THe internal maps shouldn't be mutated directly unless you know what you're doing. May cause sync errors.
 *     TODO: refactor to protect those maps
 * </p>
 */
public record FluidStates(HashMap<BlockPos, FluidState> map, HashMap<BlockPos, FluidState> unsyncedUpdates) {
    
    /**
     * A codec used to (de)serialize this class for storage/retrieval from memory or JSON.
     */
    public static final Codec<FluidStates> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.unboundedMap(
                                    // Unbound map keys must begin with strings (or things built on them)
                                    Codec.STRING
                                            .xmap(Long::parseLong, String::valueOf)
                                            .xmap(BlockPos::of, BlockPos::asLong),
                                    FluidState.CODEC
                                    // Workaround for overzealous type validation; should probably find a more efficient solution
                            ).xmap(HashMap::new, HashMap::new)
                            .fieldOf("map").forGetter(FluidStates::map)
            ).apply(inst, instance -> new FluidStates(instance, new HashMap<>()))
    );
    
    /**
     * The simplest StreamCodec for this class. Hard to see how it works under the hood, its performance
     * characteristics, and if it has undocumented limitations.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStates> SIMPLE_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
    
    /**
     * The simplest stream codec for FluidState. Hard to see how it works under the hood, its performance
     * characteristics, and if it has undocumented limitations.
     */
    private static final StreamCodec<RegistryFriendlyByteBuf, FluidState> FLUID_STATE_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(FluidState.CODEC);
    
    /**
     * A simple stream codec for this class' HashMaps. A bit hard to see how it works under the hood, its performance
     * characteristics, and if it has undocumented limitations.
     */
    public static StreamCodec<RegistryFriendlyByteBuf, HashMap<BlockPos, FluidState>> STREAM_CODEC_FOR_UPDATES =
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    FLUID_STATE_STREAM_CODEC
            );
    
    /**
     * Handwritten en/decoder combination for this class' HashMaps with known properties. Always writes at least one
     * VAR_INT to buffer. Explicitly supports empty maps, but we should still avoid encoding them because that would
     * create vacuous update packets.
     */
    public static StreamCodec<RegistryFriendlyByteBuf, HashMap<BlockPos, FluidState>> SAFE_STREAM_CODEC_FOR_UPDATES =
            StreamCodec.of(
                    (buf, map) -> {
                        buf.writeVarInt(map.size());
                        map.forEach((pos, state) -> {
                            BlockPos.STREAM_CODEC.encode(buf, pos);
                            FLUID_STATE_STREAM_CODEC.encode(buf, state);
                        });
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        HashMap<BlockPos, FluidState> updates = new HashMap<>(size);
                        for (int i = 0; i < size; i++) {
                            updates.put(
                                    BlockPos.STREAM_CODEC.decode(buf),
                                    FLUID_STATE_STREAM_CODEC.decode(buf)
                            );
                        }
                        
                        return updates;
                    }
            );
    
    /**
     * Wrapper for Map#put that updates our map of unsynced updates.
     *
     * @param pos   The position with an updated FluidState.
     * @param state The old FluidState at that position (or null, if there was none).
     * @return The new FluidState
     */
    @SuppressWarnings("UnusedReturnValue") // matches signature of wrapped method
    public @Nullable FluidState put(BlockPos pos, FluidState state) {
        map().put(pos, state);
        
        return unsyncedUpdates().put(pos, state);
    }
    
    /**
     * Wrapper for Map#putAll that updates our map of unsynced updates.
     *
     * @param changedEntries Map of entries that have been changed.
     */
    public void putAll(Map<BlockPos, FluidState> changedEntries) {
        map().putAll(changedEntries);
        unsyncedUpdates().putAll(changedEntries);
    }
    
    /**
     * Wrapper for internal map method of same name.
     */
    public @Nullable FluidState get(BlockPos pos) {
        
        return map().get(pos);
    }
    
    /**
     * Wrapper for internal map method of same name.
     */
    public FluidState getOrDefault(BlockPos pos, FluidState defaultState) {
        
        return map().getOrDefault(pos, defaultState);
    }
    
    /**
     * Wrapper for internal map method of same name. Adds empty entry in update map.
     *
     * @return The value that was removed, or null if not present.
     */
    public @Nullable FluidState remove(BlockPos pos) {
        @Nullable FluidState oldState = map().remove(pos);
        if (oldState != null) {
            // Mark removal as an EMPTY fluid state so clients can remove the entry
            unsyncedUpdates().put(pos, Fluids.EMPTY.defaultFluidState());
        }
        
        return oldState;
    }
}
