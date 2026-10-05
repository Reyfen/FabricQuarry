package net.quarrymod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class QuarryScreenRegistry {

    private QuarryScreenRegistry() {
        // Left empty to hide the original constructor
    }

    public static void init() {
        HandledScreens.register(GuiType.QUARRY.getType(), GuiType.QUARRY.getGuiFactory());
    }
}
