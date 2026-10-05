package net.quarrymod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.quarrymod.RegistryManager;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import org.jetbrains.annotations.NotNull;

public class QuarryUpgradeItem extends Item implements IQuarryUpgrade {

    public final String name;
    public final IQuarryUpgrade behavior;

    public QuarryUpgradeItem(String name, IQuarryUpgrade quarryBehaviour) {
        super(new Item.Settings().maxCount(16).registryKey(RegistryManager.itemKey(name)));
        this.name = name;
        this.behavior = quarryBehaviour;
    }

    @Override
    public void process(
        @NotNull QuarryBlockEntity quarryBlockEntity,
        @NotNull ItemStack stack) {
        behavior.process(quarryBlockEntity, stack);
    }
}
