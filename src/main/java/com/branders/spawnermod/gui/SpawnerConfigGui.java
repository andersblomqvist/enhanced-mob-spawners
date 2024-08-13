package com.branders.spawnermod.gui;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.networking.packet.SyncSpawnerPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Spawner GUI config screen. Renders the background and all the buttons. It
 * communicates with the spawner block by sending a network package with data
 * from the GUI elements.
 * 
 * @author Anders <Branders> Blomqvist
 */
@Environment(EnvType.CLIENT)
public class SpawnerConfigGui extends Screen {

    /**
     * Hold values for all NBT parameters we modify.
     */
    private static class Data {
        short LOW, DEFAULT, HIGH, HIGHEST;

        public Data(int i, int j, int k, int l) {
            LOW = (short) i;
            DEFAULT = (short) j;
            HIGH = (short) k;
            HIGHEST = (short) l;
        }
    }

    // Create the data for spawner logic NBT
    private static final Data DELAY = new Data(30, 20, 10, 5);
    private static final Data MIN_SPAWN_DELAY = new Data(300, 200, 100, 50);
    private static final Data MAX_SPAWN_DELAY = new Data(900, 800, 400, 100);
    private static final Data SPAWN_COUNT = new Data(2, 4, 6, 12);
    private static final Data MAX_NEARBY_ENTITIES = new Data(6, 6, 12, 24);
    private static final Data REQUIRED_PLAYER_RANGE = new Data(16, 32, 64, 128);

    // "Spawner Config" title in yellow at the top
    private static final Text TITLE_TEXT = Text.translatable("gui.spawnermod.spawner_config_screen_title");

    private static final Identifier SPAWNER_CONFIG_TEXTURE = Identifier.of(SpawnerMod.MOD_ID,
            "textures/gui/spawner_config_screen.png");
    private static final Identifier SPAWNS_ICON_TEXTURE = Identifier.of(SpawnerMod.MOD_ID,
            "textures/gui/spawner_config_screen_icon_spawns.png");
    private static final int SPAWNER_CONFIG_TEXTURE_WIDTH = 178;
    private static final int SPAWNER_CONFIG_TEXTURE_HEIGHT = 177;

    // Buttons for controlling Spawner data
    private ButtonWidget countButton;
    private ButtonWidget speedButton;
    private ButtonWidget rangeButton;
    private ButtonWidget disableButton;

    // Button States
    private int countOptionValue;
    private int speedOptionValue;
    private int rangeOptionValue;

    // Create the variables which holds current NBT value
    private short delay;
    private short minSpawnDelay;
    private short maxSpawnDelay;
    private short spawnCount;
    private short maxNearbyEntities;
    private short requiredPlayerRange;
    private boolean disabled;
    private short spawns;

    private final boolean cachedDisabled;
    private final boolean limitedSpawns;

    private boolean isCustomRange;
    private short customRange;

    private final BlockPos pos;

    /**
     * When creating this GUI a reference to the Mob Spawner logic and BlockPos is
     * required so we can read current NBT values (used to make GUI remember option
     * states) and send network package to server with a reference to the spawner
     * block position.
     */
    public SpawnerConfigGui(Text title, MobSpawnerLogic logic, BlockPos pos) {

        super(title);

        this.pos = pos;

        if (ConfigValues.get("default_spawner_range_enabled") == 1) {
            isCustomRange = true;
            customRange = (short) ConfigValues.get("default_spawner_range");
        }

        // Read values for Spawner to check what type of configuration it has so we can
        // render
        // correct button display strings. We have to read all the values in case the
        // player
        // doesn't change anything and presses save button.
        NbtCompound nbt = new NbtCompound();
        nbt = logic.writeNbt(nbt);
        delay = nbt.getShort("Delay");
        minSpawnDelay = nbt.getShort("MinSpawnDelay");
        maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
        spawnCount = nbt.getShort("SpawnCount");
        maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
        requiredPlayerRange = nbt.getShort("RequiredPlayerRange");

        // If spawn range differs from 4, spawner is in the disabled state and
        // the previous range is stored in this value
        short spawnRange = nbt.getShort("SpawnRange");
        if (spawnRange > 4) {
            disabled = true;
            cachedDisabled = true;

            // Set gui range to saved spawnRange value.
            requiredPlayerRange = spawnRange;
        } else {
            disabled = false;
            cachedDisabled = false;
        }

        // Load button configuration
        countOptionValue = loadOptionState(spawnCount, SPAWN_COUNT);
        speedOptionValue = loadOptionState(minSpawnDelay, MIN_SPAWN_DELAY);
        rangeOptionValue = loadOptionState(requiredPlayerRange, REQUIRED_PLAYER_RANGE);

        if (ConfigValues.get("limited_spawns_enabled") != 0) {
            limitedSpawns = true;
            if (nbt.contains("spawns")) {
                spawns = nbt.getShort("spawns");
                if (ConfigValues.get("limited_spawns_amount") - spawns == 0) {
                    // Spawner has ran out of spawns. Disable.
                    disabled = true;
                }
            }
        } else
            limitedSpawns = false;
    }

