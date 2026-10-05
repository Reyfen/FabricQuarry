package net.quarrymod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class QuarryScreenRegistry {

    private QuarryScreenRegistry() {
        // Left empty to hide the original constructor
    }

    public static void init() {
        MenuScreens.register(GuiType.QUARRY.getType(), GuiType.QUARRY.getGuiFactory());
    }
}
