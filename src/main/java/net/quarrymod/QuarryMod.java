package net.quarrymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.quarrymod.blockentity.machine.tier3.OreMatcher;
import net.quarrymod.config.QuarryMachineConfig;
import net.quarrymod.packets.QuarryManagerServerPacket;

public class QuarryMod implements ModInitializer {

    public static final String MOD_ID = "quarrymod";

    @Override
    public void onInitialize() {
        QuarryMachineConfig.register();
        RegistryManager.Init();
        QuarryManagerServerPacket.init();
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> OreMatcher.clearCache());
    }
}
