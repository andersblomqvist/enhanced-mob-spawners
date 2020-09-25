package com.branders.spawnermod;

import com.branders.spawnermod.config.SpawnerModConfig;
import com.branders.spawnermod.event.SpawnerEventHandler;
import com.branders.spawnermod.item.SpawnerKeyItem;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerConfig;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 	Small mod adding more functionality to Mob Spawners (Minecraft Forge 1.16)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(SpawnerMod.MODID)
public class SpawnerMod {
	
	public static final String MODID = "spawnermod";
	
	public static Item iron_golem_spawn_egg = new SpawnEggItem(
			EntityType.IRON_GOLEM, 15198183, 9794134, (new Item.Properties()).group(ItemGroup.MISC));
	
	/**
	 * 	Register events and config
	 */
    public SpawnerMod() {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpawnerModConfig.SPEC);
    	
    	// Register new network packet handler used to manage data from client GUI to server
    	SpawnerModPacketHandler.register();
    	
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    	// MinecraftForge.EVENT_BUS.register(new WorldEvents());
    	MinecraftForge.EVENT_BUS.register(this);
	}
	
    /**
     * 	Sync client config with server config
     * 
     * 	@param event when player connects to the server
     */
    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    	ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
    	
		SpawnerModPacketHandler.INSTANCE.sendTo(
				new SyncSpawnerConfig(
						SpawnerModConfig.GENERAL.disable_spawner_config.get(),
						SpawnerModConfig.GENERAL.disable_count.get(),
						SpawnerModConfig.GENERAL.disable_speed.get(),
						SpawnerModConfig.GENERAL.disable_range.get()),
				player.connection.getNetworkManager(), 
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
		registry.register(new SpawnerKeyItem(new Item.Properties().group(ItemGroup.TOOLS).rarity(Rarity.RARE)).setRegistryName(MODID, "spawner_key"));
    	
    	registry.register(iron_golem_spawn_egg.setRegistryName(MODID, "iron_golem_spawn_egg"));
    	registry.register(new BlockItem(Blocks.SPAWNER, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(Blocks.SPAWNER.getRegistryName()));
    }
}