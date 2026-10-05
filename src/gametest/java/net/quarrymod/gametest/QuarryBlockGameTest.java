package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.quarrymod.blockentity.machine.tier3.ExcavationState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.init.QuarryManagerContent;
import reborncore.common.blockentity.RedstoneConfiguration;

public class QuarryBlockGameTest {

    private static final BlockPos QUARRY_POS = new BlockPos(1, 2, 1);
    private static final long ENERGY = 100_000;

    @GameTest(maxTicks = 100)
    public void quarryPlacesAndTicks(TestContext context) {
        placeQuarry(context);
        context.waitAndRun(40, () -> {
            context.getBlockEntity(QUARRY_POS, QuarryBlockEntity.class);
            context.complete();
        });
    }

    @GameTest(maxTicks = 100)
    public void quarryDropsUpgradesWhenBroken(TestContext context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        Item upgrade = QuarryManagerContent.Upgrades.SILKTOUCH.item;
        quarry.quarryUpgradesInventory.setStack(0, new ItemStack(upgrade));

        context.setBlockState(QUARRY_POS, Blocks.AIR);
        context.waitAndRun(5, () -> {
            context.expectItemAt(upgrade, QUARRY_POS, 2.0);
            context.complete();
        });
    }

    @GameTest(maxTicks = 200)
    public void quarryFollowsRedstoneControl(TestContext context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        quarry.setStored(ENERGY);
        quarry.setRedstoneConfiguration(quarry.getRedstoneConfiguration()
            .withState(RedstoneConfiguration.Element.POWER_IO, RedstoneConfiguration.State.ENABLED_ON));

        context.waitAndRun(40, () -> {
            context.assertTrue(quarry.getExcavationState() == ExcavationState.NoEnergyIncome,
                Text.literal("Quarry should be stopped without redstone, state " + quarry.getExcavationState()));
            context.assertTrue(quarry.getStored() == ENERGY,
                Text.literal("Quarry used energy without redstone: " + quarry.getStored()));

            context.setBlockState(QUARRY_POS.east(), Blocks.REDSTONE_BLOCK);
            context.waitAndRun(40, () -> {
                context.assertTrue(quarry.getStored() < ENERGY, Text.literal("Quarry did not work with redstone signal"));
                context.complete();
            });
        });
    }

    private static QuarryBlockEntity placeQuarry(TestContext context) {
        context.setBlockState(QUARRY_POS, QuarryManagerContent.Machine.QUARRY.block.getDefaultState());
        return context.getBlockEntity(QUARRY_POS, QuarryBlockEntity.class);
    }
}
