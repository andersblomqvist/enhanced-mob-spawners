package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * 	Config values needs to be synced from server to client
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerConfig {
	private int disable_spawner_config;
	private int disable_count;
	private int disable_speed;
	private int disable_range;
	
	public SyncSpawnerConfig(int spawner_config, int count, int speed, int range) {
		this.disable_spawner_config = spawner_config;
		this.disable_count = count;
		this.disable_speed = speed;
		this.disable_range = range;
	}
	
	public static void encode(SyncSpawnerConfig msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.disable_spawner_config);
		buf.writeInt(msg.disable_count);
		buf.writeInt(msg.disable_speed);
		buf.writeInt(msg.disable_range);
	}
	
	public static SyncSpawnerConfig decode(FriendlyByteBuf buf) {
		int spawner_config = buf.readInt();
		int count = buf.readInt();
		int speed = buf.readInt();
		int range = buf.readInt();
		
		return new SyncSpawnerConfig(spawner_config, count, speed, range);
	}
	
	public static void handle(SyncSpawnerConfig msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ConfigValues.sync(
					msg.disable_spawner_config, 
					msg.disable_count, 
					msg.disable_speed, 
					msg.disable_range);
		});
	}
}
