package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
abstract class FFluidlogging_Entity {
    
    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
    at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"))
    private boolean nnp_f_fluidlogging_move(Stream<BlockState> instance, Predicate<? super BlockState> predicate, Operation<Boolean> original) {
        
        return !((Entity)((Object)this)).isInLava() && original.call(instance, predicate);
    }
}
