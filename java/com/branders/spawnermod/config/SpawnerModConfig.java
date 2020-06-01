package com.branders.spawnermod.config;

import com.branders.spawnermod.SpawnerMod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public class SpawnerModConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new Builder();
	
	public static final General GENERAL = new General(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();
	
	public static class General {
		
		public final DoubleValue monster_egg_drop_chance;
		public final BooleanValue disable_silk_touch;
		public final BooleanValue disable_spawner_config;
		public final BooleanValue disable_count;
		public final BooleanValue disable_speed;
		public final BooleanValue disable_range;
		public final BooleanValue disable_egg_removal_from_spawner;
		
		public General(ForgeConfigSpec.Builder builder) {
			builder.push("Drop rates");
			
			monster_egg_drop_chance = builder
					.comment("Drop chance for monster eggs. "
							+ "Value in percentage (%) ranging from 0 - 100. Default is 4.")
					.translation(SpawnerMod.MODID + ".config.monster_egg_drop_chance")
					.defineInRange("monster_egg_drop_chance", 4D, 0D, 100D);
			
			builder.pop();
			
			builder.push("Monster Spawner");
			
			disable_spawner_config = builder
					.comment("Disable/Eanble the spawner config screen.")
					.define("disable_spawner_config", false);
			
			disable_count = builder
					.comment("Disable/Eanble the Count option.")
					.define("disable_count", false);
			
			disable_speed = builder
					.comment("Disable/Eanble the Speed option.")
					.define("disable_speed", false);
			
			disable_range = builder
					.comment("Disable/Eanble the Range option.")
					.define("disable_range", false);
			
			disable_egg_removal_from_spawner = builder
					.comment("Disable/Eanble the ability to right click the spawner for "
							+ "getting the monster egg.")
					.define("disable_egg_removal_from_spawner", false);
			
			disable_silk_touch = builder
					.comment("Disable/Eanble the ability to silk touch a spawner.")
					.translation(SpawnerMod.MODID + ".config.disable_silk_touch")
					.define("disable_silk_touch", false);
			
			builder.pop();
		}
	}

	/**
	 * 	Called from server {@link SyncSpawnerConfig} to sync with client config values.
	 * 
	 * 	@param disable_spawner_config
	 * 	@param disable_count
	 * 	@param disable_speed
	 * 	@param disable_range
	 */
	public static void sync(boolean disable_spawner_config, boolean disable_count, boolean disable_speed,
			boolean disable_range) {
		GENERAL.disable_spawner_config.set(disable_spawner_config);
		GENERAL.disable_count.set(disable_count);
		GENERAL.disable_speed.set(disable_speed);
		GENERAL.disable_range.set(disable_range);
	}
}