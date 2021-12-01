package com.branders.spawnermod.config;

import java.util.HashMap;
import java.util.Set;

import com.branders.spawnermod.SpawnerMod;

import net.minecraftforge.registries.ForgeRegistries;

/**
 * 	All mod config values are stored here.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class ConfigValues {
	
	private static HashMap<String, Integer> CONFIG_SPEC = new HashMap<String, Integer>();
	
	/**
	 * 	Initializes the CONFIG_SPEC hashmap with key value pairs where the values
	 * 	are set to default.
	 */
	public static void setDefaultConfigValues() {
		CONFIG_SPEC.put("monster_egg_drop_chance", 4);
		CONFIG_SPEC.put("disable_silk_touch", 0);
		CONFIG_SPEC.put("disable_spawner_config", 0);
		CONFIG_SPEC.put("disable_count", 0);
		CONFIG_SPEC.put("disable_range", 0);
		CONFIG_SPEC.put("disable_speed", 0);
		CONFIG_SPEC.put("limited_spawns_enabled", 0);
		CONFIG_SPEC.put("limited_spawns_amount", 32);
		CONFIG_SPEC.put("disable_egg_removal_from_spawner", 0);
		CONFIG_SPEC.put("monster_egg_only_drop_when_killed_by_player", 0);
		
		// Loop through item registry and insert all spawn egg entities to hash map.
		// Example of a key: "minecraft:pig" with default value 0.
		ForgeRegistries.ITEMS.getKeys().stream().forEach(i -> {
			String s = i.toString();
			if(i.toString().contains("spawn_egg")) {
				s = s.substring(0, s.length() - 10);	// 10 is length of "_spawn_egg"
				CONFIG_SPEC.put(s, 0);
			}				
		});
	}
	
	/**
	 * 	Associates the specified value with the specified key in this map.
	 * 	If the map previously contained a mapping for the key, the old value is replaced.
	 * 
	 * 	@param key with which the specified value is to be associated
	 * 	@param value to be associated with the specified key
	 */
	public static void put(String key, int value) {
		CONFIG_SPEC.put(key, value);
	}
	
	/**
	 * 	Tries to get the value associated with given key.
	 *  
	 * 	@param key with which the specified value is to be associated
	 * 	@return Returns its value if it exists otherwise 0.
	 */
	public static int get(String key) {
		if(CONFIG_SPEC.containsKey(key))
			return CONFIG_SPEC.get(key);
		else {
			SpawnerMod.LOGGER.warn("Key=" + key + " was not found when trying to access it! Returning 0");
			return 0;
		}
	}
	
	/**
	 * 	@return a set view of the keys contained in this map
	 */
	public static Set<String> getKeys() {
		return CONFIG_SPEC.keySet();
	}
	
	/**
	 * 	Check whether a specific entity egg is disabled or not.
	 * 
	 * 	@param string entity identifier name ("minecraft:pig").
	 * 	@return true if the specified entity egg is disabled in config. Otherwise false.
	 */
	public static boolean isEggDisabled(String identifier) {
		if(get(identifier) == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * 	Sync the server config with client when player joins a server.
	 * 
	 * 	@param config 
	 * 	@param count
	 * 	@param speed
	 * 	@param range
	 */
	public static void sync(int config, int count, int speed, int range) {
		put("disable_spawner_config", config);
		put("disable_count", count);
		put("disable_speed", speed);
		put("disable_range", range);
	}
}
