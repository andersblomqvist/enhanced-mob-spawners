package com.branders.spawnermod.event;

import java.util.Optional;
import java.util.Random;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.item.SpawnerKeyItem;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerEggDrop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    		
    		ListTag list = event.getPlayer().getMainHandItem().getEnchantmentTags();
    		
    		// Check if we broke the spawner with silk touch and if it's not disabled in config
    		if(checkSilkTouch(list) && ConfigValues.get("disable_silk_touch") == 0) {
    			
    			// Set 0 EXP
    			event.setExpToDrop(0);
    			
    			if(ConfigValues.get("disable_egg_removal_from_spawner") == 0)
    				dropMonsterEgg(event.getPos(), (Level)event.getWorld());
		    	
    			// Return Spawner Block
    			ItemStack itemStack = new ItemStack(Blocks.SPAWNER.asItem());
    			ItemEntity entityItem = new ItemEntity(
    					(Level)event.getWorld(),
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
    			|| !(event.getEntity() instanceof Player))
    		return;
    	
    	Level world = (Level) event.getWorld();
    	BlockPos pos = event.getPos();
    	
    	SpawnerBlockEntity tileentity = (SpawnerBlockEntity)world.getBlockEntity(pos);
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
    	
    	Level level = (Level)event.getWorld();
    	BlockPos pos = event.getPos();
    	
    	SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(pos);
		BaseSpawner logic = spawner.getSpawner();
		CompoundTag nbt = new CompoundTag();
		
		// Get current spawner config values
		nbt = logic.save(level, pos, nbt);
		
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
		CompoundTag data = nbt.getCompound("SpawnData");
		Optional<EntityType<?>> optional = EntityType.by(data);
		if(optional.isPresent()) { 
			if(optional.get().equals(EntityType.AREA_EFFECT_CLOUD)) {
				event.setCanceled(true);
				return;
			}
		}
		
    	// Check redstone power
    	if(level.hasNeighborSignal(pos)) {
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
    		
    	else {
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
    	logic.load(level, pos, nbt);
    	spawner.setChanged();
    	BlockState blockstate = level.getBlockState(pos);
    	level.sendBlockUpdated(pos, blockstate, blockstate, 3);
    }
    
    /**
     * 	Enables mobs to have a small chance to drop an egg
     */
    @SubscribeEvent
    public void onMobDrop(LivingDropsEvent event) {	
    	if(random.nextFloat() > ConfigValues.get("monster_egg_drop_chance") / 100f)
    		return;
    	
    	Entity entity = event.getEntity();
    	EntityType<?> entityType = entity.getType();
    	
    	if(ConfigValues.isEggDisabled(entityType.getRegistryName().toString()))
			return;
    	
    	ItemStack itemStack;
    	
    	// Leave if it was a player, ender dragon or a wither
    	if(entityType.equals(EntityType.PLAYER) || entityType.equals(EntityType.ENDER_DRAGON) || entityType.equals(EntityType.WITHER))
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
    			event.getHand() == InteractionHand.OFF_HAND)
    		return;
    	
    	Level level = event.getWorld();
    	BlockPos blockpos = event.getPos();
    	
    	// Leave if we didn't right click a spawner block
		if(level.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return;
		
		// Leave if server
		if(!level.isClientSide || event.getPlayer().isSpectator())
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
    private void dropMonsterEgg(BlockPos pos, Level level) {
    	BlockState blockstate = level.getBlockState(pos);
    	SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(pos);
    	BaseSpawner logic = spawner.getSpawner();
    	
    	// Get entity ResourceLocation string from spawner by creating a empty compound which we make our 
    	// spawner logic write to. We can then access what type of entity id the spawner has inside
    	CompoundTag nbt = new CompoundTag();
    	nbt = logic.save(level, pos, nbt);
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
		double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
        double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
        double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
        
        // Create entity item
        ItemEntity entityItem = new ItemEntity(
        		level, 
        		(double)pos.getX() + d0, 
        		(double)pos.getY() + d1, 
        		(double)pos.getZ() + d2, 
        		itemStack);
		entityItem.setDefaultPickUpDelay();
		
		// Spawn entity item (egg)
		level.addFreshEntity(entityItem);
		
		// Replace the entity inside the spawner with default entity
		logic.setEntityId(EntityType.AREA_EFFECT_CLOUD);
		spawner.setChanged();
		level.sendBlockUpdated(pos, blockstate, blockstate, 3);
    }
    
    /**
     * 	Check a tools item enchantment list contains Silk Touch enchant
     * 	I don't know if there's a better way to do this	
     * 
     * 	@param NBTTagList of enchantment
     * 	@return true/false
     */
    private boolean checkSilkTouch(ListTag list) {
    	// Check list string contains silk touch
		if(list.getAsString().indexOf("minecraft:silk_touch") != -1)
			return true;
		else
			return false;
    }
}
