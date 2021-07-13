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
	public boolean isFoil(ItemStack stack) {
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
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		// Leave if disabled in config.
		if(SpawnerModConfig.GENERAL.disable_spawner_config.get())
			return ActionResultType.PASS;
		
		World world = context.getLevel();
		
		// Leave if we are server
		if(!world.isClientSide)
			return ActionResultType.PASS;
		
		// Leave if we didn't right click a spawner
		BlockPos blockpos = context.getClickedPos();
		if(world.getBlockState(blockpos).getBlock() != Blocks.SPAWNER)
			return ActionResultType.PASS;
		
		// Open GUI
		MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)world.getBlockEntity(blockpos);
    	AbstractSpawner logic = spawner.getSpawner();
    	openSpawnerGui(logic, blockpos);
		
		return super.onItemUseFirst(stack, context);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(SpawnerModConfig.GENERAL.disable_spawner_config.get()) {
			tooltip.add(textComponent.withStyle(TextFormatting.RED));
			super.appendHoverText(stack, worldIn, tooltip, flagIn);	
		}
	}
	
	/**
     * 	Opens GUI for configuration of the spawner. Only on client
     */
    @OnlyIn(Dist.CLIENT)
    private void openSpawnerGui(AbstractSpawner logic, BlockPos pos)
    {
    	Minecraft mc = Minecraft.getInstance();
    	mc.setScreen(new SpawnerConfigGui(new TranslationTextComponent(""), logic, pos));
    }
}
