package com.branders.spawnermod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnConfig 
{
	public static ForgeConfigSpec.IntValue monster_egg_drop_chance;
	
	public static void init(ForgeConfigSpec.Builder common)
	{
		common.comment("Spawn Config");
		
		monster_egg_drop_chance = common
				.comment("Drop chance for monster eggs. Value in percentage (%) ranging from 0 - 100. Default is 4")
				.defineInRange("spawnconfig.monster_egg_drop_chance", 4, 0, 100);
	}
}
