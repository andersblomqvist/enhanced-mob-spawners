package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import com.branders.spawnermod.config.SpawnerModConfig;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * 	Config values needs to be synced from server to client
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerConfig {
	private boolean disable_spawner_config;
	private boolean disable_count;
	private boolean disable_speed;
	private boolean disable_range;
	
	public SyncSpawnerConfig(boolean spawner_config, boolean count, boolean speed, boolean range) {
		this.disable_spawner_config = spawner_config;
		this.disable_count = count;
		this.disable_speed = speed;
		this.disable_range = range;
	}
	
	public static void encode(SyncSpawnerConfig msg, PacketBuffer buf) {
		buf.writeBoolean(msg.disable_spawner_config);
		buf.writeBoolean(msg.disable_count);
		buf.writeBoolean(msg.disable_speed);
		buf.writeBoolean(msg.disable_range);
	}
	
	public static SyncSpawnerConfig decode(PacketBuffer buf) {
		boolean spawner_config = buf.readBoolean();
		boolean count = buf.readBoolean();
		boolean speed = buf.readBoolean();
		boolean range = buf.readBoolean();
		
		return new SyncSpawnerConfig(spawner_config, count, speed, range);
	}
	
	public static void handle(SyncSpawnerConfig msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			// Sync config values
			SpawnerModConfig.sync(msg.disable_spawner_config,
					msg.disable_count,
					msg.disable_speed,
					msg.disable_range);
		});
	}
}
