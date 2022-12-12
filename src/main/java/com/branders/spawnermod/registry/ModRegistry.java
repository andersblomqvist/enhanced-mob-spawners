package com.branders.spawnermod.registry;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.item.SpawnerKey;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModRegistry {

    public static final Item SPAWNER_KEY = new SpawnerKey(new FabricItemSettings().maxDamage(10).rarity(Rarity.RARE));	

    public static void register() {

        // Item groups
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(SPAWNER_KEY));

        Registry.register(Registries.ITEM, new Identifier(SpawnerMod.MOD_ID, "spawner_key"), SPAWNER_KEY);
    }
}
