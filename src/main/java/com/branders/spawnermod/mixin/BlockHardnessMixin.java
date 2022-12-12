package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.branders.spawnermod.config.ConfigValues;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(AbstractBlockState.class)
public class BlockHardnessMixin {

    @Inject(
            at = @At("HEAD"),
            method = "getHardness("
                    + "Lnet/minecraft/world/BlockView;"
                    + "Lnet/minecraft/util/math/BlockPos;"
                    + ")"
                    + "F",
                    cancellable = true)
    public void getHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        AbstractBlockState state = (AbstractBlockState)(Object)this;
        Block block = state.getBlock();
        if(block instanceof SpawnerBlock) {
            // Return custom hardness
            float hardness = ConfigValues.get("spawner_hardness");
            cir.setReturnValue(hardness);
        }
    }
}
