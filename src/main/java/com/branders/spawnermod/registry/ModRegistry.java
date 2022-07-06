package com.branders.spawnermod.registry;

import java.util.OptionalInt;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.item.SpawnerKey;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class ModRegistry {

	public static final Item SPAWNER_KEY = new SpawnerKey(new FabricItemSettings().maxDamage(10).group(ItemGroup.TOOLS).rarity(Rarity.RARE));
	public static final Item IRON_GOLEM_SPAWN_EGG = new SpawnEggItem(EntityType.IRON_GOLEM, 15198183, 9794134, (new Item.Settings()).group(ItemGroup.MISC));
	public static final Item SPAWNER = new BlockItem(Blocks.SPAWNER, new Item.Settings().rarity(Rarity.EPIC).group(ItemGroup.DECORATIONS));
	
	public static void register() {
		
		Registry.register(Registry.ITEM, new Identifier(SpawnerMod.MOD_ID, "spawner_key"), SPAWNER_KEY);
		Registry.register(Registry.ITEM, new Identifier(SpawnerMod.MOD_ID, "iron_golem_spawn_egg"), IRON_GOLEM_SPAWN_EGG);
		
		Registry.ITEM.replace(
				OptionalInt.of(Registry.ITEM.getRawId(Items.SPAWNER)), 
				RegistryKey.of(RegistryKey.ofRegistry(new Identifier("spawner")), new Identifier("spawner")), 
				SPAWNER, 
				Registry.ITEM.method_39198());	// method_39198 is a getter for Lifecycle
	}
}
