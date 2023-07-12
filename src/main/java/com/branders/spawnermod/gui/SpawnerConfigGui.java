package com.branders.spawnermod.gui;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BaseSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 	Spawner GUI config screen. Renders the background and all the buttons.
 * 
 * 	It communicates with the spawner object by sending a network package with
 * 	data from the GUI elements. 
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@OnlyIn(Dist.CLIENT)
public class SpawnerConfigGui extends Screen {
	
	private static final Component titleText = Component.translatable("gui.spawnermod.spawner_config_screen_title");

	// Used for rendering.
	private Minecraft minecraft = Minecraft.getInstance();

	// References to Spawner Logic and NBT Data. Set in constructor
	private BaseSpawner logic;
	private BlockPos pos;
	private CompoundTag nbt = new CompoundTag();

	// GUI Texture
	private ResourceLocation spawnerConfigTexture =new ResourceLocation(
			SpawnerMod.MOD_ID, "/textures/gui/spawner_config_screen.png");
	private int imageWidth = 178;
	private int imageHeight = 177;
	private ResourceLocation spawnsIconTexture = new ResourceLocation(
			SpawnerMod.MOD_ID, "textures/gui/spawner_config_screen_icon_spawns.png");

	// Buttons for controlling Spawner data
	private Button countButton = null;
	private Button speedButton = null;
	private Button rangeButton = null;
	private Button disableButton = null;

	// Button States
	private int countOptionValue;
	private int speedOptionValue;
	private int rangeOptionValue;

	// What the button will display depending on option value
	String[] speedDisplayString = {"Slow", "Default", "Fast", "Very Fast"};
	String[] countDisplayString = {"Low", "Default", "High", "Very High"};
	String[] rangeDisplayString = {"Default", "Far", "Very Far", "Extreme"};
	String[] disableDisplayString = {"Enabled", "Disabled"};

	/**
	 * 	Object to hold values for all NBT parameters we modify.
	 * 	Each parameter holds 4 different values: Low - Default - High - Highest. These are used to get
	 * 	different values depending on what type of option the player decides to use.
	 */
	private class Data {
		short LOW, DEFAULT, HIGH, HIGHEST;
		public Data(int i, int j, int k, int l) {
			LOW = (short)i;
			DEFAULT = (short)j;
			HIGH = (short)k;
			HIGHEST = (short)l;
		}
	}

	// Create the data for spawner logic NBT (only used as reference)
	private Data _delay               = new Data(30, 20, 10, 5);
	private Data _minSpawnDelay       = new Data(300, 200, 100, 50);
	private Data _maxSpawnDelay       = new Data(900, 800, 400, 100);
	private Data _spawnCount          = new Data(2, 4, 6, 12);
	private Data _maxNearbyEntities   = new Data(6, 6, 12, 24);
	private Data _requiredPlayerRange = new Data(16, 32, 64, 128);

	// Create the variables which holds current NBT value
	private short delay;
	private short minSpawnDelay;
	private short maxSpawnDelay;
	private short spawnCount;
	private short maxNearbyEntities;
	private short requiredPlayerRange;
	private short spawnRange;
	private boolean disabled;
	private short spawns;

	private boolean cachedDisabled;
	private boolean limitedSpawns;

	private boolean isCustomRange;
	private short customRange;

	/**
	 * 	When creating this GUI a reference to the Mob Spawner logic and BlockPos is required so we can read
	 * 	current NBT values (used to make GUI remember option states) and send network package to server with
	 * 	a reference to the spawner block position.
	 */
	public SpawnerConfigGui(Component textComponent, BaseSpawner logic, BlockPos pos) {

		super(textComponent);

		this.logic = logic;
		this.pos = pos;

		if(ConfigValues.get("default_spawner_range_enabled") == 1) {
			isCustomRange = true;
			customRange = (short)ConfigValues.get("default_spawner_range");
		}

		// Read values for Spawner to check what type of configuration it has so we can render
		// correct button display strings. We have to read all the values in case the player
		// doesn't change anything and presses save button.
		nbt = this.logic.save(nbt);
		delay = nbt.getShort("Delay");
		minSpawnDelay = nbt.getShort("MinSpawnDelay");
		maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
		spawnCount = nbt.getShort("SpawnCount");
		maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
		requiredPlayerRange = nbt.getShort("RequiredPlayerRange");

		// If spawn range differs from 4, spawner is in the disabled state and
		// and the previous range is stored in this value
		spawnRange = nbt.getShort("SpawnRange");

		if(spawnRange > 4)
		{
			disabled = true;
			cachedDisabled = disabled;

			// Set gui range to saved spawnRange value.
			requiredPlayerRange = spawnRange;
		}

		else {
			disabled = false;
			cachedDisabled = disabled;
		}

		// Load button configuration
		countOptionValue = loadOptionState(spawnCount, _spawnCount);
		speedOptionValue = loadOptionState(minSpawnDelay, _minSpawnDelay);
		rangeOptionValue = loadOptionState(requiredPlayerRange, _requiredPlayerRange);

		if(ConfigValues.get("limited_spawns_enabled") != 0) {
			limitedSpawns = true;
			if(nbt.contains("spawns")) {
				spawns = nbt.getShort("spawns");
				if(ConfigValues.get("limited_spawns_amount") - spawns == 0) {
					// Spawner has ran out of spawns. Disable.
					disabled = true;
				}
			}
		} else
			limitedSpawns = false;
	}


