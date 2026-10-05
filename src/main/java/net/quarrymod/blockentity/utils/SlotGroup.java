package net.quarrymod.blockentity.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.RebornInventory;


public final class SlotGroup<T extends MachineBaseBlockEntity> {

    private final RebornInventory<T> inventory;
    private final int[] slotList;

    public SlotGroup(RebornInventory<T> inventory, int[] slotList) {
        this.inventory = inventory;
        this.slotList = slotList;
    }

    private boolean hasSpace(int slot, ItemStack stack) {
        ItemStack slotStack = inventory.getItem(slot);

        return slotStack.isEmpty()
            || ItemUtils.isItemEqual(slotStack, stack, true, true)
            && slotStack.getCount() + stack.getCount() <= stack.getMaxStackSize();
    }

    public boolean hasSpace(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        list.add(stack);
        return hasSpace(list);
    }

    public boolean hasSpace(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            boolean canAdd = false;

            for (int slot : slotList) {
                if (hasSpace(slot, stack)) {
                    canAdd = true;
                    break;
                }
            }

            if (!canAdd) {
                return false;
            }
        }

        return true;
    }


    private void addStack(int slot, ItemStack stack) {
        ItemStack slotStack = inventory.getItem(slot);

        if (slotStack.isEmpty()) {
            inventory.setItem(slot, stack);
        } else if (ItemUtils.isItemEqual(slotStack, stack, true, true)) {
            slotStack.setCount((Math.min(stack.getMaxStackSize(), stack.getCount() + slotStack.getCount())));
        }
    }

    public void addStack(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        list.add(stack);
        addStacks(list);
    }

    public void addStacks(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            for (int slotId : slotList) {
                if (hasSpace(slotId, drop)) {
                    addStack(slotId, drop);
                    break;
                }
            }
        }
        inventory.setHasChanged();
    }

    @SuppressWarnings("ConstantConditions")
    public void consume(ItemStack stack) {
        ItemStack slotStack = getConsumeSlot(stack);
        slotStack.setCount(slotStack.getCount() - stack.getCount());
    }

    public boolean canConsume(ItemStack stack) {
        return getConsumeSlot(stack) != null;
    }

    public void executeForAll(Consumer<ItemStack> func) {
        for (int slotId : slotList) {
            func.accept(inventory.getItem(slotId));
        }
    }

    // TODO: make it possible to grab from multiple slots
    public ItemStack consumeAny(int count, Predicate<ItemStack> filter) {
        for (int slotId : slotList) {
            ItemStack slotStack = inventory.getItem(slotId);
            if (!slotStack.isEmpty()
                && slotStack.getCount() >= count
                && filter.test(slotStack)) {
                ItemStack consumeStack = slotStack.copy();
                consumeStack.setCount(count);
                consume(consumeStack);
                return consumeStack;
            }
        }

        return ItemStack.EMPTY;
    }

    // TODO: make it possible to grab from multiple slots
    private ItemStack getConsumeSlot(ItemStack stack) {
        for (int slotId : slotList) {
            ItemStack slotStack = inventory.getItem(slotId);
            if (!slotStack.isEmpty()
                && ItemUtils.isItemEqual(slotStack, stack, true, true)
                && slotStack.getCount() >= stack.getCount()) {
                return slotStack;
            }
        }
        return null;
    }
}
