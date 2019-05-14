package com.branders.spawnermod;

import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 	Small mod adding more functionality to Mob Spawners
 * 
 * 	@author Branders
 *
 */
@Mod("spawnermod")
public class SpawnerMod
{		
	/**
	 * 	Register event handler
	 */
    public SpawnerMod() 
    {	
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    }
    
    /**
     * 	Add Spawner block into creative tabs
     */
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents 
    {
    	@SubscribeEvent
	    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) 
	    {
			Item spawner_block;
    		
	 		itemRegistryEvent.getRegistry().registerAll
	 		(
	 				spawner_block = new ItemBlock(Blocks.SPAWNER, new Item.Properties().
		     				group(ItemGroup.DECORATIONS)).
		     				setRegistryName(Blocks.SPAWNER.getRegistryName())
	 		);
	    }
    }
}