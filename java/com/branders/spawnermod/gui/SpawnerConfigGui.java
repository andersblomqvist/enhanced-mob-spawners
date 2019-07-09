package com.branders.spawnermod.gui;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.networking.SpawnerModPacketHandler;
import com.branders.spawnermod.networking.packet.SyncSpawnerMessage;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 	@author Anders <Branders> Blomqvist
 */
@OnlyIn(Dist.CLIENT)
public class SpawnerConfigGui extends Screen 
{
	// References to Spawner Logic and NBT Data. Set in constructor
	private AbstractSpawner logic;
	private BlockPos pos;
	private CompoundNBT nbt = new CompoundNBT();	
	
	// GUI Texture
	private ResourceLocation spawnerConfigTexture = new ResourceLocation(SpawnerMod.MODID, "/textures/gui/spawner_config_screen.png");
	private int imageWidth = 178;
	private int imageHeight = 177;
	
	// Buttons for controlling Spawner data
	private Button countButton = null;
	private Button speedButton = null;
	private Button rangeButton = null;
	
	// Button States
	private int countOptionValue;
	private int speedOptionValue;
	private int rangeOptionValue;
	
	// What the button will display depending on option value
	String[] speedDisplayString = {"Slow", "Default", "Fast", "Very Fast"};
	String[] countDisplayString = {"Low", "Default", "High", "Very High"};
	String[] rangeDisplayString = {"Default", "Far", "Very Far", "Extreme"};
	
	/**
	 * 	Object to hold values for all NBT parameters we modify.
	 * 	Each parameter holds 4 different values: Low - Default - High - Highest. These are used to get
	 * 	different values depending on what type of option the player decides to use.
	 */
	private class Data
	{
		short LOW, DEFAULT, HIGH, HIGHEST;
		
		public Data(int i, int j, int k, int l)
		{
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
	
	/**
	 * 	When creating this GUI a reference to the Mob Spawner logic and BlockPos is required so we can read
	 * 	current NBT values (used to make GUI remember option states) and send network package to server with
	 * 	a reference to the spawner block position.
	 */
	public SpawnerConfigGui(ITextComponent textComponent, AbstractSpawner logic, BlockPos pos) 
	{
		super(textComponent);
		
		this.logic = logic;
		this.pos = pos;
    	
    	// Read values for Spawner to check what type of configuration it has so we can render
    	// correct button display strings. We have to read all the values in case the player
    	// doesn't change anything and presses save button.
		nbt = this.logic.write(nbt);
    	delay = nbt.getShort("Delay");
    	minSpawnDelay = nbt.getShort("MinSpawnDelay");
    	maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
    	spawnCount = nbt.getShort("SpawnCount");
    	maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
    	requiredPlayerRange = nbt.getShort("RequiredPlayerRange");
    	
    	// Load button configuration
    	countOptionValue = loadOptionState(spawnCount, _spawnCount);
    	speedOptionValue = loadOptionState(minSpawnDelay, _minSpawnDelay);
    	rangeOptionValue = loadOptionState(requiredPlayerRange, _requiredPlayerRange);
	}
	
	
	/**
	 * 	Create all the GUI
	 */
	@Override
	public void init() 
	{
		/**
		 * 	Count button
		 */
		addButton(countButton = new Button(width / 2 - 48, 65, 96, 20, "Count: " + countDisplayString[countOptionValue], button -> 
		{
			switch(countOptionValue)
			{
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
			
			countButton.setMessage("Count: " + countDisplayString[countOptionValue]);
		}));
		
		
		/**
		 * 	Speed button
		 */
		addButton(speedButton = new Button(width / 2 - 48, 90, 96, 20, "Speed: " + speedDisplayString[speedOptionValue], button -> 
		{
			switch(speedOptionValue)
			{
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
			speedButton.setMessage("Speed: " + speedDisplayString[speedOptionValue]);
		}));
		
		/**
		 * 	Range button
		 */
		addButton(rangeButton = new Button(width / 2 - 48, 115, 96, 20, "Range: " + rangeDisplayString[rangeOptionValue], button -> 
		{
			switch(rangeOptionValue)
			{
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
			
			rangeButton.setMessage("Range: " + rangeDisplayString[rangeOptionValue]);
		}));
		
		/**
		 * 	Save button - configures spawner data
		 */
		addButton(new Button(width / 2 - 89, 180 + 10, 178, 20, "Save", button -> 
		{
			configureSpawner();
			onClose();
		}));
		
		/**
		 * 	Cancel button
		 */
		addButton(new Button(width / 2 - 89, 180 + 35, 178, 20, "Cancel", button -> 
		{
			onClose();
		}));
	}
	
	
	/**
	 * 	Render GUI Texture
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) 
	{
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		// Draw black transparent background (just like when pressing escape)
		renderBackground();
		
		// Draw spawner screen texture
		minecraft.getTextureManager().bindTexture(spawnerConfigTexture);
		blit(width / 2 - imageWidth / 2, 5, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		
		// Render other stuff as well (buttons)
		super.render(mouseX, mouseY, partialTicks);
	}
	
	
	/**
     * 	Send message to server with the new NBT values.
     */
    private void configureSpawner()
    {	
    	SpawnerModPacketHandler.INSTANCE.sendToServer(new SyncSpawnerMessage(pos, delay, spawnCount, requiredPlayerRange, maxNearbyEntities, minSpawnDelay, maxSpawnDelay));
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
}
