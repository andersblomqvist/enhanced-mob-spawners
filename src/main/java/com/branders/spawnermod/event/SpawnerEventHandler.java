package com.branders.spawnermod.event;

import java.util.Optional;
import java.util.Random;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.SpawnerModConfig;
import com.branders.spawnermod.item.SpawnerKeyItem;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerEggDrop;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 	Handles all the events regarding the mob spawner and entities.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@EventBusSubscriber
public class SpawnerEventHandler {
	
	private Random random = new Random();
	private EntityType<?> defaultEntityType = EntityType.AREA_EFFECT_CLOUD;
	
    /**
     * 	Prevent XP drop when spawner is destroyed with silk touch and return Spawner Block
     */
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
    	
    	// Check if a spawner broke
    	if(event.getState().getBlock() == Blocks.SPAWNER) {
    		
    		ListNBT list = event.getPlayer().getMainHandItem().getEnchantmentTags();
    		
    		// Check if we broke the spawner with silk touch and if it's not disabled in config
    		if(checkSilkTouch(list) && !SpawnerModConfig.GENERAL.disable_silk_touch.get()) {
    			
    			// Set 0 EXP
    			event.setExpToDrop(0);
    			
    			if(!SpawnerModConfig.GENERAL.disable_egg_removal_from_spawner.get())
    				dropMonsterEgg(event.getPos(), (World)event.getWorld());
		    	
    			// Return Spawner Block
    			ItemStack itemStack = new ItemStack(Blocks.SPAWNER.asItem());
    			ItemEntity entityItem = new ItemEntity(
    					(World)event.getWorld(),
    					event.getPos().getX(),
    					event.getPos().getY(),
    					event.getPos().getZ(),
    					itemStack
    			);
    			event.getWorld().addFreshEntity(entityItem);
    		}	
    	}
    }
    
    /**
     * 	Used to replace entity in spawner when block placed down by player
     */
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
    	
    	// Leave if we did not place down a spawner
    	if(event.getState().getBlock() != Blocks.SPAWNER 
    			|| !(event.getEntity() instanceof PlayerEntity))
    		return;
    	
    	World world = (World) event.getWorld();
    	BlockPos pos = event.getPos();
    	
    	MobSpawnerTileEntity tileentity = (MobSpawnerTileEntity)world.getBlockEntity(pos);
    	tileentity.getSpawner().setEntityId(defaultEntityType);
    	
    	tileentity.setChanged();
    	world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    /**
     * 	Check if Spawner block was powered by redstone or not. Used to disable
     * 	or enable the spawner.
     */
    @SubscribeEvent
    public void onNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
    	
    	// Leave if it wasn't a spawner block
    	if(event.getState().getBlock() != Blocks.SPAWNER)
    		return;
    	
    	World world = (World)event.getWorld();
    	BlockPos pos = event.getPos();
    	
    	MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getBlockEntity(pos);
		AbstractSpawner logic = spawner.getSpawner();
		CompoundNBT nbt = new CompoundNBT();
		
		// Get current spawner config values
		nbt = logic.save(nbt);
		
		/**
		 * 	Fixes bug where onBlockPlaced and onNotify both gets called when a
		 * 	player places down a spawner. This creates a problem where the egg
		 * 	would disappear after its first spawn.
		 * 
		 * 	This fix checks if the entity is "empty" (area effect cloud) and if
		 * 	so - we cancel this event. When placing down a spawner, the entity
		 * 	will always be the cloud and when checking for redstone, it will most
		 * 	likely not be a cloud.
		 */
		CompoundNBT data = nbt.getCompound("SpawnData");
		Optional<EntityType<?>> optional = EntityType.by(data);
		if(optional.isPresent()) { 
			if(optional.get().equals(EntityType.AREA_EFFECT_CLOUD)) {
				event.setCanceled(true);
				return;
			}
		}
		
    	// Check redstone power
    	if(world.hasNeighborSignal(pos))
    	{
    		short value = nbt.getShort("RequiredPlayerRange");
    		
    		// If spawner got disabled via GUI and then we toggle off by redstone
    		// we don't need to do this.
    		if(nbt.getShort("SpawnRange") > 4)
    			return;
    		
    		// Read current range and save it temporary in SpawnRange field
    		nbt.putShort("SpawnRange", value);
    		
    		// Turn off spawner
    		nbt.putShort("RequiredPlayerRange", (short) 0);
    	}
    		
    	else
    	{
    		// Read what the previus range was (before this spawner was set to range = 0)
    		short pr = nbt.getShort("SpawnRange");
    		
    		// If spawner was activated via GUI before, then we dont need to do this
    		if(pr <= 4)
    			return;
    		
    		// Set the range backt to what it was
    		nbt.putShort("RequiredPlayerRange", pr);
    		
    		// Set SpawnRange back to default=4
    		nbt.putShort("SpawnRange", (short) 4);
    	}
    		
    	
    	// Update block
    	logic.load(nbt);
    	spawner.setChanged();
    	BlockState blockstate = world.getBlockState(pos);
    	world.sendBlockUpdated(pos, blockstate, blockstate, 3);
    }
    
    /**
     * 	Enables mobs to have a small chance to drop an egg
     */
    @SubscribeEvent
    public void onMobDrop(LivingDropsEvent event) {	
    	if(random.nextFloat() > SpawnerModConfig.GENERAL.monster_egg_drop_chance.get() / 100)
    		return;
    	
    	Entity entity = event.getEntity();
    	EntityType<?> entityType = entity.getType();
    	ItemStack itemStack;
    	
    	// Leave if a player died.
    	if(entityType.equals(EntityType.PLAYER))
    		return;
    	else if (entityType.equals(EntityType.IRON_GOLEM))
    		itemStack = new ItemStack(SpawnerMod.iron_golem_spawn_egg);
    	else
    		itemStack = new ItemStack(ForgeRegistries.ITEMS
    				.getValue(new ResourceLocation(entityType.getRegistryName() + "_spawn_egg")));
		
		// Add monster egg to drops
		event.getDrops().add(new ItemEntity(
				entity.level, 
				entity.getX(), 
				entity.getY(), 
				entity.getZ(), 
				itemStack));
    }
    
    
    /**
     * 	Event when player interacts with block.
     * 	Enables so that the player can right click a spawner to get its egg.
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {   
    	Item item = event.getItemStack().getItem();
    	
    	// Leave if we are client and if we are holding a block. Also prevent off hand action
    	if(item instanceof BlockItem || 
    			item instanceof SpawnEggItem || 
    			item instanceof SpawnerKeyItem || 
    			event.getHand() == Hand.OFF_HAND)
    		return;
    	
    	World world = event.getWorld();
    	BlockPos blockpos = event.getPos();
    	
    	// Leave if we didn't right click a spawner block
		if(world.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return;
		
		// Leave if server
		if(!world.isClientSide || event.getPlayer().isSpectator())
			return;
		
		// Send Network message
		SpawnerModPacketHandler.INSTANCE.sendToServer(new SyncSpawnerEggDrop(blockpos));
    }
   
    /**
     * 	Drops the Monster Egg which is inside the spawner when the spawner is harvested.
     * 	This is only server side.
     * 
     * 	@param pos Spawner block position
     * 	@param world World reference for spawning
     */
    private void dropMonsterEgg(BlockPos pos, World world) {
    	BlockState blockstate = world.getBlockState(pos);
		MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getBlockEntity(pos);
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
			itemStack = new ItemStack(
					ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")));
		
		// Get random fly-out position offsets
		double d0 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
        double d1 = (double)(world.random.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
        double d2 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
        
        // Create entity item
        ItemEntity entityItem = new ItemEntity(
        		world, 
        		(double)pos.getX() + d0, 
        		(double)pos.getY() + d1, 
        		(double)pos.getZ() + d2, 
        		itemStack);
		entityItem.setDefaultPickUpDelay();
		
		// Spawn entity item (egg)
		world.addFreshEntity(entityItem);
		
		// Replace the entity inside the spawner with default entity
		logic.setEntityId(EntityType.AREA_EFFECT_CLOUD);
		spawner.setChanged();
		world.sendBlockUpdated(pos, blockstate, blockstate, 3);
    }
    
    /**
     * 	Check a tools item enchantment list contains Silk Touch enchant
     * 	I don't know if there's a better way to do this	
     * 
     * 	@param NBTTagList of enchantment
     * 	@return true/false
     */
    private boolean checkSilkTouch(ListNBT list) {
    	// Check list string contains silk touch
		if(list.getAsString().indexOf("minecraft:silk_touch") != -1)
			return true;
		else
			return false;
    }
}
