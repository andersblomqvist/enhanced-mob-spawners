package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(BeeEntity.class)
public abstract class BeeEntityMixin extends AnimalEntity implements IAngerable, IFlyingAnimal {
	
	protected BeeEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Inject(
			method = "readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundNBT;getInt(Ljava/lang/String;)I", ordinal = 2, shift = At.Shift.AFTER),
            cancellable = true
	)
	private void readAdditional(CompoundNBT tag, CallbackInfo ci) {
		if(!world.isRemote)
			readAngerNBT((ServerWorld) world, tag);
		
		ci.cancel();
	}
}
