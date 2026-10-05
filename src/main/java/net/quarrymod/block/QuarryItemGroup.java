package net.quarrymod.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.quarrymod.init.QuarryManagerContent;

import java.util.Arrays;

public class QuarryItemGroup {
    public static void registerItemsInItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("techreborn", "item_group"))).register(entries -> {
            entries.add(QuarryManagerContent.DRILL_TUBE);
            Arrays.stream(QuarryManagerContent.Machine.values()).forEach(value -> entries.add(value.block));
            Arrays.stream(QuarryManagerContent.Upgrades.values()).forEach(value -> entries.add(value.item));
        });
    }
}
