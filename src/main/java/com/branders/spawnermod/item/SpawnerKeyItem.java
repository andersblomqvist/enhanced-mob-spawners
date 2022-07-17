package com.branders.spawnermod.item;

import java.util.List;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.gui.SpawnerConfigGui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnerKeyItem extends Item {
	private static final Component TOOL_TIP = Component.translatable("tooltip.spawnermod.spawner_key_disabled").withStyle(ChatFormatting.RED);
	
	public SpawnerKeyItem(Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 1;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		// Leave if disabled in config.
		if(ConfigValues.get("disable_spawner_config") == 1)
			return InteractionResult.PASS;
		
		Level level = context.getLevel();
		
		// Leave if we are server
		if(!level.isClientSide)
			return InteractionResult.PASS;
		
		// Leave if we didn't right click a spawner
		BlockPos blockpos = context.getClickedPos();
		if(level.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return InteractionResult.PASS;
		
		// Open GUI
		SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(blockpos);
    	BaseSpawner logic = spawner.getSpawner();
    	openSpawnerGui(logic, blockpos);
		
		return super.useOn(context);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
		if(ConfigValues.get("disable_spawner_config") == 1) {
			tooltip.add(TOOL_TIP);
			super.appendHoverText(stack, level, tooltip, flagIn);	
		}
	}
	
	/**
     * 	Opens GUI for configuration of the spawner. Only on client
     */
    @OnlyIn(Dist.CLIENT)
    private void openSpawnerGui(BaseSpawner logic, BlockPos pos)
    {
    	Minecraft mc = Minecraft.getInstance();
    	mc.setScreen(new SpawnerConfigGui(Component.translatable(""), logic, pos));
    }
}
