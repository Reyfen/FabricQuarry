package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.quarrymod.blockentity.machine.tier3.ExcavationState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.init.QuarryManagerContent;
import reborncore.common.blockentity.RedstoneConfiguration;

public class QuarryBlockGameTest {

    private static final BlockPos QUARRY_POS = new BlockPos(1, 2, 1);
    private static final long ENERGY = 100_000;

    @GameTest(maxTicks = 100)
    public void quarryPlacesAndTicks(GameTestHelper context) {
        placeQuarry(context);
        context.runAfterDelay(40, () -> {
            context.getBlockEntity(QUARRY_POS, QuarryBlockEntity.class);
            context.succeed();
        });
    }

    @GameTest(maxTicks = 100)
    public void quarryDropsUpgradesWhenBroken(GameTestHelper context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        Item upgrade = QuarryManagerContent.Upgrades.SILKTOUCH.item;
        quarry.quarryUpgradesInventory.setItem(0, new ItemStack(upgrade));

        context.setBlock(QUARRY_POS, Blocks.AIR);
        context.runAfterDelay(5, () -> {
            context.assertItemEntityPresent(upgrade, QUARRY_POS, 2.0);
            context.succeed();
        });
    }

    @GameTest(maxTicks = 200)
    public void quarryFollowsRedstoneControl(GameTestHelper context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        quarry.setStored(ENERGY);
        quarry.setRedstoneConfiguration(quarry.getRedstoneConfiguration()
            .withState(RedstoneConfiguration.Element.POWER_IO, RedstoneConfiguration.State.ENABLED_ON));

        context.runAfterDelay(40, () -> {
            context.assertTrue(quarry.getExcavationState() == ExcavationState.NoEnergyIncome,
                Component.literal("Quarry should be stopped without redstone, state " + quarry.getExcavationState()));
            context.assertTrue(quarry.getStored() == ENERGY,
                Component.literal("Quarry used energy without redstone: " + quarry.getStored()));

            context.setBlock(QUARRY_POS.east(), Blocks.REDSTONE_BLOCK);
            context.runAfterDelay(40, () -> {
                context.assertTrue(quarry.getStored() < ENERGY, Component.literal("Quarry did not work with redstone signal"));
                context.succeed();
            });
        });
    }

    @GameTest
    public void quarryScreenHandlerBuilds(GameTestHelper context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        Player player = context.makeMockServerPlayerInLevel();
        context.assertTrue(quarry.createScreenHandler(0, player) != null,
            Component.literal("Quarry screen handler was not created"));
        context.succeed();
    }

    private static QuarryBlockEntity placeQuarry(GameTestHelper context) {
        context.setBlock(QUARRY_POS, QuarryManagerContent.Machine.QUARRY.block.defaultBlockState());
        return context.getBlockEntity(QUARRY_POS, QuarryBlockEntity.class);
    }
}
