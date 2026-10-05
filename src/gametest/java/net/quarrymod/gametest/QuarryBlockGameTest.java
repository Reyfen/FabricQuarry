package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.quarrymod.blockentity.machine.tier3.ExcavationState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.init.QuarryManagerContent;
import reborncore.common.blockentity.RedstoneConfiguration;

public class QuarryBlockGameTest implements FabricGameTest {

    private static final BlockPos QUARRY_POS = new BlockPos(1, 2, 1);
    private static final long ENERGY = 100_000;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void quarryPlacesAndTicks(TestContext context) {
        placeQuarry(context);
        context.waitAndRun(40, () -> {
            context.assertTrue(context.getBlockEntity(QUARRY_POS) instanceof QuarryBlockEntity,
                "Quarry block entity missing after 40 ticks");
            context.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
    public void quarryFollowsRedstoneControl(TestContext context) {
        QuarryBlockEntity quarry = placeQuarry(context);
        quarry.setStored(ENERGY);
        quarry.setRedstoneConfiguration(quarry.getRedstoneConfiguration()
            .withState(RedstoneConfiguration.Element.POWER_IO, RedstoneConfiguration.State.ENABLED_ON));

        context.waitAndRun(40, () -> {
            context.assertTrue(quarry.getExcavationState() == ExcavationState.NoEnergyIncome,
                "Quarry should be stopped without redstone, state " + quarry.getExcavationState());
            context.assertTrue(quarry.getStored() == ENERGY,
                "Quarry used energy without redstone: " + quarry.getStored());

            context.setBlockState(QUARRY_POS.east(), Blocks.REDSTONE_BLOCK);
            context.waitAndRun(40, () -> {
                context.assertTrue(quarry.getStored() < ENERGY, "Quarry did not work with redstone signal");
                context.complete();
            });
        });
    }

    private static QuarryBlockEntity placeQuarry(TestContext context) {
        context.setBlockState(QUARRY_POS, QuarryManagerContent.Machine.QUARRY.block.getDefaultState());
        return (QuarryBlockEntity) context.getBlockEntity(QUARRY_POS);
    }
}
