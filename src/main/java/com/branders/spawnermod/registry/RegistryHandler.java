package com.branders.spawnermod.registry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RegistryHandler {

	public static void init() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ItemRegistry items = new ItemRegistry();
		bus.register(items);
		items.register(bus);
	}
}
