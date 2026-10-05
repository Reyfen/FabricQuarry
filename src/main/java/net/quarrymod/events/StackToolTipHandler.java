package net.quarrymod.events;

import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
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
    public void getTooltip(ItemStack stack, TooltipContext tooltipContext, TooltipType tooltipType, List<Text> tooltipLines) {
        Item item = stack.getItem();

        if (!MinecraftClient.getInstance().isOnThread()) {
            return;
        }
        if (!isQMItem(item)) {
            return;
        }

        if (item instanceof QuarryUpgradeItem quarryItem) {
            tooltipLines.addAll(
                getUpgradeStats(
                    QuarryManagerContent.Upgrades.getFrom(quarryItem),
                    MinecraftClient.getInstance().isShiftPressed()));
        }
    }

    private static boolean isQMItem(Item item) {
        return IS_QM_ITEM_CACHE.computeIfAbsent(item,
            b -> Registries.ITEM.getId(item).getNamespace().equals(QuarryMod.MOD_ID));
    }
}
