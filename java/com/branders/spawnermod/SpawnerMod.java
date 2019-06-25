package com.branders.spawnermod;

import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 	Small mod adding more functionality to Mob Spawners
 * 
 * 	@author Anders <Branders> Blomqvist
 *
 */
@Mod(modid = SpawnerMod.MODID, name = SpawnerMod.NAME, version = SpawnerMod.VERSION)
public class SpawnerMod
{
	/** Mod info */
    public static final String MODID = "spawnermod";
    public static final String NAME = "Enhanced Spawner Mod";
    public static final String VERSION = "1.0-1.12.2";
    
    /**
     * 	Register events
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	// Register Spawner events
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    	
    	// Set Mob Spawner block to a creative tab
    	Blocks.MOB_SPAWNER.setCreativeTab(CreativeTabs.DECORATIONS);
    }
}
