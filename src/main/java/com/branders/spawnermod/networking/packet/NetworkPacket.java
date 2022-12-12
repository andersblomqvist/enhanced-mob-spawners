package com.branders.spawnermod.networking.packet;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class NetworkPacket extends PacketByteBuf {

    public NetworkPacket() {
        super(Unpooled.buffer());
    }

    public void send() {
        ClientPlayNetworking.send(this.getId(), this);
    }

    public abstract Identifier getId();
}
