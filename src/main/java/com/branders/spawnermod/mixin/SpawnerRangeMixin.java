package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

/**
 *	Sets the required player range one time to config value.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(BaseSpawner.class)
public class SpawnerRangeMixin {
	
	private boolean rangeSet = false;
	
	@Inject(
			at = @At("HEAD"),
			method = "isNearPlayer("
					+ "Lnet/minecraft/world/level/Level;"
					+ "Lnet/minecraft/core/BlockPos;"
					+ ")"
					+ "Z",
			cancellable = true)
	private void isNearPlayer(Level level, BlockPos spawner, CallbackInfoReturnable<Boolean> cir) {
		
		if(ConfigValues.get("default_spawner_range_enabled") == 1) {
		
			if(rangeSet)
				return;
			
			int range = ConfigValues.get("default_spawner_range");
			
			BaseSpawner logic = (BaseSpawner)(Object)this;
			CompoundTag nbt = new CompoundTag(); 
        	nbt = logic.save(nbt);
        	nbt.putShort("RequiredPlayerRange", (short) range);
        	logic.load(level, spawner, nbt);
        	
        	rangeSet = true;
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S"), method = "load")
	private void load(Level level, BlockPos pos, CompoundTag nbt, CallbackInfo info) {

		if(ConfigValues.get("default_spawner_range_enabled") == 0)
			return;
		
		rangeSet = nbt.getBoolean("RangeSet");
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"), method = "save")
	private void save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
		
		if(ConfigValues.get("default_spawner_range_enabled") == 0)
			return;
		
		nbt.putBoolean("RangeSet", rangeSet);
	}
}
