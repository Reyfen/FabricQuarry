package net.quarrymod.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.quarrymod.RegistryManager;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import org.jetbrains.annotations.NotNull;

public class QuarryUpgradeItem extends Item implements IQuarryUpgrade {

    public final String name;
    public final IQuarryUpgrade behavior;

    public QuarryUpgradeItem(String name, IQuarryUpgrade quarryBehaviour) {
        super(new Item.Properties().stacksTo(16).setId(RegistryManager.itemKey(name)));
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
