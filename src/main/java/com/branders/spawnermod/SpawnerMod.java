package com.branders.spawnermod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.branders.spawnermod.event.EventHandler;
import com.branders.spawnermod.networking.SpawnerModNetworking;
import com.branders.spawnermod.networking.packet.SyncConfigPacket;
import com.branders.spawnermod.registry.ModRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Small mod adding more functionality to the Mob Spawner for Minecraft Fabric
 * 
 * @author Anders <Branders> Blomqvist
 */
public class SpawnerMod implements ModInitializer {

    public static final String MOD_ID = "spawnermod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final EventHandler eventHandler = new EventHandler();

    @Override
    public void onInitialize() {

        ModConfigManager.initConfig(MOD_ID);

        UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);
        PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
        LootTableEvents.MODIFY.register(eventHandler::onLootTablesLoaded);

        SpawnerModNetworking.registerServerMessages();

        // If we are a server we send server config values to client
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(handler.player,
                    new SyncConfigPacket(ConfigValues.get("disable_spawner_config"), ConfigValues.get("disable_count"),
                            ConfigValues.get("disable_range"), ConfigValues.get("disable_speed"),
                            ConfigValues.get("limited_spawns_enabled"), ConfigValues.get("limited_spawns_amount"),
                            ConfigValues.get("default_spawner_range_enabled"),
                            ConfigValues.get("default_spawner_range")));
        });

        ModRegistry.register();
    }
}
