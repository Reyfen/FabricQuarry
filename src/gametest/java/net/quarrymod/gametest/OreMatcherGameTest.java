package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.quarrymod.blockentity.machine.tier3.OreMatcher;

public class OreMatcherGameTest {

    @GameTest
    public void vanillaOres(GameTestHelper context) {
        expectOre(context, Blocks.COAL_ORE, true);
        expectOre(context, Blocks.DEEPSLATE_DIAMOND_ORE, true);
        expectOre(context, Blocks.NETHER_GOLD_ORE, true);
        expectOre(context, Blocks.NETHER_QUARTZ_ORE, true);
        expectOre(context, Blocks.ANCIENT_DEBRIS, true);
        expectOre(context, Blocks.STONE, false);
        expectOre(context, Blocks.DEEPSLATE, false);
        expectOre(context, Blocks.DIRT, false);
        context.succeed();
    }

    @GameTest
    public void modOres(GameTestHelper context) {
        expectOre(context, QuarryGameTestBlocks.TAGGED_ROCK, true);
        expectOre(context, QuarryGameTestBlocks.DENIED_ORE, false);
        expectOre(context, QuarryGameTestBlocks.ORE_SHINY, true);
        expectOre(context, QuarryGameTestBlocks.ORE_MACHINE, false);
        expectOre(context, QuarryGameTestBlocks.PLAIN_ROCK, false);
        context.succeed();
    }

    @GameTest
    public void techRebornOres(GameTestHelper context) {
        for (String name : new String[]{"tin_ore", "deepslate_iridium_ore", "pyrite_ore", "sheldonite_ore"}) {
            Identifier id = Identifier.fromNamespaceAndPath("techreborn", name);
            context.assertTrue(BuiltInRegistries.BLOCK.containsKey(id), Component.literal("Missing block " + id));
            expectOre(context, BuiltInRegistries.BLOCK.getValue(id), true);
        }
        context.succeed();
    }

    private static void expectOre(GameTestHelper context, Block block, boolean expected) {
        boolean actual = OreMatcher.isOre(block.defaultBlockState());
        context.assertTrue(actual == expected,
            Component.literal(BuiltInRegistries.BLOCK.getKey(block) + " should " + (expected ? "" : "not ") + "be an ore"));
    }
}
