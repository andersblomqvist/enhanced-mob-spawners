package com.branders.spawnermod.networking.packet;

import com.branders.spawnermod.SpawnerMod;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncConfigPacket(int config, int count, int range, int speed, int limitedSpawns, int limitedSpawnsAmount,
        int isCustomRange, int customRange) implements CustomPayload {

    public static final CustomPayload.Id<SyncConfigPacket> PACKET_ID = new CustomPayload.Id<>(
            Identifier.of(SpawnerMod.MOD_ID, "packet.sync_config_message"));

    public static final PacketCodec<RegistryByteBuf, SyncConfigPacket> PACKET_CODEC = PacketCodec
            .of(SyncConfigPacket::write, SyncConfigPacket::new);

    public SyncConfigPacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt());
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(config);
        buf.writeVarInt(count);
        buf.writeVarInt(range);
        buf.writeVarInt(speed);
        buf.writeVarInt(limitedSpawns);
        buf.writeVarInt(limitedSpawnsAmount);
        buf.writeVarInt(isCustomRange);
        buf.writeVarInt(customRange);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
