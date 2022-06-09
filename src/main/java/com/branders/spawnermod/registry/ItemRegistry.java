package com.branders.spawnermod.registry;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.item.SpawnerKeyItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

	public static final DeferredRegister<Item> MINECRAFT_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpawnerMod.MOD_ID);
	
	public static final RegistryObject<Item> SPAWNER_KEY = ITEMS.register("spawner_key", () -> new SpawnerKeyItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).durability(10).rarity(Rarity.RARE)));
	public static final RegistryObject<Item> SPAWNER_BLOCK = MINECRAFT_ITEMS.register("spawner", () -> new BlockItem(Blocks.SPAWNER, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.RARE)));
	
	@SuppressWarnings("deprecation")
	public static final RegistryObject<SpawnEggItem> IRON_GOLEM_SPAWN_EGG = ITEMS.register("iron_golem_spawn_egg", () -> new SpawnEggItem(EntityType.IRON_GOLEM, 15198183, 9794134, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
	
	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
		MINECRAFT_ITEMS.register(modEventBus);
	}
}
