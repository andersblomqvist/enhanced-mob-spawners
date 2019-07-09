package com.branders.spawnermod.item;

import com.branders.spawnermod.gui.SpawnerConfigGui;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnerKeyItem extends Item
{
	public SpawnerKeyItem(Properties properties) 
	{
		super(properties);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) 
	{
		return true;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) 
	{
		return 1;
	}
	
	/**
	 * 	Replacement for PlayerInteractEvent.RightClickBlock in SpawnerEventHandler due to not fired on client side. 
	 */
	@Override
	public ActionResultType onItemUse(ItemUseContext context) 
	{
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
	
	/**
     * 	Opens GUI for configuration of the spawner. Only on client
     */
    @OnlyIn(Dist.CLIENT)
   	private void openSpawnerGui(AbstractSpawner logic, BlockPos pos)
    {
    	Minecraft mc = Minecraft.getInstance();
    	mc.displayGuiScreen(new SpawnerConfigGui(new TranslationTextComponent("why do I need this?"), logic, pos));
    }
}
