package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.event.EventHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * 	Injects at the onStacksDropped method in SpawnerBlock to disable the EXP drops when block harvested.
 * 
 * 	The exp is added back in from {@link EventHandler}
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(SpawnerBlock.class)
public class SpawnerBlockExpMixin {


    @Inject(
            at = @At("HEAD"), 
            method = "onStacksDropped("
                    + "Lnet/minecraft/block/BlockState;"
                    + "Lnet/minecraft/server/world/ServerWorld;"
                    + "Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/item/ItemStack;"
                    + "Z)"
                    + "V", 
                    cancellable = true
            )
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, boolean dropExp, CallbackInfo info) {	
        info.cancel();
    }
}
