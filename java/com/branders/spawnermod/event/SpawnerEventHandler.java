package com.branders.spawnermod.event;

import java.util.Random;

import com.branders.spawnermod.config.SpawnConfig;
import com.branders.spawnermod.item.SpawnerKeyItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
 *
 */
@EventBusSubscriber
public class SpawnerEventHandler
{
	// Get Spawn Rate chance for monster egg from config spec
	private float SPAWN_RATE = SpawnConfig.monster_egg_drop_chance.get() / 100F;
	
	private Random random = new Random();
	private EntityType<?> defaultEntityType = EntityType.AREA_EFFECT_CLOUD;
	
	/**
     * 	Return spawner block when spawner harvested with silk touch
     * 	
     * 	Dont work in 1.14.
     
    @SubscribeEvent
    public void onBlockHarvestDrops(BlockEvent.HarvestDropsEvent event)
    {	
    	if(event.getState().getBlock() == Blocks.SPAWNER)
    	{   
    		ListNBT list = event.getHarvester().getHeldItemMainhand().getEnchantmentTagList();
    		
    		// Return Spawner Block when harvested with Silk Touch
    		if(checkSilkTouch(list))
    			event.getDrops().add(new ItemStack(Blocks.SPAWNER, 1));
    	}
    }
    */
    
    /**
     * 	Prevent XP drop when spawner is destroyed with silk touch
     */
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) 
    {	
    	// Check if a spawner broke
    	if(event.getState().getBlock() == Blocks.SPAWNER)
    	{
    		ListNBT list = event.getPlayer().getHeldItemMainhand().getEnchantmentTagList();
    		
    		// Return 0 EXP when harvested with silk touch
    		if(checkSilkTouch(list))
    		{
    			event.setExpToDrop(0);
    			
    			// Return Spawner Block
    			ItemStack itemStack = new ItemStack(Blocks.SPAWNER.asItem());
    			ItemEntity entityItem = new ItemEntity((World)event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), itemStack);
    			event.getWorld().addEntity(entityItem);
    		}
    				
    	}
    }
    
    
    /**
     * 	Used to replace entity in spawner when block placed down by player
     */
    @SubscribeEvent
    public void onNotifyEvent(BlockEvent.NeighborNotifyEvent event)
    {
    	if(event.getState().getBlock() != Blocks.SPAWNER)
    		return;
    	
    	World world = (World)event.getWorld();
    	
    	BlockPos blockpos = event.getPos();
    	BlockState iblockstate = world.getBlockState(blockpos);

    	MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(blockpos);
    	AbstractSpawner logic = spawner.getSpawnerBaseLogic();

    	// Replace the entity inside the spawner with default entity
    	logic.setEntityType(defaultEntityType);
    	spawner.markDirty();
    	world.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
    }
    
    
    /**
     * 	Enables mobs to have a small chance to drop an egg
     */
    @SubscribeEvent
    public void onMobDrop(LivingDropsEvent event)
    {	
    	if(random.nextFloat() > SPAWN_RATE)
    		return;
    	
    	Entity entity = event.getEntity();
    	EntityType<?> entityType = entity.getType();
    	
		ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entityType.getRegistryName() + "_spawn_egg")));
		
		// Add monster egg to drops
		event.getDrops().add(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, itemStack));
    }
    
    
    /**
     * 	Event when player interacts with block.
     * 	Enables so that the player can right click a spawner to get its egg.
     * 
     * 	Bug: Event is only fired on server.
     * 	{@link https://github.com/MinecraftForge/MinecraftForge/issues/5802}
     * 
     * 	The bug "fixed" via SpawnerKeyItem so when that item is used on a block we open the GUI from there.
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
    {
    	World world = event.getWorld();
    	
    	// Leave if we are client and if we are holding a block. Also prevent off hand action
    	if(event.getItemStack().getItem() instanceof BlockItem || event.getHand() == Hand.OFF_HAND)		
    		return;
    	
    	// Leave if we right-clicked with the SpawnerKey. That stuff is moved as said above
    	Item item = event.getItemStack().getItem();
    	if(item instanceof SpawnerKeyItem)
    		return;
    	
    	BlockPos blockpos = event.getPos();
		BlockState iblockstate = world.getBlockState(blockpos);	
		
		// Leave if we didn't right click a spawner block
		if(world.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return;
		
		// Leave if we client just to make sure.
		if(world.isRemote)
			return;
		
		dropMonsterEgg(world, blockpos, iblockstate);	
    }

    
    /**
     * 	Spawns a mob egg depending on what type of entity inside mob spawner.
     * 	When successfully retrieved monster egg we set spawner entity to default.
     */	
    private void dropMonsterEgg(World world, BlockPos blockpos, BlockState iblockstate)
    {
    	MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(blockpos);
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
		if(entity_string.equalsIgnoreCase(defaultEntityType.getRegistryName().toString()))
			return;
		
    	// Get the entity mob egg and put in an ItemStack
		ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")));
		
		// Get random fly-out position offsets
		double d0 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
        double d1 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
        double d2 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
        
        // Create entity item
        ItemEntity entityItem = new ItemEntity(world, (double)blockpos.getX() + d0, (double)blockpos.getY() + d1, (double)blockpos.getZ() + d2, itemStack);
		entityItem.setDefaultPickupDelay();
		
		// Spawn entity item (egg)
		world.addEntity(entityItem);
		
		// Replace the entity inside the spawner with default entity
		logic.setEntityType(defaultEntityType);
		spawner.markDirty();
		world.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
    }
   
    
    /**
     * 	Check a tools item enchantment list contains Silk Touch enchant
     * 	I don't know if there's a better way to do this	
     * 
     * 	@param NBTTagList of enchantment
     * 	@return true/false
     */
    private boolean checkSilkTouch(ListNBT list)
    {
    	// Check list string contains silk touch
		if(list.getString().indexOf("minecraft:silk_touch") != -1)
			return true;
		else
			return false;
    }
}
