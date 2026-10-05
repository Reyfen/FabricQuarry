package net.quarrymod.events;

import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.quarrymod.QuarryMod;
import net.quarrymod.init.QuarryManagerContent;
import net.quarrymod.items.QuarryUpgradeItem;

import java.util.List;
import java.util.Map;

import static net.quarrymod.utils.ToolTipAssistUtils.getUpgradeStats;

public class StackToolTipHandler implements ItemTooltipCallback {

    public static final Map<Item, Boolean> IS_QM_ITEM_CACHE = Maps.newHashMap();

    public static void setup() {
        ItemTooltipCallback.EVENT.register(new StackToolTipHandler());
    }

    @Override
    public void getTooltip(ItemStack stack, TooltipContext tooltipContext, TooltipFlag tooltipType, List<Component> tooltipLines) {
        Item item = stack.getItem();

        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }
        if (!isQMItem(item)) {
            return;
        }

        if (item instanceof QuarryUpgradeItem quarryItem) {
            tooltipLines.addAll(
                getUpgradeStats(
                    QuarryManagerContent.Upgrades.getFrom(quarryItem),
                    Minecraft.getInstance().hasShiftDown()));
        }
    }

    private static boolean isQMItem(Item item) {
        return IS_QM_ITEM_CACHE.computeIfAbsent(item,
            b -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(QuarryMod.MOD_ID));
    }
}
