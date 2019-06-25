package com.branders.spawnermod.event;

import java.util.Random;

import com.branders.spawnermod.config.SpawnerModConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 	Handles all the events regarding the mob spawner and entities.
 * 
 * 	@author Anders <Branders> Blomqvist
 *
 */
@EventBusSubscriber
public class SpawnerEventHandler 
{	
	private ResourceLocation defaultEntityType = new ResourceLocation("area_effect_cloud");
	
	// Get Spawn Rate chance for monster egg from config spec
	private float SPAWN_RATE = SpawnerModConfig.drop_rate_in_percentage / 100F;
	
	private Random random = new Random();
	
	
	/**
     * 	When we harvest a block
     * 	Return spawner block when harvested with silk touch
     */
	@SubscribeEvent
	public void onBlockHarvestEvent(BlockEvent.HarvestDropsEvent event)
	{
		if(event.getState().getBlock() == Blocks.MOB_SPAWNER)
    	{   
    		NBTTagList list = event.getHarvester().getHeldItemMainhand().getEnchantmentTagList();
    		
    		// Check if silk touch enchant is on the tool and drop spawner if true
    		if(CheckSilkTouch(list))
    			event.getDrops().add(new ItemStack(Blocks.MOB_SPAWNER, 1));
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
    	if(event.getState().getBlock() == Blocks.MOB_SPAWNER)
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
    	if(event.getState().getBlock() != Blocks.MOB_SPAWNER)
    		return;
    	
    	World world = (World)event.getWorld();
    	
    	BlockPos blockpos = event.getPos();
    	IBlockState iblockstate = world.getBlockState(blockpos);

    	TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getTileEntity(blockpos);
    	MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();

    	// Replace the entity inside the spawner with default entity
    	System.out.println();
    	logic.setEntityId(defaultEntityType);
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
    	
    	// Get NBT data from Entity, used to get entity type (resource location)
    	NBTTagCompound compound = entity.serializeNBT();
    	
    	// Get the entity mob egg and put in an ItemStack
		ItemStack itemStack = getMonsterEgg(compound.getString("id"));
    	
		// Add egg in drops
		event.getDrops().add(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, itemStack));
    }
    
    

    /**
     * 	Event when player interacts with block
     * 	Enables so we can right click a spawner to retrieve its egg
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
		if(world.getBlockState(blockpos).getBlock() == Blocks.MOB_SPAWNER && event.getHand() == EnumHand.MAIN_HAND)
			DropMonsterEgg(world, blockpos, iblockstate);		
    }
	
    
    /**
     *  Spawns a mob egg depending on what type of entity inside mob spawner.
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
    	
    	// String entity_string = nbtIn.getTag("SpawnData").getString();
    	String entity_string = nbtIn.getTag("SpawnData").toString();
    	
    	// Strips the string
    	// Example: {id: "minecraft:xxx_xx"} --> minecraft:xxx_xx
    	entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
    	entity_string = entity_string.substring(0, entity_string.indexOf("\""));
    	
		// Leave if the spawner does not contain a valid mob
		if(entity_string.equalsIgnoreCase(defaultEntityType.toString()))
			return;
		
    	// Get the entity mob egg and put in an ItemStack
		ItemStack itemStack = getMonsterEgg(entity_string);
		
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
		logic.setEntityId(defaultEntityType);
		spawner.markDirty();
		world.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
    }
    
    
    /**
     * 	Create, add and return an ItemStack containing Monster Egg specified by resource location
     * 
     * 	@param String with ResourceLocation name
     * 	@return ItemStack with 1 Monster Egg
     */
    private ItemStack getMonsterEgg(String resourceLocation)
    {
    	// Create ItemStack with unspecified Spawn Egg
    	ItemStack itemStack = new ItemStack(Items.SPAWN_EGG);
    	
    	// Do some NBT work to specify entity type
    	NBTTagCompound nbttagcompound = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setString("id", resourceLocation);
        nbttagcompound.setTag("EntityTag", nbttagcompound1);
        
        // Set new NBT Data to Item Stack
        itemStack.setTagCompound(nbttagcompound);
        
        return itemStack;
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
    	for(int i = 0; i < list.tagCount(); i++)
    	{
    		String id = list.get(i).toString();
    		id = id.substring(11, 13);
    		
    		if(id.equalsIgnoreCase("33"))
    			return true;
    	}
    	
    	return false;
    }
}
