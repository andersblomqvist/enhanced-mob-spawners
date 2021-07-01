package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

/**
 * 	"Event" for player place block.
 * 	
 * 	This is where we remove the Pig from the Spawner Block when placed down by a player.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin (BlockItem.class)
public class PlayerPlaceBlockMixin {
	
	@Inject(
			at = @At("TAIL"),
			method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", 
			cancellable = true
	)
	private boolean place(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {

		World world = context.getWorld();
		
		if(!(world instanceof ServerWorld))
			return true;
		
		if(state.getBlock() instanceof SpawnerBlock) {
			
			BlockPos pos = context.getBlockPos();
			MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(pos);
			MobSpawnerLogic logic = spawner.getLogic();
			
			// Replace the entity inside the spawner with default entity
			logic.setEntityId(EntityType.AREA_EFFECT_CLOUD);
			spawner.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
		}
		
		return true;
	}
}
