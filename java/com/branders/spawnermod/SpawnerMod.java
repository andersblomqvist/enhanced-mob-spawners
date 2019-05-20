package com.branders.spawnermod;

import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/**
 * 	Small mod adding more functionality to Mob Spawners
 * 
 * 	@author Branders
 *
 */
@Mod(SpawnerMod.modid)
public class SpawnerMod
{		
	public static final String modid = "spawnermod";
	
	/**
	* 	Register event handler
	*/
    public SpawnerMod() 
    {
    	MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
    }
}
