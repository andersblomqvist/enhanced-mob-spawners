package com.branders.spawnermod.item;

import java.util.List;

import com.branders.spawnermod.config.SpawnerModConfig;
import com.branders.spawnermod.gui.SpawnerConfigGui;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnerKeyItem extends Item
{
	private static final TranslationTextComponent textComponent = 
			new TranslationTextComponent("tooltip.spawnermod.spawner_key_disabled");
	
	public SpawnerKeyItem(Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}
	
	/**
	 * 	Replacement for PlayerInteractEvent.RightClickBlock in SpawnerEventHandler due to not fired on client side. 
	 */
	@Override
	public ActionResultType onItemUse(ItemUseContext context) 
	{
		// Leave if disabled in config.
		if(SpawnerModConfig.GENERAL.disable_spawner_config.get())
			return ActionResultType.FAIL;
		
		World world = context.getWorld();
		
		// Leave if we are server
		if(!world.isRemote)
			return ActionResultType.FAIL;
		
		// Leave if we didn't right click a spawner
		BlockPos blockpos = context.getPos();
		if(world.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return ActionResultType.FAIL;
		
		// Open GUI
		MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getTileEntity(blockpos);
    	AbstractSpawner logic = spawner.getSpawnerBaseLogic();
    	openSpawnerGui(logic, blockpos);
		
		return super.onItemUse(context);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(SpawnerModConfig.GENERAL.disable_spawner_config.get()) {
			tooltip.add(textComponent.applyTextStyle(TextFormatting.RED));
			super.addInformation(stack, worldIn, tooltip, flagIn);	
		}
	}
	
	/**
     * 	Opens GUI for configuration of the spawner. Only on client
     */
    @OnlyIn(Dist.CLIENT)
    private void openSpawnerGui(AbstractSpawner logic, BlockPos pos)
    {
    	Minecraft mc = Minecraft.getInstance();
    	mc.displayGuiScreen(new SpawnerConfigGui(new TranslationTextComponent(""), logic, pos));
    }
}
