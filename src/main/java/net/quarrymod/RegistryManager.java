package net.quarrymod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

    public static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, name));
    }

    public static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, name));
    }

    private static BlockItem blockItem(Block block) {
        return new BlockItem(block, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block)))
            .useBlockDescriptionPrefix());
    }

    public static void Init() {
        registerBlock(QuarryManagerContent.DRILL_TUBE,
            RegistryManager::blockItem,
            Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, "drill_tube"));

        Arrays.stream(Machine.values()).forEach(
            value ->
                registerBlock(value.block,
                    RegistryManager::blockItem,
                    Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, value.name)));

        Arrays.stream(Upgrades.values()).forEach(
            value -> registerItem(value.item, Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, value.name)));
        QuarryModBlockEntities.init();
        QuarryItemGroup.registerItemsInItemGroup();
    }

    @SuppressWarnings("MethodCallSideOnly")
    public static void ClientInit() {

        StackToolTipHandler.setup();
        QuarryScreenRegistry.init();
    }
}
