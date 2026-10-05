package net.quarrymod.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.quarrymod.QuarryMod;

public class QuarryDataGameTest implements FabricGameTest {

    private static final int RECIPE_COUNT = 9;

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void recipesLoad(TestContext context) {
        long loaded = context.getWorld().getRecipeManager().values().stream()
            .filter(entry -> entry.id().getNamespace().equals(QuarryMod.MOD_ID))
            .count();
        context.assertTrue(loaded == RECIPE_COUNT, "Loaded " + loaded + " of " + RECIPE_COUNT + " recipes");
        context.complete();
    }
}
