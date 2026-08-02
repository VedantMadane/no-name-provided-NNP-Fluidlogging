package com.github.no_name_provided.nnp_fluidlogging.common.attachments;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.sync.FluidStatesAttachmentSyncHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Neo only. Create and register attachment types. These reference the data structures we use to store
 * information that needs to be persistent or synchronized.
 */
public class FAttachments {
    public static DeferredRegister<AttachmentType<?>> FATTACHMENTS = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            MODID
    );
    
    /**
     * The attachment we use to store fluid information for logged blocks.
     */
    public static DeferredHolder<AttachmentType<?>, AttachmentType<FluidStates>> FLUID_STATES = FATTACHMENTS.register(
            "fluid_states", () -> AttachmentType.builder(() -> new FluidStates(new HashMap<>(), new HashMap<>()))
                    .sync(new FluidStatesAttachmentSyncHandler())
                    .serialize(FluidStates.CODEC)
                    .build()
    );
    
    /**
     * Convenience method that registers our deferred object(s) with the mod event bus.
     */
    public static void register(IEventBus modBus) {
        FATTACHMENTS.register(modBus);
    }
}
