package com.branders.spawnermod.networking;

import com.branders.spawnermod.networking.packet.SyncConfigMessage;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;

public class SpawnerModNetworking {

    public static void registerServerMessages() {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(SyncSpawnerMessage.ID, SyncSpawnerMessage::apply);
    }

    public static void registerClientMessages() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(SyncConfigMessage.ID, SyncConfigMessage::apply);
    }
}
