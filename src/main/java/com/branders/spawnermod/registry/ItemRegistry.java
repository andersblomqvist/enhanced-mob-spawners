package com.branders.spawnermod.registry;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.item.SpawnerKeyItem;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber
public class ItemRegistry {

	public static final DeferredRegister<Item> MINECRAFT_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpawnerMod.MOD_ID);

	public static final RegistryObject<Item> SPAWNER_KEY = ITEMS.register(
			"spawner_key", () -> new SpawnerKeyItem(new Item.Properties().durability(10).rarity(Rarity.RARE)));

	public void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
		MINECRAFT_ITEMS.register(modEventBus);
	}
	
	@SubscribeEvent
	public void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(SPAWNER_KEY);
		}
	}
}