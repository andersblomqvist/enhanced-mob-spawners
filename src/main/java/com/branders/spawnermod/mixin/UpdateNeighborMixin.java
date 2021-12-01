package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;

@Mixin(Level.class)
public class UpdateNeighborMixin {
	
	@Inject(
			at = @At("HEAD"),
			method = "neighborChanged(Lnet/minecraft/core/BlockPos;"
					+ "Lnet/minecraft/world/level/block/Block"
					+ "Lnet/minecraft/core/BlockPos;)V",
			cancellable = true)
	private void neighborChanged(BlockPos pos, Block sourceBlock, BlockPos neighborPos, CallbackInfo ci) {
		Level world = (Level)(Object)this;
		SpawnerMod.LOGGER.info("NEIGHBOR CHANGED MIXIN");
		if(world.getBlockState(neighborPos).getBlock() instanceof SpawnerBlock)
			SpawnerEventHandler.updateNeighbor(neighborPos, world);
		else if(world.getBlockState(pos).getBlock() instanceof SpawnerBlock)
			SpawnerEventHandler.updateNeighbor(pos, world);
	}
}
