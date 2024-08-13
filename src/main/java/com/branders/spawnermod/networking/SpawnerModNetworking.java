package com.branders.spawnermod.networking;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.item.SpawnerKey;
import com.branders.spawnermod.networking.packet.SyncConfigPacket;
import com.branders.spawnermod.networking.packet.SyncSpawnerPacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

public class SpawnerModNetworking {

    public static void registerServerMessages() {

        PayloadTypeRegistry.playC2S().register(SyncSpawnerPacket.PACKET_ID, SyncSpawnerPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SyncSpawnerPacket.PACKET_ID, (payload, context) -> {
            var world = context.player().getServerWorld();

            if (world == null) {
                SpawnerMod.LOGGER.warn("Server world is NULL, cannot sync spawner.");
                return;
            }

            BlockPos pos = payload.pos();
            short requiredPlayerRange = (short) payload.requiredPlayerRange();
            short delay = (short) payload.delay();
            short spawnCount = (short) payload.spawnCount();
            short maxNearbyEntities = (short) payload.maxNearbyEntities();
            short minSpawnDelay = (short) payload.minSpawnDelay();
            short maxSpawnDelay = (short) payload.maxSpawnDelay();

            MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(pos);
            MobSpawnerLogic logic = spawner.getLogic();
            BlockState blockstate = world.getBlockState(pos);

            NbtCompound nbt = new NbtCompound();

            nbt = logic.writeNbt(nbt);

            if (requiredPlayerRange == 0)
                nbt.putShort("SpawnRange", nbt.getShort("RequiredPlayerRange"));
            else
                nbt.putShort("SpawnRange", (short) 4);

            // Change NBT values
            nbt.putShort("Delay", delay);
            nbt.putShort("SpawnCount", spawnCount);
            nbt.putShort("RequiredPlayerRange", requiredPlayerRange);
            nbt.putShort("MaxNearbyEntities", maxNearbyEntities);
            nbt.putShort("MinSpawnDelay", minSpawnDelay);
            nbt.putShort("MaxSpawnDelay", maxSpawnDelay);

            // Update block
            logic.readNbt(world, pos, nbt);
            spawner.markDirty();
            world.updateListeners(pos, blockstate, blockstate, Block.NOTIFY_ALL);

            // Damage the Spawner Key item.
            ItemStack stack = context.player().getMainHandStack();
            if (stack.getItem() instanceof SpawnerKey) {
                stack.damage(1, context.player(), EquipmentSlot.MAINHAND);
            }

            world.syncWorldEvent(WorldEvents.WAX_REMOVED, pos, 0);
        });
    }

    public static void registerClientMessages() {

        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.PACKET_ID, SyncConfigPacket.PACKET_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPacket.PACKET_ID, (payload, context) -> {
            ConfigValues.put("disable_spawner_config", payload.config());
            ConfigValues.put("disable_count", payload.count());
            ConfigValues.put("disable_range", payload.range());
            ConfigValues.put("disable_speed", payload.speed());
            ConfigValues.put("limited_spawns_enabled", payload.limitedSpawns());
            ConfigValues.put("limited_spawns_amount", payload.limitedSpawnsAmount());
            ConfigValues.put("default_spawner_range_enabled", payload.isCustomRange());
            ConfigValues.put("default_spawner_range", payload.customRange());
        });
    }
}
