package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

/**
 *	
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
}
