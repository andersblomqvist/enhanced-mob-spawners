package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.SpawnerEventHandler;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerConfig;
import com.branders.spawnermod.registry.RegistryHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;

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
    
    /**
     * 	Sync client config with server config when a client joins a server.
     * 
     * 	@param event when player connects to the server.
     */
    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    	ServerPlayer player = (ServerPlayer) event.getPlayer();
    	
		SpawnerModPacketHandler.INSTANCE.sendTo(
				new SyncSpawnerConfig(
						ConfigValues.get("disable_spawner_config"),
						ConfigValues.get("disable_count"),
						ConfigValues.get("disable_speed"),
						ConfigValues.get("disable_range")),
				player.connection.getConnection(),
				NetworkDirection.PLAY_TO_CLIENT);
    }
}