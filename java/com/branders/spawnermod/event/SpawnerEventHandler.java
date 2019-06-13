package com.branders.spawnermod.event;

import java.util.Random;

import com.branders.spawnermod.config.SpawnConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSpawnEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
     * 	When we harvest a block
     * 	Return spawner block when harvested with silk touch
     */
    @SubscribeEvent
    public void onBlockHarvestDrops(BlockEvent.HarvestDropsEvent event)
    {
    	if(event.getState().getBlock() == Blocks.SPAWNER)
    	{   
    		NBTTagList list = event.getHarvester().getHeldItemMainhand().getEnchantmentTagList();
    		
    		// Check if silk touch enchant is on the tool
    		if(CheckSilkTouch(list))
    			event.getDrops().add(new ItemStack(Blocks.SPAWNER, 1));
    	}
    }
    
    /**
     * 	When a block is destroyed
     * 	Prevent XP drop when spawner is destroyed with silk touch
     */
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) 
    {	
    	// Check if a spawner broke
    	if(event.getState().getBlock() == Blocks.SPAWNER)
    	{
    		NBTTagList list = event.getPlayer().getHeldItemMainhand().getEnchantmentTagList();
    		
    		// Return 0 EXP when harvested with silk touch
    		if(CheckSilkTouch(list))
    			event.setExpToDrop(0);
    	}
    }
    
    
    /**
     * 	Called when a block gets an update
     * 	Used to replace entity in spawner when block placed
     */
    @SubscribeEvent
    public void onNotifyEvent(BlockEvent.NeighborNotifyEvent event)
    {
    	if(event.getState().getBlock() != Blocks.SPAWNER)
    		return;
    	
    	World world = (World)event.getWorld();
    	
    	BlockPos blockpos = event.getPos();
    	IBlockState iblockstate = world.getBlockState(blockpos);

    	TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getTileEntity(blockpos);
    	MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();

    	// Replace the entity inside the spawner with default entity
    	logic.setEntityType(defaultEntityType);
    	spawner.markDirty();
    	world.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
    }
    
    
    /**
     * 	Called when a mob drops items
     * 	Enables mobs to have a small chance to drop an egg
     */
    @SubscribeEvent
    public void onMobDrop(LivingDropsEvent event)
    {	
    	if(random.nextFloat() > SPAWN_RATE)
    		return;
    	
    	Entity entity = event.getEntity();
    	EntityType<?> entityType = entity.getType();
    	
		// Get the entity mob egg and put in an ItemStack
		ItemSpawnEgg egg = ItemSpawnEgg.getEgg(entityType);
		ItemStack itemStack = new ItemStack(egg);
		
		// Add egg in drops
		event.getDrops().add(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, itemStack));
    }
    
    
    /**
     * 	Event when player interacts with block
     * 	Enables so that the player can right click a spawner to get its egg
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
    {
    	World world = event.getWorld();
    	
    	// Leave if we are client and if the block isn't a spawner
    	if(world.isRemote || event.getItemStack().getItem() instanceof ItemBlock)		
    		return;
    	
    	BlockPos blockpos = event.getPos();
		IBlockState iblockstate = world.getBlockState(blockpos);	
		
    	// Check if we right-clicked and return mob egg from spawner
		if(world.getBlockState(blockpos).getBlock() == Blocks.SPAWNER && event.getHand() == EnumHand.MAIN_HAND)
			DropMonsterEgg(world, blockpos, iblockstate);
			
    }
    
    /**
     * 	Spawns a mob egg depending on what type of entity inside mob spawner.
     * 	When successfully retrieved monster egg we set spawner entity to default.
     */
    private void DropMonsterEgg(World world, BlockPos blockpos, IBlockState iblockstate)
    {
    	TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getTileEntity(blockpos);
    	MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
    	
    	// Get entity ResourceLocation string from spawner by creating a empty NBTTagComp which we make our 
    	// spawner logic write to. We can then access what type of entity id the spawner has inside
    	NBTTagCompound nbtIn = new NBTTagCompound();
    	nbtIn = logic.writeToNBT(nbtIn);
    	String entity_string = nbtIn.getTag("SpawnData").getString();
    	
    	// Strips the string
    	// Example: {id: "minecraft:xxx_xx"} --> minecraft:xxx_xx
    	entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
    	entity_string = entity_string.substring(0, entity_string.indexOf("\""));
    	
		// Get entity type
		EntityType<?> entityType = EntityType.getById(entity_string);
		
		// Leave if the spawner does not contain an egg
		if(entityType.equals(defaultEntityType))
			return;
		
    	// Get the entity mob egg and put in an ItemStack
		ItemSpawnEgg egg = ItemSpawnEgg.getEgg(entityType);
		ItemStack itemStack = new ItemStack(egg);
		
		// Get random fly-out position offsets
		double d0 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
        double d1 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
        double d2 = (double)(world.rand.nextFloat() * 0.7F) + (double)0.15F;
        
        // Create entity item
		EntityItem entityItem = new EntityItem(world, (double)blockpos.getX() + d0, (double)blockpos.getY() + d1, (double)blockpos.getZ() + d2, itemStack);
		entityItem.setDefaultPickupDelay();
		
		// Spawn entity item (egg)
		world.spawnEntity(entityItem);
		
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
    private boolean CheckSilkTouch(NBTTagList list)
    {
    	// Check list string contains silk touch
		if(list.getString().indexOf("minecraft:silk_touch") != -1)
			return true;
		else
			return false;
    }
}
