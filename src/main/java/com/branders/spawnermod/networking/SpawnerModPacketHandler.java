package com.branders.spawnermod.networking;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.networking.packet.SyncSpawnerConfig;
import com.branders.spawnermod.networking.packet.SyncSpawnerEggDrop;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 	Register network messages
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SpawnerModPacketHandler {
	
	private static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(SpawnerMod.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	public static void register() {
        int messageId = 0;
        
        INSTANCE.registerMessage(messageId++, 
        		SyncSpawnerMessage.class, 
        		SyncSpawnerMessage::encode, 
        		SyncSpawnerMessage::decode, 
        		SyncSpawnerMessage::handle);
        
        INSTANCE.registerMessage(messageId++, 
        		SyncSpawnerEggDrop.class, 
        		SyncSpawnerEggDrop::encode, 
        		SyncSpawnerEggDrop::decode, 
        		SyncSpawnerEggDrop::handle);
        
        INSTANCE.registerMessage(messageId++, 
        		SyncSpawnerConfig.class, 
        		SyncSpawnerConfig::encode, 
        		SyncSpawnerConfig::decode, 
        		SyncSpawnerConfig::handle);
    }
}
