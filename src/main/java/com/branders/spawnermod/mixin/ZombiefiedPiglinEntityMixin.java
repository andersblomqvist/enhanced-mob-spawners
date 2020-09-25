package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

@Mixin(ZombifiedPiglinEntity.class)
public abstract class ZombiefiedPiglinEntityMixin extends MonsterEntity implements IAngerable {

	protected ZombiefiedPiglinEntityMixin(EntityType<? extends MonsterEntity> type, World worldIn) {
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
