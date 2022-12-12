package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.event.EventHandler;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * 	Redstone event for Spawner.
 * 
 * 	Found from following PistonBlock: {@code neighborUpdate()} function.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mixin(AbstractBlock.class)
public class UpdateNeighborMixin {

    @Inject(
            at = @At("HEAD"),
            method = "neighborUpdate("
                    + "Lnet/minecraft/block/BlockState;"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/block/Block;"
                    + "Lnet/minecraft/util/math/BlockPos;"
                    + "Z"
                    + ")V")
    private void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for(Direction dir : Direction.values()) {
            mutable.set(sourcePos, dir);
            if(world.getBlockState(mutable).getBlock() instanceof SpawnerBlock) {
                EventHandler.updateNeighbor(mutable, (World)world);
            }
        }
    }
}
