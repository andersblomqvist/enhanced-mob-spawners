package com.branders.spawnermod;

import com.branders.spawnermod.networking.SpawnerModNetworking;

import net.fabricmc.api.ClientModInitializer;

/**
 * Initialize client specifics
 * 
 * @author Anders <Branders> Blomqvist
 */
public class SpawnerModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SpawnerModNetworking.registerClientMessages();
    }
}
