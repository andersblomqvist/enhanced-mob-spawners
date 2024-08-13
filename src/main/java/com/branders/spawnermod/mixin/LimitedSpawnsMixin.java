package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

/**
 * Implements a limit to how many spawns a spawner can do. Only if enabled in
 * config!
 * 
 * @author Anders <Branders> Blomqvist
 */
@Mixin(MobSpawnerLogic.class)
public class LimitedSpawnsMixin {

    private short spawns = 0;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"), method = "serverTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V", cancellable = true)
    private void entitySpawn(ServerWorld world, BlockPos pos, CallbackInfo ci) {

        if (ConfigValues.get("limited_spawns_enabled") == 0)
            return;

        // Don't count "empty" entities.
        NbtCompound nbt = new NbtCompound();
        nbt = ((MobSpawnerLogic) (Object) this).writeNbt(nbt);
        String entity_string = nbt.get("SpawnData").toString();
        entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
        entity_string = entity_string.substring(0, entity_string.indexOf("\""));
        if (entity_string.contains("area_effect_cloud"))
            return;

        spawns++;
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/EntityType;fromNbt(Lnet/minecraft/nbt/NbtCompound;)Ljava/util/Optional;"), method = "serverTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V", cancellable = true)
    public void cancel(ServerWorld world, BlockPos pos, CallbackInfo ci) {

        if (ConfigValues.get("limited_spawns_enabled") == 0)
            return;

        world.getBlockEntity(pos).markDirty();
        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);

        if (spawns >= ConfigValues.get("limited_spawns_amount")) {
            // Disable the spawner AND remove egg.
            NbtCompound nbt = new NbtCompound();
            nbt = ((MobSpawnerLogic) (Object) this).writeNbt(nbt);
            nbt.putShort("RequiredPlayerRange", (short) 0);
            ((MobSpawnerLogic) (Object) this).readNbt(world, pos, nbt);
            // ((MobSpawnerLogic)(Object)this).setEntityId(EntityType.AREA_EFFECT_CLOUD,
            // world, world.random, pos);
            world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getShort(Ljava/lang/String;)S"), method = "readNbt")
    private void readNbt(World world, BlockPos pos, NbtCompound nbt, CallbackInfo info) {

        if (ConfigValues.get("limited_spawns_enabled") == 0)
            return;

        spawns = nbt.getShort("spawns");
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putShort(Ljava/lang/String;S)V"), method = "writeNbt")
    private void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {

        if (ConfigValues.get("limited_spawns_enabled") == 0)
            return;

        nbt.putShort("spawns", spawns);
    }
}
