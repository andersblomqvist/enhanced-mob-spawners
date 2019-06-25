package com.branders.spawnermod.config;

import com.branders.spawnermod.SpawnerMod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SpawnerMod.MODID)
@Config.LangKey("spawnermod.config")
public class SpawnerModConfig 
{	
	@Config.RangeInt(min = 0, max = 100)
	@Config.Comment("Drop rate for Monster Eggs from all mobs (in percentage ranging from 0 - 100)")
	public static int drop_rate_in_percentage = 4;
	
	@Mod.EventBusSubscriber(modid = SpawnerMod.MODID)
	private static class EventHandler
	{
		/**
		 * 	Inject the new values and save to the config file when the config has been changed from the GUI.
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) 
		{
			if (event.getModID().equals(SpawnerMod.MODID)) 
			{
				ConfigManager.sync(SpawnerMod.MODID, Config.Type.INSTANCE);
			}
		}
	}
}
