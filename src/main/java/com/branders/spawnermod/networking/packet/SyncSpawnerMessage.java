package com.branders.spawnermod.networking.packet;

import com.branders.spawnermod.SpawnerMod;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

/**
 * 	Sync the Spawner Config GUI to the actual spawner block.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerMessage extends NetworkPacket {

	public static final Identifier ID = new Identifier(SpawnerMod.MOD_ID, "packet.sync_spawner_message");
	
	public SyncSpawnerMessage(BlockPos pos, short delay, short spawnCount, short requiredPlayerRange, short maxNearbyEntities, short minSpawnDelay, short maxSpawnDelay) {
		this.writeBlockPos(pos);
		this.writeShort(delay);
		this.writeShort(minSpawnDelay);
		this.writeShort(maxSpawnDelay);
		this.writeShort(spawnCount);
		this.writeShort(maxNearbyEntities);
		this.writeShort(requiredPlayerRange);
	}
	
	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		
		BlockPos pos = buf.readBlockPos();
		short delay = buf.readShort();
		short minSpawnDelay = buf.readShort();
		short maxSpawnDelay = buf.readShort();
		short spawnCount = buf.readShort();
		short maxNearbyEntities = buf.readShort();
		short requiredPlayerRange = buf.readShort();
		
		server.execute(() -> {
			
			World world = player.world;
	    	
	    	if(world != null)
	    	{
	    		MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)world.getBlockEntity(pos);
	    		MobSpawnerLogic logic = spawner.getLogic();
	        	BlockState blockstate = world.getBlockState(pos);
	        	
	        	NbtCompound nbt = new NbtCompound();
	        	
	        	nbt = logic.writeNbt(world, pos, nbt);
	        	
	        	if(requiredPlayerRange == 0)
	        		nbt.putShort("SpawnRange", nbt.getShort("RequiredPlayerRange"));
	        	else
	        		nbt.putShort("SpawnRange", (short) 4);
	        	
	        	// Change NBT values
	        	nbt.putShort("Delay", delay);
	        	nbt.putShort("SpawnCount", spawnCount);
	        	nbt.putShort("RequiredPlayerRange", requiredPlayerRange);
	        	nbt.putShort("MaxNearbyEntities", maxNearbyEntities);
	        	nbt.putShort("MinSpawnDelay", minSpawnDelay);
	        	nbt.putShort("MaxSpawnDelay", maxSpawnDelay);
	        	
	        	// Update block
	        	logic.readNbt(world, pos, nbt);
	        	spawner.markDirty();
	    		world.updateListeners(pos, blockstate, blockstate, 3);
	    	}
		});
	}
	
	@Override
	public Identifier getId() {
		return ID;
	}
}
