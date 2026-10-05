package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.quarrymod.QuarryMod;
import net.quarrymod.init.QuarryManagerContent;

public class QuarryDataGameTest implements FabricGameTest {

    private static final int RECIPE_COUNT = 9;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void recipesLoad(TestContext context) {
        long loaded = context.getWorld().getServer().getRecipeManager().values().stream()
            .filter(entry -> entry.id().getValue().getNamespace().equals(QuarryMod.MOD_ID))
            .count();
        context.assertTrue(loaded == RECIPE_COUNT, "Loaded " + loaded + " of " + RECIPE_COUNT + " recipes");
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void blocksUseModNamespace(TestContext context) {
        for (Block block : new Block[]{QuarryManagerContent.Machine.QUARRY.block, QuarryManagerContent.DRILL_TUBE}) {
            String path = Registries.BLOCK.getId(block).getPath();
            String translationKey = "block." + QuarryMod.MOD_ID + "." + path;
            context.assertTrue(block.getTranslationKey().equals(translationKey),
                "Block translation key " + block.getTranslationKey());
            context.assertTrue(block.asItem().getTranslationKey().equals(translationKey),
                "Item translation key " + block.asItem().getTranslationKey());

            var lootTableKey = block.getLootTableKey().orElseThrow();
            context.assertTrue(lootTableKey.getValue().getNamespace().equals(QuarryMod.MOD_ID),
                "Loot table " + lootTableKey.getValue());
            LootTable lootTable = context.getWorld().getServer().getReloadableRegistries().getLootTable(lootTableKey);
            context.assertTrue(lootTable != LootTable.EMPTY, "Loot table " + lootTableKey.getValue() + " is missing");
        }
        context.complete();
    }
}
