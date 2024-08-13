package com.branders.spawnermod.item;

import java.util.List;

import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.gui.SpawnerConfigGui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnerKey extends Item {

    private static final Text TOOL_TIP = Text.translatable("tooltip.spawnermod.spawner_key_disabled")
            .setStyle(Style.EMPTY.withColor(0xff0000));

    public SpawnerKey(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (ConfigValues.get("disable_spawner_config") != 0) {
            tooltip.add(TOOL_TIP);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (ConfigValues.get("disable_spawner_config") != 0)
            return ActionResult.FAIL;

        World world = context.getWorld();

        // Leave if we are server
        if (!world.isClient)
            return ActionResult.FAIL;

        // Leave if we didn't right click a spawner
        BlockPos pos = context.getBlockPos();
        if (world.getBlockState(pos).getBlock() != Blocks.SPAWNER)
            return ActionResult.FAIL;

        // Open GUI
        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(pos);
        MobSpawnerLogic logic = spawner.getLogic();
        openSpawnerGui(logic, pos);

        return super.useOnBlock(context);
    }

    @Environment(EnvType.CLIENT)
    private void openSpawnerGui(MobSpawnerLogic logic, BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(new SpawnerConfigGui(Text.translatable(""), logic, pos));
    }
}
