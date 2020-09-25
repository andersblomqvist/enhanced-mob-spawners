package com.branders.spawnermod.event;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 	Currently not in use.
 * 
 * 	@author Anders Blomqvist
 */
@EventBusSubscriber
public class WorldEvents
{
	// Keep track of the world spawned Mob Spawners
	private ArrayList<BlockPos> spawners = new ArrayList<BlockPos>();
	
	/**
	 * 	Load chunk event for changing world spawned mob spawners.
	 * 
	 * 	@param event {@link ChunkEvent.Load}
	 */
	@SubscribeEvent
	public void onChunkLoadEvent(ChunkEvent.Load event) {
		
		// Get all tileentities inside this chunk
		Chunk chunk = (Chunk) event.getChunk();
		Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
		
		// Go through all the keys and check if their values are mob spawners
		for(BlockPos k : map.keySet()) {
			if(map.get(k) instanceof MobSpawnerTileEntity) {
				MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) map.get(k);
				AbstractSpawner logic = spawner.getSpawnerBaseLogic();
				
				// Read NBT to find out if spawner has been played out by player
				// or by the world.
		    	CompoundNBT nbt = new CompoundNBT();
		    	nbt = logic.write(nbt);
		    	short placedByPlayer = nbt.getShort("MaxNearbyEntities");
		    	
		    	if(placedByPlayer == 6)
		    		spawners.add(spawner.getPos());
			}
		}
	}
	
	/**
	 * 	Event fired when the player enters the world
	 * 	@param event
	 
	@SubscribeEvent
	public void otherEvent(PlayerEvent.PlayerLoggedInEvent event) {
		for(BlockPos pos : spawners) {
			World world = event.getEntity().getEntityWorld();
			MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(pos);
    		AbstractSpawner logic = spawner.getSpawnerBaseLogic();
        	BlockState blockstate = world.getBlockState(pos);	
        	
        	CompoundNBT nbt = new CompoundNBT();
        	nbt = logic.write(nbt);
        	
        	// Change NBT values
        	nbt.putShort("RequiredPlayerRange", (short)128);
        	
        	// Set this to true so we only change the spawner once. We don't want to overwrite
        	// the data if a player has found the spawner and modified its stats.
        	//
        	// Funkar inte, behöver ett sätt att identifiera en world spawnad spawner och player placed spawner
        	// 
        	nbt.putShort("MaxNearbyEntities", (short)7);
        	
        	// Update block
        	logic.read(nbt);
        	spawner.markDirty();
        	world.notifyBlockUpdate(pos, blockstate, blockstate, 3);
        	System.out.println("Finished with one spawner");
		}
	}
	*/
}