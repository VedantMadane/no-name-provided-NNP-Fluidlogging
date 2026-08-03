package com.github.no_name_provided.nnp_fluidlogging.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Expose static references to the fluid tags referenced in this mod.
 */
public class NNPFFluidTags {
        public static final TagKey<Fluid> DOES_NOT_LOG = TagKey.create(
                Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath(MODID, "does_not_log")
        );
}
