package net.quarrymod.block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.quarrymod.init.QuarryManagerContent;

import java.util.Arrays;

public class QuarryItemGroup {
    public static void registerItemsInItemGroup() {
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("techreborn", "item_group"))).register(entries -> {
            entries.accept(QuarryManagerContent.DRILL_TUBE);
            Arrays.stream(QuarryManagerContent.Machine.values()).forEach(value -> entries.accept(value.block));
            Arrays.stream(QuarryManagerContent.Upgrades.values()).forEach(value -> entries.accept(value.item));
        });
    }
}
