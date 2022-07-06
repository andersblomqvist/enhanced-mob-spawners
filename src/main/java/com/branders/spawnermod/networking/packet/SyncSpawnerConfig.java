package com.branders.spawnermod.networking.packet;

import java.util.function.Supplier;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
	private int limitedSpawns;
	private int limitedSpawnsAmount;
	private int isCustomRange;
	private int customRange;
	
	public SyncSpawnerConfig(int spawner_config, int count, int speed, int range, int limitedSpawns, int limitedSpawnsAmount, int isCustomRange, int customRange) {
		this.disable_spawner_config = spawner_config;
		this.disable_count = count;
		this.disable_speed = speed;
		this.disable_range = range;
		this.limitedSpawns = limitedSpawns;
		this.limitedSpawnsAmount = limitedSpawnsAmount;
		this.isCustomRange = isCustomRange;
		this.customRange = customRange;
	}
	
	public static void encode(SyncSpawnerConfig msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.disable_spawner_config);
		buf.writeInt(msg.disable_count);
		buf.writeInt(msg.disable_speed);
		buf.writeInt(msg.disable_range);
		buf.writeInt(msg.limitedSpawns);
		buf.writeInt(msg.limitedSpawnsAmount);
		buf.writeInt(msg.isCustomRange);
		buf.writeInt(msg.customRange);
	}
	
	public static SyncSpawnerConfig decode(FriendlyByteBuf buf) {
		int spawner_config = buf.readInt();
		int count = buf.readInt();
		int speed = buf.readInt();
		int range = buf.readInt();
		int limitedSpawns = buf.readInt();
		int limitedSpawnsAmount = buf.readInt();
		int isCustomRange = buf.readInt();
		int customRange = buf.readInt();
		
		return new SyncSpawnerConfig(spawner_config, count, speed, range, limitedSpawns, limitedSpawnsAmount, isCustomRange, customRange);
	}
	
	public static void handle(SyncSpawnerConfig msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ConfigValues.sync(
					msg.disable_spawner_config, 
					msg.disable_count, 
					msg.disable_speed, 
					msg.disable_range,
					msg.limitedSpawns,
					msg.limitedSpawnsAmount,
					msg.isCustomRange,
					msg.customRange);
		});
	}
}
