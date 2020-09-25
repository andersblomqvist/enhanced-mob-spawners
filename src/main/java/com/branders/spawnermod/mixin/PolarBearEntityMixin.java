package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

@Mixin(PolarBearEntity.class)
public abstract class PolarBearEntityMixin extends AnimalEntity implements IAngerable {
	
	protected PolarBearEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn) {
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
