package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(PolarBearEntity.class)
public abstract class PolarBearEntityMixin extends AnimalEntity implements IAngerable {
	
	protected PolarBearEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Inject(
			method = "readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V", shift = At.Shift.AFTER),
            cancellable = true
	)
	private void readAdditional(CompoundNBT tag, CallbackInfo ci) {
		if(!world.isRemote)
			readAngerNBT((ServerWorld) world, tag);
		
		ci.cancel();
	}
}
