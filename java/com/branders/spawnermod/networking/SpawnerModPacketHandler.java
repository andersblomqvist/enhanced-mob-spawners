package com.branders.spawnermod.networking;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * 	Class to handle network message for synchronizing spawner nbt values
 * 
 * 	@author Anders <Branders> Blomqvist
 *
 */
public class SpawnerModPacketHandler 
{
	private static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
	    new ResourceLocation(SpawnerMod.MODID, "main"),
	    () -> PROTOCOL_VERSION,
	    PROTOCOL_VERSION::equals,
	    PROTOCOL_VERSION::equals
	);
	
	public static void register()
    {
        int messageId = 0;

        INSTANCE.registerMessage(messageId++, SyncSpawnerMessage.class, SyncSpawnerMessage::encode, SyncSpawnerMessage::decode, SyncSpawnerMessage::handle);
    }
}
