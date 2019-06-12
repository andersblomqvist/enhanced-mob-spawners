package com.branders.spawnermod;

import com.branders.spawnermod.config.Config;
import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 	Small mod adding more functionality to Mob Spawners
 * 
 * 	@author Anders <Branders> Blomqvist
 *
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(SpawnerMod.modid)
public class SpawnerMod
{		
	public static final String modid = "spawnermod";
	
	/**
	 * 	Register event handler
	 */
    public SpawnerMod() 
    {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.config);
    	Config.loadConfig(Config.config, FMLPaths.CONFIGDIR.get().resolve("spawnermod-config.toml").toString());
    	

    	MinecraftForge.EVENT_BUS.register(this);
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    }
    
    /**
     * 	Event for register spawner item block
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) 
    {
        registerItems(event.getRegistry());
    }
    
    public static void registerItems(IForgeRegistry<Item> registry)
    {
    	// Register Spawner ItemBlock as a "new" item block
    	// It only makes the spawner show up in decoration tab
    	registry.register(new ItemBlock(Blocks.SPAWNER, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(modid, "spawner"));
    }
}
