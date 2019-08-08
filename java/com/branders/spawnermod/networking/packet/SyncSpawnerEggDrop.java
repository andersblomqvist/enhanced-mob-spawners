package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

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
 * 	Temp fix for PlayerInteractEvent.RightClickBlock not working on server. Only called on client
 * 	which means we have to send networking
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
			
			World world = ctx.get().getSender().world;
			
			if(world != null)
			{
				BlockState blockstate = world.getBlockState(msg.pos);
				MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(msg.pos);
		    	AbstractSpawner logic = spawner.getSpawnerBaseLogic();
		    	
		    	// Get entity ResourceLocation string from spawner by creating a empty compund which we make our 
		    	// spawner logic write to. We can then access what type of entity id the spawner has inside
		    	CompoundNBT nbt = new CompoundNBT();
		    	nbt = logic.write(nbt);
		    	String entity_string = nbt.get("SpawnData").toString();
		    	
		    	// Strips the string
		    	// Example: {id: "minecraft:xxx_xx"} --> minecraft:xxx_xx
		    	entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
		    	entity_string = entity_string.substring(0, entity_string.indexOf("\""));
		    	
				// Leave if the spawner does not contain an egg	
				if(entity_string.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString()))
					return;
				
		    	// Get the entity mob egg and put in an ItemStack
				ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")));
				
				// Get random fly-out position offsets
				double d0 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
		        double d1 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
		        double d2 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
		        
		        // Create entity item
		        ItemEntity entityItem = new ItemEntity(world, (double)msg.pos.getX() + d0, (double)msg.pos.getY() + d1, (double)msg.pos.getZ() + d2, itemStack);
				entityItem.setDefaultPickupDelay();
				
				// Spawn entity item (egg)
				world.addEntity(entityItem);
				
				// Replace the entity inside the spawner with default entity
				logic.setEntityType(EntityType.AREA_EFFECT_CLOUD);
				spawner.markDirty();
				world.notifyBlockUpdate(msg.pos, blockstate, blockstate, 3);
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}
