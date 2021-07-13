package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.SpawnerModConfig;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 	Do monster egg drop when player right clicks a spawner.
 * 	Needs to be done on server which requires a network message
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerEggDrop 
{
	private final BlockPos pos;
	
	public SyncSpawnerEggDrop(BlockPos pos)
	{
		this.pos = pos;
	}
	
	public static void encode(SyncSpawnerEggDrop msg, PacketBuffer buf)
	{
		buf.writeBlockPos(msg.pos);
	}
	
	public static SyncSpawnerEggDrop decode(PacketBuffer buf)
	{
		BlockPos pos = new BlockPos(buf.readBlockPos());
		
		return new SyncSpawnerEggDrop(pos);
	}
	
	public static void handle(SyncSpawnerEggDrop msg, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			
			World world = ctx.get().getSender().level;
			
			if(world != null)
			{
		    	// Leave if disabled in config
		    	if(SpawnerModConfig.GENERAL.disable_egg_removal_from_spawner.get())
		    		return;
		    	
				BlockState blockstate = world.getBlockState(msg.pos);
				MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getBlockEntity(msg.pos);
		    	AbstractSpawner logic = spawner.getSpawner();
		    	
		    	// Get entity ResourceLocation string from spawner by creating a empty compound which we make our 
		    	// spawner logic write to. We can then access what type of entity id the spawner has inside
		    	CompoundNBT nbt = new CompoundNBT();
		    	nbt = logic.save(nbt);
		    	String entity_string = nbt.get("SpawnData").toString();
		    	
		    	// Strips the string
		    	// Example: {id: "minecraft:xxx_xx"} --> minecraft:xxx_xx
		    	entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
		    	entity_string = entity_string.substring(0, entity_string.indexOf("\""));
		    	
				// Leave if the spawner does not contain an egg	
				if(entity_string.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString()))
					return;
				
		    	// Get the entity mob egg and put in an ItemStack
				ItemStack itemStack;
				if(entity_string.contains("iron_golem"))
					itemStack = new ItemStack(SpawnerMod.iron_golem_spawn_egg);
				else
					itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")));
				
				// Get random fly-out position offsets
				double d0 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
		        double d1 = (double)(world.random.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
		        double d2 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
		        
		        // Create entity item
		        ItemEntity entityItem = new ItemEntity(world, (double)msg.pos.getX() + d0, (double)msg.pos.getY() + d1, (double)msg.pos.getZ() + d2, itemStack);
				entityItem.setDefaultPickUpDelay();
				
				// Spawn entity item (egg)
				world.addFreshEntity(entityItem);
				
				// Replace the entity inside the spawner with default entity
				logic.setEntityId(EntityType.AREA_EFFECT_CLOUD);
				spawner.setChanged();
				world.sendBlockUpdated(msg.pos, blockstate, blockstate, 3);
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}
