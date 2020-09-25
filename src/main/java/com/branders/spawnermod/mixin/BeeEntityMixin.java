package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

@Mixin(BeeEntity.class)
public abstract class BeeEntityMixin extends AnimalEntity implements IAngerable, IFlyingAnimal {
	
	protected BeeEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	/**
	 * 	Fixes Minecraft bug where server world would be casted on client.
	 * 	@reason bug
	 * 	@author
	 */
	@Overwrite
	public void readAdditional(CompoundNBT compound) {
		if(!world.isRemote) {
			super.readAdditional(compound);
        }
	}
}
