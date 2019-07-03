package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	    		// Leave if blockpos not loaded, safety measures
	    		if(!world.isBlockLoaded(msg.pos))
	    			return;
	    		
	    		TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getTileEntity(msg.pos);
	        	MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
	        	IBlockState iblockstate = world.getBlockState(msg.pos);	
	        	
	        	NBTTagCompound nbt = new NBTTagCompound();
	        	nbt = logic.writeToNBT(nbt);
	        	
	        	// Change NBT values
	        	nbt.setShort("Delay", msg.delay);
	        	nbt.setShort("SpawnCount", msg.spawnCount);
	        	nbt.setShort("RequiredPlayerRange", msg.requiredPlayerRange);
	        	nbt.setShort("MaxNearbyEntities", msg.maxNearbyEntities);
	        	nbt.setShort("MinSpawnDelay", msg.minSpawnDelay);
	        	nbt.setShort("MaxSpawnDelay", msg.maxSpawnDelay);
	        	
	        	// Update block
	        	logic.readFromNBT(nbt);
	        	spawner.markDirty();
	        	world.notifyBlockUpdate(msg.pos, iblockstate, iblockstate, 3);
	    	}
	    });
	    
	    ctx.get().setPacketHandled(true);
	}
}
