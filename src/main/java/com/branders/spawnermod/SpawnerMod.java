package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.SpawnerEventHandler;
import com.branders.spawnermod.item.SpawnerKeyItem;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 	Small mod adding more functionality to Mob Spawners (Minecraft Forge 1.17)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(SpawnerMod.MODID)
public class SpawnerMod {
	
	public static final String MODID = "spawnermod";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static Item iron_golem_spawn_egg = new SpawnEggItem(
			EntityType.IRON_GOLEM, 15198183, 9794134, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC));
	
	/**
	 * 	Register events and config
	 */
    public SpawnerMod() {
    	// ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpawnerModConfig.SPEC);
    	ModConfigManager.initConfig(MODID);
    	
    	// Register new network packet handler used to manage data from client GUI to server
    	SpawnerModPacketHandler.register();
    	
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    	MinecraftForge.EVENT_BUS.register(this);
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
     * 	Event for register spawner wrench and spawner item block
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        registerItems(event.getRegistry());
    }
    
    public static void registerItems(IForgeRegistry<Item> registry) {
    	// Only register Spawner Key if enabled in config
		registry.register(new SpawnerKeyItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).durability(10).rarity(Rarity.RARE)).setRegistryName(MODID, "spawner_key"));
    	
    	registry.register(iron_golem_spawn_egg.setRegistryName(MODID, "iron_golem_spawn_egg"));
    	registry.register(new BlockItem(Blocks.SPAWNER, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.EPIC)).setRegistryName(Blocks.SPAWNER.getRegistryName()));
    }
}