package com.branders.spawnermod.networking.packet;

import com.branders.spawnermod.SpawnerMod;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SyncSpawnerPacket(BlockPos pos, int delay, int spawnCount, int requiredPlayerRange, int maxNearbyEntities,
        int minSpawnDelay, int maxSpawnDelay) implements CustomPayload {

    public static final CustomPayload.Id<SyncSpawnerPacket> PACKET_ID = new CustomPayload.Id<>(
            Identifier.of(SpawnerMod.MOD_ID, "packet.sync_spawner_message"));

    public static final PacketCodec<RegistryByteBuf, SyncSpawnerPacket> PACKET_CODEC = PacketCodec
            .of(SyncSpawnerPacket::write, SyncSpawnerPacket::new);

    public SyncSpawnerPacket(RegistryByteBuf buf) {
        this(RegistryByteBuf.readBlockPos(buf), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt());
    }

    public void write(RegistryByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(delay);
        buf.writeVarInt(spawnCount);
        buf.writeVarInt(requiredPlayerRange);
        buf.writeVarInt(maxNearbyEntities);
        buf.writeVarInt(minSpawnDelay);
        buf.writeVarInt(maxSpawnDelay);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
