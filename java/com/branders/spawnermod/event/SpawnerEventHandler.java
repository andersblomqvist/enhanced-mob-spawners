package com.branders.spawnermod.event;

import java.util.Random;

import com.branders.spawnermod.config.SpawnConfig;
import com.branders.spawnermod.item.SpawnerKeyItem;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerEggDrop;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	private float DROP_RATE = (float)(SpawnConfig.monster_egg_drop_chance.get() / 100F);
	
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
    	
    	BlockPos pos = event.getPos();
    	World world = (World)event.getWorld();
    	
    	world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
    	TileEntity tileentity = world.getTileEntity(pos);
    	
    	((MobSpawnerTileEntity)tileentity).getSpawnerBaseLogic().setEntityType(defaultEntityType);
    	tileentity.markDirty();
    	world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    
    /**
     * 	Enables mobs to have a small chance to drop an egg
     */
    @SubscribeEvent
    public void onMobDrop(LivingDropsEvent event)
    {	
    	if(random.nextFloat() < DROP_RATE)
    		return;
    	
    	Entity entity = event.getEntity();
    	EntityType<?> entityType = entity.getType();
    	
    	// Leave if a player died.
    	if(entityType.equals(EntityType.PLAYER))
    		return;
    	
		ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entityType.getRegistryName() + "_spawn_egg")));
		
		// Add monster egg to drops
		event.getDrops().add(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, itemStack));
    }
    
    
    /**
     * 	Event when player interacts with block.
     * 	Enables so that the player can right click a spawner to get its egg.
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
    {
    	Item item = event.getItemStack().getItem();
    	
    	// Leave if we are client and if we are holding a block. Also prevent off hand action
    	if(item instanceof BlockItem || item instanceof SpawnEggItem || item instanceof SpawnerKeyItem || event.getHand() == Hand.OFF_HAND)		
    		return;
    	
    	World world = event.getWorld();
    	BlockPos blockpos = event.getPos();
    	
    	// Leave if we didn't right click a spawner block
		if(world.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return;
		
		// Send Network message
		SpawnerModPacketHandler.INSTANCE.sendToServer(new SyncSpawnerEggDrop(blockpos));
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
