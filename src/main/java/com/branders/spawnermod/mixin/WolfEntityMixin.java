package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity implements IAngerable {

	protected WolfEntityMixin(EntityType<? extends TameableEntity> type, World worldIn) {
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
