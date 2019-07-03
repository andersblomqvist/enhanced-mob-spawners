package com.branders.spawnermod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SpawnerKeyItem extends Item
{
	public SpawnerKeyItem(Properties properties) 
	{
		super(properties);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) 
	{
		return true;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) 
	{
		return 1;
	}
}
