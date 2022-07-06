package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

/**
 * 	Sets the required player range one time to config value.	
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(MobSpawnerLogic.class)
public class SpawnerRangeMixin {

	private boolean rangeSet = false;
	
	@Inject(
			at = @At("HEAD"),
			method = "isPlayerInRange("
					+ "Lnet/minecraft/world/World;"
					+ "Lnet/minecraft/util/math/BlockPos;"
					+ ")"
					+ "Z",
			cancellable = true)
	private void isPlayerInRange(World level, BlockPos spawner, CallbackInfoReturnable<Boolean> cir) {
		
		if(ConfigValues.get("default_spawner_range_enabled") == 1) {
		
			if(rangeSet)
				return;
			
			int range = ConfigValues.get("default_spawner_range");
			rangeSet = true;
			
			MobSpawnerLogic logic = (MobSpawnerLogic)(Object)this;
			NbtCompound nbt = new NbtCompound();
        	nbt = logic.writeNbt(nbt);
        	nbt.putShort("RequiredPlayerRange", (short) range);
        	logic.readNbt(level, spawner, nbt);
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getShort(Ljava/lang/String;)S"), method = "readNbt")
	private void readNbt(World world, BlockPos pos, NbtCompound nbt, CallbackInfo info) {

		if(ConfigValues.get("default_spawner_range_enabled") == 0)
			return;
		
		rangeSet = nbt.getBoolean("RangeSet");
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putShort(Ljava/lang/String;S)V"), method = "writeNbt")
	private void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> info) {
		
		if(ConfigValues.get("default_spawner_range_enabled") == 0)
			return;
		
		nbt.putBoolean("RangeSet", rangeSet);
	}
}
