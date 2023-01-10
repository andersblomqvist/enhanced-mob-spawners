package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;

/**
 * 	Implements a limit to how many spawns a spawner can do. Only if enabled in config!
 * 
 * 	@author Anders <Branders> Blomqvist 
 */
@Mixin(BaseSpawner.class)
public class LimitedSpawnsMixin {
	
	private short spawns = 0;
	
	@Inject(at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"), 
			method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V", 
			cancellable = true)
	private void entitySpawn(ServerLevel level, BlockPos pos, CallbackInfo ci) {
		
		if(ConfigValues.get("limited_spawns_enabled") == 0)
			return;
		
		// Don't count "empty" entities.
		CompoundTag nbt = new CompoundTag(); 
    	nbt = ((BaseSpawner)(Object)this).save(nbt);
    	String entity_string = nbt.get("SpawnData").toString();
    	entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
    	entity_string = entity_string.substring(0, entity_string.indexOf("\""));
    	if(entity_string.contains("area_effect_cloud"))
    		return;
    	
		spawns++;
	}
	
	@Inject(at = @At(value = "INVOKE_ASSIGN", 
			target = "Lnet/minecraft/world/entity/EntityType;by(Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"), 
			method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V", 
			cancellable = true)
    private void cancel(ServerLevel level, BlockPos pos, CallbackInfo ci) {
		
		if(ConfigValues.get("limited_spawns_enabled") == 0)
			return;
		
		level.getBlockEntity(pos).setChanged();
		level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
		
        if (spawns >= ConfigValues.get("limited_spawns_amount")) {
        	
        	BaseSpawner logic = ((BaseSpawner)(Object)this);
        	
        	// Disable the spawner AND remove egg.
        	CompoundTag nbt = new CompoundTag(); 
        	nbt = logic.save(nbt);
        	nbt.putShort("RequiredPlayerRange", (short) 0);
        	logic.load(level, pos, nbt);
        	logic.m_253197_(EntityType.AREA_EFFECT_CLOUD, level, level.random, pos);
        	level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
        	ci.cancel();
        }
    }
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S"), method = "load")
	private void load(Level level, BlockPos pos, CompoundTag nbt, CallbackInfo info) {

		if(ConfigValues.get("limited_spawns_enabled") == 0)
			return;
		
		spawns = nbt.getShort("spawns");
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"), method = "save")
	private void save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
		
		if(ConfigValues.get("limited_spawns_enabled") == 0)
			return;
		
		nbt.putShort("spawns", spawns);
	}
}
