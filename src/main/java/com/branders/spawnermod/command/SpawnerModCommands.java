package com.branders.spawnermod.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.config.ModConfigManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;

public class SpawnerModCommands {

    /**
     * Register commands for changing the config ingame. The first command will set
     * a new value and the second will only print current value.
     * 
     * All commands have the format: <br>
     * <li>/ems name value</li>
     * <li>/ems name</li>
     */
    public static void register() {
        newCommand("default_spawner_range", IntegerArgumentType.integer(0));
        newCommand("default_spawner_range_enabled", IntegerArgumentType.integer(0, 1));
        newCommand("disable_count", IntegerArgumentType.integer(0, 1));
        newCommand("disable_egg_removal_from_spawner", IntegerArgumentType.integer(0, 1));
        newCommand("disable_range", IntegerArgumentType.integer(0, 1));
        newCommand("disable_silk_touch", IntegerArgumentType.integer(0, 1));
        newCommand("disable_spawner_config", IntegerArgumentType.integer(0, 1));
        newCommand("disable_speed", IntegerArgumentType.integer(0, 1));
        newCommand("display_item_id_from_right_click_in_log", IntegerArgumentType.integer(0, 1));
        newCommand("limited_spawns_amount", IntegerArgumentType.integer(0));
        newCommand("limited_spawns_enabled", IntegerArgumentType.integer(0, 1));
        newCommand("monster_egg_drop_chance", IntegerArgumentType.integer(0, 100));
        newCommand("monster_egg_only_drop_when_killed_by_player", IntegerArgumentType.integer(0, 1));
        newCommand("spawner_hardness", IntegerArgumentType.integer(0));

        ConfigValues.getSpawnEggEntities().forEachRemaining(e -> {
            newCommand(e, IntegerArgumentType.integer(0, 1));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment.integrated) {
                dispatcher.register(literal("ems").then(literal("reset").executes(ctx -> {
                    ConfigValues.setDefaultConfigValues();
                    ctx.getSource().sendFeedback(() -> Text.literal("[EMS]: Config reset to default"), false);
                    return 1;
                })));
            }
        });

    }

    private static void newCommand(String literal, IntegerArgumentType type) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment.integrated) {
                dispatcher.register(literal("ems").then(literal(literal).executes(ctx -> {
                    final int dropChance = ConfigValues.get(literal);
                    ctx.getSource().sendFeedback(
                            () -> Text.literal("[EMS]: %s is currently set to %s".formatted(literal, dropChance)),
                            false);
                    return 1;
                }).then(argument("value", type).requires(source -> source.hasPermissionLevel(2)).executes(ctx -> {
                    final int dropChance = IntegerArgumentType.getInteger(ctx, "value");
                    ConfigValues.put(literal, dropChance);
                    ctx.getSource().sendFeedback(
                            () -> Text.literal("[EMS]: %s updated to %s".formatted(literal, dropChance)), false);
                    ModConfigManager.saveConfigToFile();
                    return 1;
                }))));
            }
        });
    }
}
