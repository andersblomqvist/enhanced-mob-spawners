package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends MonsterEntity implements IAngerable {

	protected EndermanEntityMixin(EntityType<? extends MonsterEntity> type, World worldIn) {
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
