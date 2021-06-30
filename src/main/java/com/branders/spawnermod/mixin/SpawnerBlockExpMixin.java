package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * 	Injects at the onStacksDropped method in SpawnerBlock to disable the EXP drops when block harvested.
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
					+ "Lnet/minecraft/item/ItemStack;)V", 
			cancellable = true
	)
	public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, CallbackInfo info) {	
		info.cancel();
	}
}
