package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.quarrymod.blockentity.machine.tier3.OreMatcher;

public class OreMatcherGameTest {

    @GameTest
    public void vanillaOres(TestContext context) {
        expectOre(context, Blocks.COAL_ORE, true);
        expectOre(context, Blocks.DEEPSLATE_DIAMOND_ORE, true);
        expectOre(context, Blocks.NETHER_GOLD_ORE, true);
        expectOre(context, Blocks.NETHER_QUARTZ_ORE, true);
        expectOre(context, Blocks.ANCIENT_DEBRIS, true);
        expectOre(context, Blocks.STONE, false);
        expectOre(context, Blocks.DEEPSLATE, false);
        expectOre(context, Blocks.DIRT, false);
        context.complete();
    }

    @GameTest
    public void modOres(TestContext context) {
        expectOre(context, QuarryGameTestBlocks.TAGGED_ROCK, true);
        expectOre(context, QuarryGameTestBlocks.DENIED_ORE, false);
        expectOre(context, QuarryGameTestBlocks.ORE_SHINY, true);
        expectOre(context, QuarryGameTestBlocks.ORE_MACHINE, false);
        expectOre(context, QuarryGameTestBlocks.PLAIN_ROCK, false);
        context.complete();
    }

    @GameTest
    public void techRebornOres(TestContext context) {
        for (String name : new String[]{"tin_ore", "deepslate_iridium_ore", "pyrite_ore", "sheldonite_ore"}) {
            Identifier id = Identifier.of("techreborn", name);
            context.assertTrue(Registries.BLOCK.containsId(id), Text.literal("Missing block " + id));
            expectOre(context, Registries.BLOCK.get(id), true);
        }
        context.complete();
    }

    private static void expectOre(TestContext context, Block block, boolean expected) {
        boolean actual = OreMatcher.isOre(block.getDefaultState());
        context.assertTrue(actual == expected,
            Text.literal(Registries.BLOCK.getId(block) + " should " + (expected ? "" : "not ") + "be an ore"));
    }
}
