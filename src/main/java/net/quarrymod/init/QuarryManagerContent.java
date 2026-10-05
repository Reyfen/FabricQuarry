package net.quarrymod.init;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.quarrymod.block.QuarryBlock;
import net.quarrymod.block.misc.BlockDrillTube;
import net.quarrymod.items.IQuarryUpgrade;
import net.quarrymod.items.QuarryUpgradeItem;


public class QuarryManagerContent {

    public static final Block DRILL_TUBE = new BlockDrillTube();

    public enum Machine implements ItemConvertible {
        QUARRY(new QuarryBlock());

        public final String name;
        public final Block block;

        <B extends Block> Machine(B block) {
            this.name = this.toString().toLowerCase(Locale.ROOT);
            this.block = block;
        }

        public ItemStack getStack() {
            return new ItemStack(block);
        }

        @Override
        public Item asItem() {
            return block.asItem();
        }
    }

    public enum Upgrades implements ItemConvertible {
        RANGE_EXTENDER_LVL1((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setRangeExtenderLevel(1);
        }),
        RANGE_EXTENDER_LVL2((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setRangeExtenderLevel(2);
        }),
        RANGE_EXTENDER_LVL3((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setRangeExtenderLevel(3);
        }),
        FORTUNE_LVL1((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setFortuneLevel(1);
        }),
        FORTUNE_LVL2((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setFortuneLevel(2);
        }),
        FORTUNE_LVL3((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setFortuneLevel(3);
        }),
        SILKTOUCH((quarryBlockEntity, stack) -> {
            quarryBlockEntity.setSilkTouch(true);
        });

        public final String name;
        public final Item item;

        Upgrades(IQuarryUpgrade upgrade) {
            name = this.toString().toLowerCase(Locale.ROOT) + "_upgrade";
            item = new QuarryUpgradeItem(name, upgrade);
        }

        @Override
        public Item asItem() {
            return item;
        }

        public static Upgrades getFrom(QuarryUpgradeItem item) {
            for (Upgrades upgrade : values()) {
                if (upgrade.item == item) {
                    return upgrade;
                }
            }

            throw null;
        }
    }
}
