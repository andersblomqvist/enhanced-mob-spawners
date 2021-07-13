package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;

/**
 * 	This mixin fixes the bug where the egg inside the spawner would go back to previous entity if the 
 * 	entity was changed while spawner is turned off.
 * 
 * 	Produced by these steps:
 * 		1. Place down spawner.
 * 		2. Turn it off (either with key or redstone)
 * 		3. Insert new egg.
 * 		4. Turn it back on again.
 * 		5. When the spawner decides to spawn the entity it will spawn the correct entity one round
 * 		   and then go back to the entity at stage 1.
 * 	
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(MobSpawnerTileEntity.class)
public class SpawnerBlockEntityMixin {

	@Inject(at = @At(value = "TAIL"), method = "getUpdateTag()Lnet/minecraft/nbt/CompoundNBT;")
	public CompoundNBT getUpdateTag(CallbackInfoReturnable<CompoundNBT> info) {
		CompoundNBT nbt = ((MobSpawnerTileEntity)(Object)this).save(new CompoundNBT());
		ListNBT list = nbt.getList("SpawnPotentials", 10);
		String e1 = list.getCompound(0).getCompound("Entity").getString("id");
		String e2 = nbt.getCompound("SpawnData").getString("id");
		
		if(!(e1.equals(e2))) {
			list.getCompound(0).getCompound("Entity").putString("id", e2);
			nbt.put("SpawnPotentials", list);
		}
		return nbt;
	}
}