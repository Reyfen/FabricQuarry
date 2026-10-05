package net.quarrymod.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.quarrymod.config.QuarryMachineConfig;
import net.quarrymod.init.QuarryManagerContent;

public class ToolTipAssistUtils {

    public static List<Component> getUpgradeStats(QuarryManagerContent.Upgrades upgradeType, boolean shiftHeld) {
        List<Component> tips = new ArrayList<>();

        switch (upgradeType) {
            case RANGE_EXTENDER_LVL1 -> getTextForRangeExtender(1, tips);
            case RANGE_EXTENDER_LVL2 -> getTextForRangeExtender(2, tips);
            case RANGE_EXTENDER_LVL3 -> getTextForRangeExtender(3, tips);
            default -> tips.add(
                Component.translatable("tooltip.quarrymod." + upgradeType.name)
                    .withStyle(ChatFormatting.GOLD));
        }

        if (shiftHeld && upgradeType.name.contains("lvl")) {
            tips.add(Component.nullToEmpty(""));
            String translation = I18n.get("tooltip.quarrymod.upgrade_leveled_warining");
            Arrays.stream(translation.split("\n"))
                .forEach(line -> tips.add(Component.nullToEmpty(line.formatted(ChatFormatting.RED))));
        }

        return tips;
    }

    private static void getTextForRangeExtender(int level, List<Component> tips) {
        tips.add(Component.translatable("tooltip.quarrymod.range_extender_effect")
            .withStyle(ChatFormatting.GOLD));
        tips.add(Component.translatable("tooltip.quarrymod.range_extender_value")
            .withStyle(ChatFormatting.GREEN)
            .append(Component.nullToEmpty(String.valueOf(QuarryMachineConfig.quarrySqrWorkRadiusByUpgradeLevel.get(level).intValue())
                .formatted(ChatFormatting.GOLD)))
            .append(Component.translatable("tooltip.quarrymod.range_extender_blocks")
                .withStyle(ChatFormatting.GOLD)));
    }
}
