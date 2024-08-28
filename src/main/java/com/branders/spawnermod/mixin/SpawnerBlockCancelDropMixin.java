package com.branders.spawnermod.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Cancel the Silk Touch spawner drop
 */
@Mixin(Block.class)
public class SpawnerBlockCancelDropMixin {

    @Inject(at = @At("HEAD"), method = "afterBreak(" + "Lnet/minecraft/world/World;"
            + "Lnet/minecraft/entity/player/PlayerEntity;" + "Lnet/minecraft/util/math/BlockPos;"
            + "Lnet/minecraft/block/BlockState;" + "Lnet/minecraft/block/entity/BlockEntity;"
            + "Lnet/minecraft/item/ItemStack;)" + "V", cancellable = true)
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo info) {
        if (state.getBlock() instanceof SpawnerBlock) {
            if (ConfigValues.get("disable_silk_touch") == 1)
                info.cancel();
        }
    }
}
