package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import com.branders.spawnermod.item.SpawnerKeyItem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

/**
 * 	Network message to handle communication from Client GUI to logical server in order to write
 * 	new NBT values for MobSpawnerBaseLogic.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerMessage {
	private final BlockPos pos;
	private short delay;
	private short minSpawnDelay;
	private short maxSpawnDelay;
	private short spawnCount;
	private short maxNearbyEntities;
	private short requiredPlayerRange;
	
	public SyncSpawnerMessage(BlockPos pos, short delay, short spawnCount, short requiredPlayerRange, short maxNearbyEntities, short minSpawnDelay, short maxSpawnDelay) {
		this.pos = pos;
		this.delay = delay;
		this.minSpawnDelay = minSpawnDelay;
		this.maxSpawnDelay = maxSpawnDelay;
		this.spawnCount = spawnCount;
		this.maxNearbyEntities = maxNearbyEntities;
		this.requiredPlayerRange = requiredPlayerRange;
	}
	
	public static void encode(SyncSpawnerMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		
		buf.writeShort(msg.delay);
		buf.writeShort(msg.maxNearbyEntities);
		buf.writeShort(msg.maxSpawnDelay);
		buf.writeShort(msg.minSpawnDelay);
		buf.writeShort(msg.requiredPlayerRange);
		buf.writeShort(msg.spawnCount);
	}
	
	public static SyncSpawnerMessage decode(FriendlyByteBuf buf) {
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
	    	Level level = ctx.get().getSender().level;
	    	
	    	if(level != null)
	    	{
	    		SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(msg.pos);
	    		BaseSpawner logic = spawner.getSpawner();
	        	BlockState blockstate = level.getBlockState(msg.pos);
	        	
	        	CompoundTag nbt = new CompoundTag();
	        	nbt = logic.save(nbt);
	        	
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
	        	logic.load(level, msg.pos, nbt);
	        	spawner.setChanged();
	        	level.sendBlockUpdated(msg.pos, blockstate, blockstate, 3);
	        	
	        	ItemStack stack = ctx.get().getSender().getMainHandItem();
	        	if(stack.getItem() instanceof SpawnerKeyItem) {
	        		stack.hurtAndBreak(1, (LivingEntity)ctx.get().getSender(), (player) -> {
	        			player.broadcastBreakEvent(ctx.get().getSender().getUsedItemHand());
	        		});
	        		level.levelEvent(LevelEvent.PARTICLES_WAX_OFF, msg.pos, 0);
	        	}
	    	}
	    });
	    
	    ctx.get().setPacketHandled(true);
	}
}
