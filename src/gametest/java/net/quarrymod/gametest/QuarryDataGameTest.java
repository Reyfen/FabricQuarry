package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.quarrymod.QuarryMod;
import net.quarrymod.init.QuarryManagerContent;

public class QuarryDataGameTest {

    private static final int RECIPE_COUNT = 9;

    @GameTest
    public void recipesLoad(GameTestHelper context) {
        long loaded = context.getLevel().getServer().getRecipeManager().getRecipes().stream()
            .filter(entry -> entry.id().identifier().getNamespace().equals(QuarryMod.MOD_ID))
            .count();
        context.assertTrue(loaded == RECIPE_COUNT, Component.literal("Loaded " + loaded + " of " + RECIPE_COUNT + " recipes"));
        context.succeed();
    }

    @GameTest
    public void blocksUseModNamespace(GameTestHelper context) {
        for (Block block : new Block[]{QuarryManagerContent.Machine.QUARRY.block, QuarryManagerContent.DRILL_TUBE}) {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            String translationKey = "block." + QuarryMod.MOD_ID + "." + path;
            context.assertTrue(block.getDescriptionId().equals(translationKey),
                Component.literal("Block translation key " + block.getDescriptionId()));
            context.assertTrue(block.asItem().getDescriptionId().equals(translationKey),
                Component.literal("Item translation key " + block.asItem().getDescriptionId()));

            var lootTableKey = block.getLootTable().orElseThrow();
            context.assertTrue(lootTableKey.identifier().getNamespace().equals(QuarryMod.MOD_ID),
                Component.literal("Loot table " + lootTableKey.identifier()));
            LootTable lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(lootTableKey);
            context.assertTrue(lootTable != LootTable.EMPTY, Component.literal("Loot table " + lootTableKey.identifier() + " is missing"));
        }
        context.succeed();
    }
}
