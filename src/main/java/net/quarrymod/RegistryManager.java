package net.quarrymod;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.util.Identifier;
import net.quarrymod.block.QuarryItemGroup;
import net.quarrymod.client.QuarryScreenRegistry;
import net.quarrymod.events.StackToolTipHandler;
import net.quarrymod.init.QuarryManagerContent;
import net.quarrymod.init.QuarryManagerContent.Machine;
import net.quarrymod.init.QuarryManagerContent.Upgrades;
import net.quarrymod.init.QuarryModBlockEntities;

import java.util.Arrays;

import static reborncore.RebornRegistry.registerBlock;
import static reborncore.RebornRegistry.registerItem;

public class RegistryManager {

    private static Settings itemGroupSettings;

    private RegistryManager() {
    }

    public static Settings getItemGroupSettings() {
        return itemGroupSettings;
    }

    public static void Init() {
        itemGroupSettings = new Item.Settings();

        registerBlock(QuarryManagerContent.DRILL_TUBE,
            itemGroupSettings,
            Identifier.of(QuarryMod.MOD_ID, "drill_tube"));

        Arrays.stream(Machine.values()).forEach(
            value ->
                registerBlock(value.block,
                    itemGroupSettings,
                    Identifier.of(QuarryMod.MOD_ID, value.name)));

        Arrays.stream(Upgrades.values()).forEach(
            value -> registerItem(value.item, Identifier.of(QuarryMod.MOD_ID, value.name)));
        QuarryModBlockEntities.init();
        QuarryItemGroup.registerItemsInItemGroup();
    }

    @SuppressWarnings("MethodCallSideOnly")
    public static void ClientInit() {

        StackToolTipHandler.setup();
        QuarryScreenRegistry.init();
    }
}
