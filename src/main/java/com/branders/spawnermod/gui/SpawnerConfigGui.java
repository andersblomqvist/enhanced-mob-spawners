package com.branders.spawnermod.gui;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;

/**
 * 	Spawner GUI config screen. Renders the background and all the buttons.
 * 
 * 	It communicates with the spawner object by sending a network package with
 * 	data from the GUI elements. 
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Environment(EnvType.CLIENT)
public class SpawnerConfigGui extends Screen {
	
	// "Spawner Config" title in yellow at the top
	private static final Text titleText = Text.translatable("gui.spawnermod.spawner_config_screen_title");
	
	// Reference to spawner logic and NBT data. Set in constructor
	private MobSpawnerLogic logic;
	private BlockPos pos;
	private NbtCompound nbt = new NbtCompound();
	
	// GUI textures
	private Identifier spawnerConfigTexture = new Identifier(SpawnerMod.MOD_ID, "textures/gui/spawner_config_screen.png");
	private int imageWidth = 178;
	private int imageHeight = 177;
	private Identifier spawnsIconTexture = new Identifier(SpawnerMod.MOD_ID, "textures/gui/spawner_config_screen_icon_spawns.png");
	
	// Buttons for controlling Spawner data
	private ButtonWidget countButton = null;
	private ButtonWidget speedButton = null;
	private ButtonWidget rangeButton = null;
	private ButtonWidget disableButton = null;
	
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
	private Data _delay 			  = new Data(30, 20, 10, 5);
	private Data _minSpawnDelay 	  = new Data(300, 200, 100, 50);
	private Data _maxSpawnDelay 	  = new Data(900, 800, 400, 100);
	private Data _spawnCount 		  = new Data(2, 4, 6, 12);
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
	
	/**
	 * 	When creating this GUI a reference to the Mob Spawner logic and BlockPos is required so we can read
	 * 	current NBT values (used to make GUI remember option states) and send network package to server with
	 * 	a reference to the spawner block position.
	 */
	public SpawnerConfigGui(Text title, MobSpawnerLogic logic, BlockPos pos) {
		
		super(title);
		
		this.logic = logic;
		this.pos = pos;
    	
    	// Read values for Spawner to check what type of configuration it has so we can render
    	// correct button display strings. We have to read all the values in case the player
    	// doesn't change anything and presses save button.
		nbt = this.logic.writeNbt(nbt);
    	delay = nbt.getShort("Delay");
    	minSpawnDelay = nbt.getShort("MinSpawnDelay");
    	maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
    	spawnCount = nbt.getShort("SpawnCount");
    	maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
    	requiredPlayerRange = nbt.getShort("RequiredPlayerRange");
    	
    	// If spawn range differs from 4, spawner is in the disabled state and
    	// and the previous range is stored in this value
    	spawnRange = nbt.getShort("SpawnRange");
    	
    	if(spawnRange > 4) {
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
	
	@Override
	protected void init() {
		
		/**
		 * 	Count button
		 */
		addDrawableChild(countButton = new ButtonWidget(
				width / 2 - 48, 55, 108, 20, Text.translatable(
						"button.count." + getButtonText(countOptionValue)), button -> {
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
			
			countButton.setMessage(Text.translatable("button.count." + getButtonText(countOptionValue)));
		}));
		
		
		/**
		 * 	Speed button
		 */
		addDrawableChild(speedButton = new ButtonWidget(
				width / 2 - 48, 80, 108, 20, Text.translatable(
						"button.speed." + getButtonText(speedOptionValue)), button -> {
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
			speedButton.setMessage(Text.translatable("button.speed." + getButtonText(speedOptionValue)));
		}));
		
		/**
		 * 	Range button
		 */
		addDrawableChild(rangeButton = new ButtonWidget(
				width / 2 - 48, 105, 108, 20, Text.translatable(
						"button.range." + getButtonText(rangeOptionValue)).append(" " + requiredPlayerRange), button -> {
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
					
				// Extreme, set back to Default
				case 3:
					rangeOptionValue = 0;
					requiredPlayerRange = _requiredPlayerRange.LOW;
					break;
			}
			
			rangeButton.setMessage(Text.translatable("button.range." + getButtonText(rangeOptionValue)).append(" " + requiredPlayerRange));
		}));
		
		/**
		 * 	Disable button
		 */
		addDrawableChild(disableButton = new ButtonWidget(
				width / 2 - 48, 130, 108, 20, Text.translatable(
						"button.toggle." + getButtonText(disabled)), button -> {
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
			
			disableButton.setMessage(Text.translatable("button.toggle." + getButtonText(disabled)));
		}));
		
		/**
		 * 	Save button - configures spawner data
		 */
		addDrawableChild(new ButtonWidget(width / 2 - 89, 180 + 10, 178, 20, Text.translatable("button.save"), button -> 
		{
			configureSpawner();
			this.close();
		}));
		
		/**
		 * 	Cancel button
		 */
		addDrawableChild(new ButtonWidget(width / 2 - 89, 180 + 35, 178, 20, Text.translatable("button.cancel"), button -> 
		{
			this.close();
		}));
		
		if(disabled)
			toggleButtons(false);
		else
			toggleButtons(true);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		// Draw black transparent background (just like when pressing escape)
		renderBackground(matrices);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, spawnerConfigTexture);
		drawTexture(matrices, width / 2 - imageWidth / 2, 5, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		
		// Render spawner title text
		int length = titleText.getString().length() * 2;
		drawTextWithShadow(matrices, client.textRenderer, titleText, width / 2 - length - 3, 33, 0xFFD964);
		
		// Render spawns icon and text (only if enabled in config)
		if(limitedSpawns) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, spawnsIconTexture);
			drawTexture(matrices, width / 2 - 7 + 101, 23, 0, 0, 14, 14, 14, 14);
			drawTextWithShadow(matrices, client.textRenderer, Text.literal("" + (ConfigValues.get("limited_spawns_amount") - spawns)), width / 2 + 114, 27, 0xFFFFFF);
		}
		
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	/**
     * 	Send message to server with the new NBT values.
     */
    private void configureSpawner() {
    	
    	if(cachedDisabled)
    		if(cachedDisabled == disabled)
    			return;
    	
    	new SyncSpawnerMessage(
    					pos, 
    					delay, 
    					spawnCount, 
    					requiredPlayerRange, 
    					maxNearbyEntities,
    					minSpawnDelay, 
    					maxSpawnDelay).send();
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
	 * 	@param current: value which the spawner has when player just right clicked it
	 * 	@param reference: reference to all the Low -> Highest data values
	 * 	@return optionValue: current config spec
	 */
	private int loadOptionState(short current, Data reference)
	{
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
	 * 	Toggles the count, speed and range button to specified state. However take the config
	 * 	into consideration first. 
	 * 
	 * 	@param state True/False - On/Off
	 */
	private void toggleButtons(boolean state) {
		
		if(ConfigValues.get("disable_count") != 0) {
			countButton.active = false;
			countButton.setMessage(Text.translatable("button.count.disabled"));
		} else
			countButton.active = state;
		
		if(ConfigValues.get("disable_speed") != 0) {
			speedButton.active = false;
			speedButton.setMessage(Text.translatable("button.speed.disabled"));
		} else
			speedButton.active = state;
		
		if(ConfigValues.get("disable_range") != 0) {
			rangeButton.active = false;
			rangeButton.setMessage(Text.translatable("button.range.disabled"));
		} else 
			rangeButton.active = state;
	}
}
