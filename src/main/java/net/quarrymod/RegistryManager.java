package net.quarrymod;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

    private RegistryManager() {
    }

    public static RegistryKey<Block> blockKey(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(QuarryMod.MOD_ID, name));
    }

    public static RegistryKey<Item> itemKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(QuarryMod.MOD_ID, name));
    }

    private static BlockItem blockItem(Block block) {
        return new BlockItem(block, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Registries.BLOCK.getId(block)))
            .useBlockPrefixedTranslationKey());
    }

    public static void Init() {
        registerBlock(QuarryManagerContent.DRILL_TUBE,
            RegistryManager::blockItem,
            Identifier.of(QuarryMod.MOD_ID, "drill_tube"));

        Arrays.stream(Machine.values()).forEach(
            value ->
                registerBlock(value.block,
                    RegistryManager::blockItem,
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
