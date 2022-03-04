package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.SpawnerEventHandler;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerConfig;
import com.branders.spawnermod.registry.RegistryHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 	Small mod adding more functionality to Mob Spawners (Minecraft Forge 1.18)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mod(SpawnerMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpawnerMod {
	
	public static final String MOD_ID = "spawnermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	/**
	 * 	Register events and config
	 */
	@SuppressWarnings("resource")
    public SpawnerMod() {
    	
    	ModConfigManager.initConfig(MOD_ID, Minecraft.getInstance().gameDirectory.getAbsoluteFile());
    	
    	// Register new network packet handler used to manage data from client GUI to server
    	SpawnerModPacketHandler.register();
    	RegistryHandler.init();
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    	MinecraftForge.EVENT_BUS.register(this);
	}
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    	ModConfigManager.initConfig(MOD_ID, event.getServer().getServerDirectory().getAbsoluteFile());
    }
    
    /**
     * 	Sync client config with server config
     * 
     * 	@param event when player connects to the server
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
    
    /**
     * 	Event for register the spawner block
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        registerItems(event.getRegistry());
    }
    
    public static void registerItems(IForgeRegistry<Item> registry) {
    	registry.register(new BlockItem(Blocks.SPAWNER, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.EPIC)).setRegistryName(Blocks.SPAWNER.getRegistryName()));
    }
}