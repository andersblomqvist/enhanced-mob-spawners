package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * 	Network message to handle communication from Client GUI to logical server in order to write
 * 	new NBT values for MobSpawnerBaseLogic.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerMessage 
{
	private final BlockPos pos;
	private short delay;
	private short minSpawnDelay;
	private short maxSpawnDelay;
	private short spawnCount;
	private short maxNearbyEntities;
	private short requiredPlayerRange;
	
	public SyncSpawnerMessage(BlockPos pos, short delay, short spawnCount, short requiredPlayerRange, short maxNearbyEntities, short minSpawnDelay, short maxSpawnDelay)
	{
		this.pos = pos;
		this.delay = delay;
		this.minSpawnDelay = minSpawnDelay;
		this.maxSpawnDelay = maxSpawnDelay;
		this.spawnCount = spawnCount;
		this.maxNearbyEntities = maxNearbyEntities;
		this.requiredPlayerRange = requiredPlayerRange;
	}
	
	public static void encode(SyncSpawnerMessage msg, PacketBuffer buf)
	{
		buf.writeBlockPos(msg.pos);
		
		buf.writeShort(msg.delay);
		buf.writeShort(msg.maxNearbyEntities);
		buf.writeShort(msg.maxSpawnDelay);
		buf.writeShort(msg.minSpawnDelay);
		buf.writeShort(msg.requiredPlayerRange);
		buf.writeShort(msg.spawnCount);
	}
	
	public static SyncSpawnerMessage decode(PacketBuffer buf)
	{
		BlockPos pos = new BlockPos(buf.readBlockPos());
		
		short delay = buf.readShort();
		short maxNearbyEntities = buf.readShort();
		short maxSpawnDelay = buf.readShort();
		short minSpawnDelay = buf.readShort();
		short requiredPlayerRange = buf.readShort();
		short spawnCount = buf.readShort();
		
		return new SyncSpawnerMessage(pos, delay, spawnCount, requiredPlayerRange, maxNearbyEntities, minSpawnDelay, maxSpawnDelay);
	}
	
	public static void handle(SyncSpawnerMessage msg, Supplier<NetworkEvent.Context> ctx) 
	{
		// Do threadsafe work. Change NBT values for spawner logic
	    ctx.get().enqueueWork(() -> {
	    	
	    	// Get world (server world, isRemote is false)
	    	World world = ctx.get().getSender().world;
	    	
	    	if(world != null)
	    	{
	    		MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(msg.pos);
	    		AbstractSpawner logic = spawner.getSpawnerBaseLogic();
	        	BlockState blockstate = world.getBlockState(msg.pos);
	        	
	        	CompoundNBT nbt = new CompoundNBT();
	        	nbt = logic.write(nbt);
	        	
	        	if(msg.requiredPlayerRange == 0)
	        		nbt.putShort("SpawnRange", nbt.getShort("RequiredPlayerRange"));
	        	else
	        		nbt.putShort("SpawnRange", (short) 4);
	        	
	        	// Change NBT values
	        	nbt.putShort("Delay", msg.delay);
	        	nbt.putShort("SpawnCount", msg.spawnCount);
	        	nbt.putShort("RequiredPlayerRange", msg.requiredPlayerRange);
	        	nbt.putShort("MaxNearbyEntities", msg.maxNearbyEntities);
	        	nbt.putShort("MinSpawnDelay", msg.minSpawnDelay);
	        	nbt.putShort("MaxSpawnDelay", msg.maxSpawnDelay);
	        	
	        	// Update block
	        	logic.read(nbt);
	        	spawner.markDirty();
	        	world.notifyBlockUpdate(msg.pos, blockstate, blockstate, 3);
	    	}
	    });
	    
	    ctx.get().setPacketHandled(true);
	}
}
