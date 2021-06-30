package com.branders.spawnermod.networking.packet;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * 	When connecting to a server we need to sync the server config with the client.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncConfigMessage extends NetworkPacket {
	
	public static final Identifier ID = new Identifier(SpawnerMod.MOD_ID, "packet.sync_config_message");
	
	public SyncConfigMessage(short config, short count, short range, short speed, short limitedSpawns, short limitedSpawnsAmount) {
		this.writeShort(config);
		this.writeShort(count);
		this.writeShort(range);
		this.writeShort(speed);
		this.writeShort(limitedSpawns);
		this.writeShort(limitedSpawnsAmount);
	}
	
	public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		
		buf.retain();
		short disable_spawner_config = buf.readShort();
		short disable_count = buf.readShort();
		short disable_range = buf.readShort();
		short disable_speed = buf.readShort();
		short limited_spawns = buf.readShort();
		short limitedSpawnsAmount = buf.readShort();
		
		client.execute(() -> {
			ConfigValues.put("disable_spawner_config", (int) disable_spawner_config);
			ConfigValues.put("disable_count", (int) disable_count);
			ConfigValues.put("disable_range", (int) disable_range);
			ConfigValues.put("disable_speed", (int) disable_speed);
			ConfigValues.put("limited_spawns_enabled", (int)limited_spawns);
			ConfigValues.put("limited_spawns_amount", (int)limitedSpawnsAmount);
		});
		buf.release();
	}

	@Override
	public Identifier getId() {
		return ID;
	}
}
