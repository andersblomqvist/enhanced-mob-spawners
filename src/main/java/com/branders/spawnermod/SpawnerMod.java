package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.SpawnerEventHandler;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.registry.RegistryHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * 	Small mod adding more functionality to Mob Spawners (Minecraft Forge 1.19)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mod(SpawnerMod.MOD_ID)
public class SpawnerMod {
	
	public static final String MOD_ID = "spawnermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	/**
	 * 	Start of the mod.
	 * 
	 * 	- Initialize config
	 *  - Register network packets
	 *  - Register items and blocks
	 *  - Register event handler
	 */
    public SpawnerMod() {
    	
    	ModConfigManager.initConfig(MOD_ID, FMLPaths.CONFIGDIR.get());
    	SpawnerModPacketHandler.register();
    	RegistryHandler.init();
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
	}
}