    @Override
    protected void init() {

        countButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.count." + getButtonText(countOptionValue)), button -> {
                    switch (countOptionValue) {
                    // Low, set to Default
                    case 0:
                        countOptionValue = 1;
                        spawnCount = SPAWN_COUNT.DEFAULT;
                        maxNearbyEntities = MAX_NEARBY_ENTITIES.DEFAULT;
                        break;

                    // Default, set to High
                    case 1:
                        countOptionValue = 2;
                        spawnCount = SPAWN_COUNT.HIGH;
                        maxNearbyEntities = MAX_NEARBY_ENTITIES.HIGH;
                        break;

                    // High, set to Very High
                    case 2:
                        countOptionValue = 3;
                        spawnCount = SPAWN_COUNT.HIGHEST;
                        maxNearbyEntities = MAX_NEARBY_ENTITIES.HIGHEST;
                        break;

                    // Very high, set back to Low
                    case 3:
                        countOptionValue = 0;
                        spawnCount = SPAWN_COUNT.LOW;
                        maxNearbyEntities = MAX_NEARBY_ENTITIES.LOW;
                        break;
                    }
                    countButton.setMessage(Text.translatable("button.count." + getButtonText(countOptionValue)));
                }).dimensions(width / 2 - 48, 55, 108, 20).build());

        speedButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.speed." + getButtonText(speedOptionValue)), button -> {
                    switch (speedOptionValue) {
                    // Slow, set to default
                    case 0:
                        speedOptionValue = 1;
                        delay = DELAY.DEFAULT;
                        minSpawnDelay = MIN_SPAWN_DELAY.DEFAULT;
                        maxSpawnDelay = MAX_SPAWN_DELAY.DEFAULT;
                        break;

                    // Default, set to Fast
                    case 1:
                        speedOptionValue = 2;
                        delay = DELAY.HIGH;
                        minSpawnDelay = MIN_SPAWN_DELAY.HIGH;
                        maxSpawnDelay = MAX_SPAWN_DELAY.HIGH;
                        break;

                    // High, set to Very Fast
                    case 2:
                        speedOptionValue = 3;
                        delay = DELAY.HIGHEST;
                        minSpawnDelay = MIN_SPAWN_DELAY.HIGHEST;
                        maxSpawnDelay = MAX_SPAWN_DELAY.HIGHEST;
                        break;

                    // Very high, set back to Slow
                    case 3:
                        speedOptionValue = 0;
                        delay = DELAY.LOW;
                        minSpawnDelay = MIN_SPAWN_DELAY.LOW;
                        maxSpawnDelay = MAX_SPAWN_DELAY.LOW;
                        break;
                    }
                    speedButton.setMessage(Text.translatable("button.speed." + getButtonText(speedOptionValue)));
                }).dimensions(width / 2 - 48, 80, 108, 20).build());

        rangeButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.range." + getButtonText(rangeOptionValue)).append(" " + requiredPlayerRange),
                button -> {
                    switch (rangeOptionValue) {
                    // Default, set to Far
                    case 0:
                        rangeOptionValue = 1;
                        requiredPlayerRange = REQUIRED_PLAYER_RANGE.DEFAULT;
                        break;

                    // Far, set to Very Far
                    case 1:
                        rangeOptionValue = 2;
                        requiredPlayerRange = REQUIRED_PLAYER_RANGE.HIGH;
                        break;

                    // Very Far, set to Extreme
                    case 2:
                        rangeOptionValue = 3;
                        requiredPlayerRange = REQUIRED_PLAYER_RANGE.HIGHEST;
                        break;

                    // Extreme, set back to Default or Custom
                    case 3:
                        if (isCustomRange) {
                            rangeOptionValue = 4;
                            requiredPlayerRange = customRange;
                        } else {
                            rangeOptionValue = 0;
                            requiredPlayerRange = REQUIRED_PLAYER_RANGE.LOW;
                        }
                        break;

                    // Custom, set back to Default
                    case 4:
                        rangeOptionValue = 0;
                        requiredPlayerRange = REQUIRED_PLAYER_RANGE.LOW;
                        break;
                    }
                    rangeButton.setMessage(Text.translatable("button.range." + getButtonText(rangeOptionValue))
                            .append(" " + requiredPlayerRange));
                }).dimensions(width / 2 - 48, 105, 108, 20).build());

        disableButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.toggle." + getButtonText(disabled)), button -> {
                    if (disabled) {
                        // Set spawner to ON
                        disabled = false;
                        toggleButtons(true);
                        switch (rangeOptionValue) {
                        case 0:
                            requiredPlayerRange = REQUIRED_PLAYER_RANGE.LOW;
                            break;
                        case 1:
                            requiredPlayerRange = REQUIRED_PLAYER_RANGE.DEFAULT;
                            break;
                        case 2:
                            requiredPlayerRange = REQUIRED_PLAYER_RANGE.HIGH;
                            break;
                        case 3:
                            requiredPlayerRange = REQUIRED_PLAYER_RANGE.HIGHEST;
                            break;
                        }
                    } else {
                        // Set spawner OFF
                        disabled = true;
                        toggleButtons(false);
                        requiredPlayerRange = 0;
                    }
                    disableButton.setMessage(Text.translatable("button.toggle." + getButtonText(disabled)));
                }).dimensions(width / 2 - 48, 130, 108, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.save"), button -> {
            configureSpawner();
            this.close();
        }).dimensions(width / 2 - 89, 180 + 10, 178, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.cancel"), button -> {
            this.close();
        }).dimensions(width / 2 - 89, 180 + 35, 178, 20).build());

        toggleButtons(!disabled);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        super.renderBackground(context, mouseX, mouseY, delta);

        context.drawTexture(SPAWNER_CONFIG_TEXTURE, width / 2 - SPAWNER_CONFIG_TEXTURE_WIDTH / 2, 5, 0, 0,
                SPAWNER_CONFIG_TEXTURE_WIDTH, SPAWNER_CONFIG_TEXTURE_HEIGHT, SPAWNER_CONFIG_TEXTURE_WIDTH,
                SPAWNER_CONFIG_TEXTURE_HEIGHT);

        // Render spawner title text
        int length = TITLE_TEXT.getString().length() * 2;
        context.drawTextWithShadow(client.textRenderer, TITLE_TEXT, width / 2 - length - 3, 33, 0xFFD964);

        // Render spawns icon and text (only if enabled in config)
        if (limitedSpawns) {
            context.drawTexture(SPAWNS_ICON_TEXTURE, width / 2 - 7 + 101, 23, 0, 0, 14, 14, 14, 14);
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal("" + (ConfigValues.get("limited_spawns_amount") - spawns)), width / 2 + 114, 27,
                    0xFFFFFF);
        }
    }

    private void configureSpawner() {

        if (cachedDisabled)
            if (disabled)
                return;

        ClientPlayNetworking.send(new SyncSpawnerPacket(pos, delay, spawnCount, requiredPlayerRange, maxNearbyEntities,
                minSpawnDelay, maxSpawnDelay));
    }

    /**
     * @return a string with the last part of the translation key depending on the
     *         button option value.
     */
    private String getButtonText(int optionValue) {
        return switch (optionValue) {
        case 0 -> "low";
        case 1 -> "default";
        case 2 -> "high";
        case 3 -> "very_high";
        case 4 -> "custom";
        default -> "default";
        };
    }

    private String getButtonText(boolean disabled) {
        if (disabled)
            return "disabled";
        else
            return "enabled";
    }

    /**
     * Loads what type of configuration spawner has. So it can remember what we have
     * changed
     * 
     * @param current   value which the spawner has when player just right clicked
     *                  it
     * @param reference reference to all the Low -> Highest data values
     * @return optionValue current config spec
     */
    private int loadOptionState(short current, Data reference) {
        if (isCustomRange && current == customRange)
            return 4;

        if (current == reference.LOW)
            return 0;
        else if (current == reference.DEFAULT)
            return 1;
        else if (current == reference.HIGH)
            return 2;
        else if (current == reference.HIGHEST)
            return 3;
        else
            return 0;
    }

    /**
     * Toggles the count, speed and range button to specified state. However take
     * the config into consideration first.
     * 
     * @param state True/False - On/Off
     */
    private void toggleButtons(boolean state) {

        if (ConfigValues.get("disable_count") != 0) {
            countButton.active = false;
            countButton.setMessage(Text.translatable("button.count.disabled"));
        } else
            countButton.active = state;

        if (ConfigValues.get("disable_speed") != 0) {
            speedButton.active = false;
            speedButton.setMessage(Text.translatable("button.speed.disabled"));
        } else
            speedButton.active = state;

        if (ConfigValues.get("disable_range") != 0) {
            rangeButton.active = false;
            rangeButton.setMessage(Text.translatable("button.range.disabled"));
        } else
            rangeButton.active = state;
    }
}
