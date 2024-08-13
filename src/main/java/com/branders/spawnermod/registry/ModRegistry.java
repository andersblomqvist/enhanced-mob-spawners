package com.branders.spawnermod.registry;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.command.SpawnerModCommands;
import com.branders.spawnermod.item.SpawnerKey;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModRegistry {

    public static final Item SPAWNER_KEY = new SpawnerKey(new Item.Settings().maxDamage(10).rarity(Rarity.RARE));

    public static void register() {

        // Item groups
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(SPAWNER_KEY));

        Registry.register(Registries.ITEM, Identifier.of(SpawnerMod.MOD_ID, "spawner_key"), SPAWNER_KEY);

        SpawnerModCommands.register();
    }

    /**
     * Given the entity, get its spawn egg registry name. Spawn eggs can have
     * different naming schemes.
     * 
     * @param entityString
     * @return modid:entity_spawn_egg or whackmod_spawn_egg_entity
     */
    public static String getSpawnEggRegistryName(String entityString) {
        Item egg = null;

        // if we follow minecraft naming conventions this will not be null
        egg = Registries.ITEM.get(Identifier.of(entityString + "_spawn_egg"));

        if (egg == null) {
            // entity is "whackmod:pig" and we want it to be "whackmod:spawn_egg_pig"
            String[] split = entityString.split(":");
            assert (split.length == 2);
            String id = split[0];
            String e = "spawn_egg_" + split[1];
            return id + ":" + e;
        }

        return entityString + "_spawn_egg";
    }
}