	/**
	 * 	Create all the GUI
	 */
	@Override
	public void init() {

		/**
		 * 	Count button
		 */
		countButton = addRenderableWidget(Button.builder(
				Component.translatable("button.count." + getButtonText(countOptionValue)), button -> {
					switch(countOptionValue) {
					// Low, set to Default
					case 0:
						countOptionValue = 1;
						spawnCount = _spawnCount.DEFAULT;
						maxNearbyEntities = _maxNearbyEntities.DEFAULT;
						break;

						// Default, set to High
					case 1:
						countOptionValue = 2;
						spawnCount = _spawnCount.HIGH;
						maxNearbyEntities = _maxNearbyEntities.HIGH;
						break;

						// High, set to Very High
					case 2:
						countOptionValue = 3;
						spawnCount = _spawnCount.HIGHEST;
						maxNearbyEntities = _maxNearbyEntities.HIGHEST;
						break;

						// Very high, set back to Low
					case 3:
						countOptionValue = 0;
						spawnCount = _spawnCount.LOW;
						maxNearbyEntities = _maxNearbyEntities.LOW;
						break;
					}
					countButton.setMessage(Component.translatable("button.count." + getButtonText(countOptionValue)));
				})
				.bounds(width / 2 - 48, 55, 108, 20)
				.build());


		/**
		 * 	Speed button
		 */
		speedButton = addRenderableWidget(Button.builder(
				Component.translatable("button.speed." + getButtonText(speedOptionValue)), button -> {
					switch(speedOptionValue) {
					// Slow, set to default
					case 0:
						speedOptionValue = 1;
						delay = _delay.DEFAULT;
						minSpawnDelay = _minSpawnDelay.DEFAULT;
						maxSpawnDelay = _maxSpawnDelay.DEFAULT;
						break;

						// Default, set to Fast
					case 1:
						speedOptionValue = 2;
						delay = _delay.HIGH;
						minSpawnDelay = _minSpawnDelay.HIGH;
						maxSpawnDelay = _maxSpawnDelay.HIGH;
						break;

						// High, set to Very Fast
					case 2:
						speedOptionValue = 3;
						delay = _delay.HIGHEST;
						minSpawnDelay = _minSpawnDelay.HIGHEST;
						maxSpawnDelay = _maxSpawnDelay.HIGHEST;
						break;

						// Very high, set back to Slow
					case 3:
						speedOptionValue = 0;
						delay = _delay.LOW;
						minSpawnDelay = _minSpawnDelay.LOW;
						maxSpawnDelay = _maxSpawnDelay.LOW;
						break;
					}
					speedButton.setMessage(Component.translatable("button.speed." + getButtonText(speedOptionValue)));
				})
				.bounds(width / 2 - 48, 80, 108, 20)
				.build());

		/**
		 * 	Range button
		 */
		rangeButton = addRenderableWidget(Button.builder(
				Component.translatable("button.range." + getButtonText(rangeOptionValue)).append(" " + requiredPlayerRange), button -> {
					switch(rangeOptionValue) {
					// Default, set to Far
					case 0:
						rangeOptionValue = 1;
						requiredPlayerRange = _requiredPlayerRange.DEFAULT;
						break;

						// Far, set to Very Far
					case 1:
						rangeOptionValue = 2;
						requiredPlayerRange = _requiredPlayerRange.HIGH;
						break;

						// Very Far, set to Extreme
					case 2:
						rangeOptionValue = 3;
						requiredPlayerRange = _requiredPlayerRange.HIGHEST;
						break;

						// Extreme, set back to Default or Custom
					case 3:
						if(isCustomRange) {
							rangeOptionValue = 4;
							requiredPlayerRange = customRange;
						} else {
							rangeOptionValue = 0;
							requiredPlayerRange = _requiredPlayerRange.LOW;
						}
						break;

						// Custom, set back to Default
					case 4:
						rangeOptionValue = 0;
						requiredPlayerRange = _requiredPlayerRange.LOW;
						break;
					}

					rangeButton.setMessage(Component.translatable("button.range." + getButtonText(rangeOptionValue)).append(" " + requiredPlayerRange));
				})
				.bounds(width / 2 - 48, 105, 108, 20)
				.build());

		/**
		 * 	Disable button
		 */
		disableButton = addRenderableWidget(Button.builder(
				Component.translatable("button.toggle." + getButtonText(disabled)), button -> {
					if(disabled) {
						// Set spawner to ON
						disabled = false;
						toggleButtons(true);
						switch(rangeOptionValue) {
						case 0:
							requiredPlayerRange = _requiredPlayerRange.LOW;
							break;
						case 1:
							requiredPlayerRange = _requiredPlayerRange.DEFAULT;
							break;
						case 2:
							requiredPlayerRange = _requiredPlayerRange.HIGH;
							break;
						case 3:
							requiredPlayerRange = _requiredPlayerRange.HIGHEST;
							break;
						}
					}
					else {
						// Set spawner OFF
						disabled = true;
						toggleButtons(false);
						requiredPlayerRange = 0;
					}

					disableButton.setMessage(Component.translatable("button.toggle." + getButtonText(disabled)));
				})
				.bounds(width / 2 - 48, 130, 108, 20)
				.build());

		/**
		 * 	Save button - configures spawner data
		 */
		addRenderableWidget(Button.builder(Component.translatable("button.save"), button -> {
			configureSpawner();
			this.close();
		}).bounds(width / 2 - 89, 180 + 10, 178, 20).build());

		/**
		 * 	Cancel button
		 */
		addRenderableWidget(Button.builder(Component.translatable("button.cancel"), button -> {
			this.close();
		}).bounds(width / 2 - 89, 180 + 35, 178, 20).build());


		if(disabled)
			toggleButtons(false);
		else
			toggleButtons(true);
	}
	
