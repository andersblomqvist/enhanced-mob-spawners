package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(ZombifiedPiglinEntity.class)
public abstract class ZombiefiedPiglinEntityMixin extends MonsterEntity implements IAngerable {

	protected ZombiefiedPiglinEntityMixin(EntityType<? extends MonsterEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Inject(
            method = "readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/ZombieEntity;readAdditional(Lnet/minecraft/nbt/CompoundNBT;)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void readAdditional(CompoundNBT tag, CallbackInfo ci) {
        if(!world.isRemote)
            readAngerNBT((ServerWorld) world, tag);

        ci.cancel();
    }
}
