package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.EventHandler;
import com.branders.spawnermod.item.SpawnerKey;
import com.branders.spawnermod.networking.SpawnerModNetworking;
import com.branders.spawnermod.networking.packet.SyncConfigMessage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

/**
 * 	Small mod adding more functionality to the Mob Spawner for Minecraft Fabric (1.18.2)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SpawnerMod implements ModInitializer {

	public static final String MOD_ID = "spawnermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static final Item SPAWNER_KEY = new SpawnerKey(new FabricItemSettings().maxDamage(10).group(ItemGroup.TOOLS).rarity(Rarity.RARE));
	public static final Item IRON_GOLEM_SPAWN_EGG = new SpawnEggItem(EntityType.IRON_GOLEM, 15198183, 9794134, (new Item.Settings()).group(ItemGroup.MISC));
	
	public static final EventHandler eventHandler = new EventHandler();
	
	@Override
	public void onInitialize() {
		
		ModConfigManager.initConfig(MOD_ID);
		
		UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);
		PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
		
		// If we are a server we send server config values to client
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.player, SyncConfigMessage.ID, new SyncConfigMessage(
					(short)ConfigValues.get("disable_spawner_config"), 
					(short)ConfigValues.get("disable_count"), 
					(short)ConfigValues.get("disable_range"), 
					(short)ConfigValues.get("disable_speed"),
					(short)ConfigValues.get("limited_spawns_enabled"),
					(short)ConfigValues.get("limited_spawns_amount")));
		});
		
		SpawnerModNetworking.registerServerMessages();
		
		// Registry.register(Registry.ITEM, 1101, "spawner", new BlockItem(Blocks.SPAWNER, new Item.Settings().group(ItemGroup.DECORATIONS).rarity(Rarity.EPIC)));
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "spawner_key"), SPAWNER_KEY);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "iron_golem_spawn_egg"), IRON_GOLEM_SPAWN_EGG);
	}
}
