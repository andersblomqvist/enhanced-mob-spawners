package com.branders.spawnermod.event;

import com.branders.spawnermod.SpawnerMod;
import com.branders.spawnermod.config.ConfigValues;
import com.branders.spawnermod.item.SpawnerKey;
import com.branders.spawnermod.mixin.UpdateNeighborMixin;
import com.google.common.collect.Iterables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

/**
 * 	Handles events regarding the mob spawner.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class EventHandler {

    /**
     * 	Called when player breaks a block
     * 
     * 	If silk touch was used we want to drop the monster egg. Otherwise just exp.
     * 	The spawner block is dropped via loot_table json
     */
    public boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

        // Leave if client
        if(world.isClient)
            return true;

        // No need to drop if in creative mode
        if(player.isCreative())
            return true;

        // Make sure it was a spawner block
        if(world.getBlockState(pos).getBlock() instanceof SpawnerBlock) {

            ItemStack item = Iterables.get(player.getHandItems(), 0);
            NbtList enchants = item.getEnchantments();

            if(checkSilkTouch(enchants) && ConfigValues.get("disable_silk_touch") == 0) {
                // Drop egg inside if not disabled from config
                if(ConfigValues.get("disable_egg_removal_from_spawner") == 0)
                    dropMonsterEgg(pos, world);
            } else {
                int size = 15 + world.random.nextInt(15) + world.random.nextInt(15);
                ExperienceOrbEntity.spawn((ServerWorld) world, Vec3d.ofCenter(pos), size);
            }
        }

        return true;
    }

    /**
     * 	Called when player right-clicks a block.
     * 
     * 	Used to retrieve egg from Spawner.
     */
    public ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

        if(world.isClient)
            return ActionResult.PASS;

        if(world.getBlockState(hitResult.getBlockPos()).getBlock() != Blocks.SPAWNER)
            return ActionResult.PASS;

        if(hand == Hand.OFF_HAND)
            return ActionResult.PASS;

        if(ConfigValues.get("disable_egg_removal_from_spawner") != 0)
            return ActionResult.PASS;

        Item item = player.getMainHandStack().getItem();

        if(item instanceof BlockItem || item instanceof SpawnEggItem || item instanceof SpawnerKey)
            return ActionResult.PASS;

        return dropMonsterEgg(hitResult.getBlockPos(), world);
    }

    /**
     * 	Drops the Monster Egg which is inside the spawner when the spawner is harvested.
     * 	This is only server side.
     * 
     * 	@param pos Spawner block position
     * 	@param world World reference for spawning
     */
    private ActionResult dropMonsterEgg(BlockPos pos, World world) {

        BlockState blockstate = world.getBlockState(pos);
        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)world.getBlockEntity(pos);
        MobSpawnerLogic logic = spawner.getLogic();

        // Get entity ResourceLocation string from spawner by creating a empty compound which we make our 
        // spawner logic write to. We can then access what type of entity id the spawner has inside
        NbtCompound nbt = new NbtCompound();
        nbt = logic.writeNbt(nbt);
        NbtElement spawnData = nbt.get(MobSpawnerLogic.SPAWN_DATA_KEY);
        if(spawnData == null)
            return ActionResult.PASS;
        String entity_string = spawnData.asString();

        // Leave if the spawner does not contain an entity
        if(entity_string.indexOf("\"") == -1)
            return ActionResult.PASS;

        // Strips the string
        // Example: {{id:"minecraft:xxx_xx"}} --> minecraft:xxx_xx
        entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
        entity_string = entity_string.substring(0, entity_string.indexOf("\""));

        // Just in case
        if(entity_string.contains("area_effect_cloud"))
            return ActionResult.PASS;

        // Get the entity mob egg and put in an ItemStack
        Item egg = Registries.ITEM.get(new Identifier(entity_string + "_spawn_egg"));
        if(egg == null) {
            SpawnerMod.LOGGER.info("Identifier: " + entity_string + "_spawn_egg" + " could not be found in registries.");
            return ActionResult.PASS;
        }
        ItemStack itemStack = new ItemStack(egg);

        // Get random fly-out position offsets
        double d0 = (double)(world.getRandom().nextFloat() * 0.7F) + (double)0.15F;
        double d1 = (double)(world.getRandom().nextFloat() * 0.7F) + (double)0.06F + 0.6D;
        double d2 = (double)(world.getRandom().nextFloat() * 0.7F) + (double)0.15F;

        // Create entity item
        ItemEntity entityItem = new ItemEntity(
                world, 
                (double)pos.getX() + d0, 
                (double)pos.getY() + d1, 
                (double)pos.getZ() + d2, 
                itemStack);
        entityItem.setToDefaultPickupDelay();

        // Spawn entity item (egg)
        world.spawnEntity(entityItem);

        // Replace the entity inside the spawner with default entity
        logic.setEntityId(EntityType.AREA_EFFECT_CLOUD, world, world.random, pos);
        spawner.markDirty();
        world.updateListeners(pos, blockstate, blockstate, 3);

        return ActionResult.SUCCESS;
    }

    /**	
     * 	Check for redstone update. If block gets powered we want to turn it off (set range to 0).
     * 	We store previous range: 16, 32, 64 or 128, so we can set it back when spawner regain power.
     * 
     * 	<br><br>
     * 	Called from {@link UpdateNeighborMixin}
     * 	 
     * 	@param spawnerwPos
     * 	@param world
     */
    public static void updateNeighbor(BlockPos spawnerPos, World world) {

        BlockState blockstate = world.getBlockState(spawnerPos);

        if(!(world.getBlockEntity(spawnerPos) instanceof MobSpawnerBlockEntity))
            return;

        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)world.getBlockEntity(spawnerPos);
        MobSpawnerLogic logic = spawner.getLogic();
        NbtCompound nbt = new NbtCompound();

        nbt = logic.writeNbt(nbt);

        if(world.isReceivingRedstonePower(spawnerPos)) {
            short value = nbt.getShort("RequiredPlayerRange");

            // If spawner got disabled via GUI and then we toggle off by redstone
            // we don't need to do this.
            if(nbt.getShort("SpawnRange") > 4)
                return;

            // Read current range and save it temporary in SpawnRange field
            nbt.putShort("SpawnRange", value);

            // Turn off spawner
            nbt.putShort("RequiredPlayerRange", (short) 0);
        }

        else {
            // Read what the previus range was (before this spawner was set to range = 0)
            short pr = nbt.getShort("SpawnRange");

            // If spawner was activated via GUI before, then we dont need to do this
            if(pr <= 4)
                return;

            // Set the range backt to what it was
            nbt.putShort("RequiredPlayerRange", pr);

            // Set SpawnRange back to default=4
            nbt.putShort("SpawnRange", (short) 4);
        }

        // Update block
        logic.readNbt(world, spawnerPos, nbt);
        spawner.markDirty();
        world.updateListeners(spawnerPos, blockstate, blockstate, Block.NOTIFY_ALL);
    }

    /**
     * 	Check a tools item enchantment list contains Silk Touch enchant
     * 
     * 	@param ListTag of enchantment
     * 	@returns
     * 		true if tool has Silk Touch
     * 		false otherwise
     */
    private boolean checkSilkTouch(NbtList  list) {
        if(list.asString().contains("silk_touch"))
            return true;
        else
            return false;
    }
}