	/**
	 * 	Render GUI Texture
	 */
	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Draw black transparent background (just like when pressing escape)
		renderBackground(context);

		// Draw spawner screen texture
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, spawnerConfigTexture);
		context.blit(spawnerConfigTexture, width / 2 - imageWidth / 2, 5, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		
		// Render spawner title text
		int length = titleText.getString().length() * 2;
		context.drawString(minecraft.font, titleText, width / 2 - length - 3, 33, 0xFFD964);

		if(limitedSpawns) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, spawnsIconTexture);
			context.blit(spawnsIconTexture, width / 2 - 7 + 101, 23, 0, 0, 14, 14, 14, 14);
			context.drawString(minecraft.font, "" + (ConfigValues.get("limited_spawns_amount") - spawns), width / 2 + 114, 27, 0xFFFFFF);
			// drawString(matrixStack, minecraft.font, "not available yet", width / 2 + 114, 27, 0xFFFFFF);
		}

		super.render(context, mouseX, mouseY, partialTicks);
	}

	/**
	 * 	Close GUI
	 */
	private void close() {
		minecraft.setScreen((Screen)null);
	}

	/**
	 * 	Send message to server with the new NBT values.
	 */
	private void configureSpawner() {

		if(cachedDisabled)
			if(cachedDisabled == disabled)
				return;

		SpawnerModPacketHandler.INSTANCE.sendToServer(
				new SyncSpawnerMessage(
						pos, 
						delay, 
						spawnCount, 
						requiredPlayerRange, 
						maxNearbyEntities,
						minSpawnDelay, 
						maxSpawnDelay));
	}

	/**
	 * 	@returns a string with the last part of the translation key depending on the button
	 * 	option value.
	 */
	private String getButtonText(int optionValue) {
		switch(optionValue) {
		case 0:
			return "low";
		case 1:
			return "default";
		case 2:
			return "high";
		case 3:
			return "very_high";
		case 4:
			return "custom";
		default:
			return "default";
		}
	}
	private String getButtonText(boolean disabled) {
		if(disabled)
			return "disabled";
		else
			return "enabled";
	}

	/**
	 * 	Loads what type of configuration spawner has. So it can remember what we have changed 
	 * 
	 * 	@param current value which the spawner has when player just right clicked it
	 * 	@param reference reference to all the Low -> Highest data values
	 * 	@return optionValue current config spec
	 */
	private int loadOptionState(short current, Data reference)
	{
		if(isCustomRange && current == customRange)
			return 4;

		if(current == reference.LOW)
			return 0;
		else if(current == reference.DEFAULT)
			return 1;
		else if(current == reference.HIGH)
			return 2;
		else if(current == reference.HIGHEST)
			return 3;
		else
			return 0;
	}

	/**
	 * 	Toggles the count, speed and range button to specified state.
	 * 
	 * 	@param state True/False - On/Off
	 */
	private void toggleButtons(boolean state) {

		if(ConfigValues.get("disable_count") != 0) {
			countButton.active = false;
			countButton.setMessage(Component.translatable("button.count.disabled"));
		} else
			countButton.active = state;

		if(ConfigValues.get("disable_speed") != 0) {
			speedButton.active = false;
			speedButton.setMessage(Component.translatable("button.speed.disabled"));
		} else
			speedButton.active = state;

		if(ConfigValues.get("disable_range") != 0) {
			rangeButton.active = false;
			rangeButton.setMessage(Component.translatable("button.range.disabled"));
		}
		else
			rangeButton.active = state;

	}
}
