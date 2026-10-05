package net.quarrymod.config;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.quarrymod.QuarryMod;
import reborncore.common.config.Config;
import reborncore.common.config.ConfigGroup;
import reborncore.common.config.ConfigValue;
import reborncore.common.config.RebornCoreConfigApi;

public class QuarryMachineConfig {

    public static final int QUARRY_MINE_ORE_CONFIG = 1;
    public static final int QUARRY_MINE_ALL_CONFIG = 2;

    private static final Config MACHINES = RebornCoreConfigApi.config(
        Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, "machines"));
    private static final ConfigGroup QUARRY = MACHINES.group("quarry");

    public static final ConfigValue<Integer> quarryMaxInput = QUARRY.intValue("QuarryMaxInput", 128)
        .comment("Quarry Max Input (Value in EU)");

    public static final ConfigValue<Double> quarryMaxInputOverclockerMultipier = QUARRY.doubleValue("QuarryMaxInputOverclockerMultipier", 6)
        .comment("Quarry Max Input Multiplier by overclocker upgrades");

    public static final ConfigValue<Integer> quarryMaxEnergy = QUARRY.intValue("QuarryMaxEnergy", 100_000)
        .comment("Quarry Max Energy (Value in EU)");

    public static final ConfigValue<Integer> quarryEnergyPerExcavation = QUARRY.intValue("QuarryEnergyPerExcavation", 6_000)
        .comment("Quarry Energy Per Excavation (Value in EU)");

    public static final ConfigValue<Integer> quarryTiksPerExcavation = QUARRY.intValue("QuarryTiksPerExcavation", 60)
        .comment("Quarry Tiks Per Excavation, 20 ticks - 1 second");

    public static final ConfigValue<Integer> quarryMinTiksPerExcavation = QUARRY.intValue("QuarryMinTiksPerExcavation", 8)
        .comment("Quarry Min Tiks Per Excavation (with all 4 upgrdes), 20 ticks - 1 second");

    public static final ConfigValue<List<Double>> quarrySqrWorkRadiusByUpgradeLevel = QUARRY.codec("QuarryExtenderWorkRadius", Codec.DOUBLE.listOf(), List.of(7.0, 12.0, 18.0, 24.0))
        .comment("Quarry Extender Work Radius, added radius to SqrWorkRadius for each level, in blocks");

    public static final ConfigValue<Integer> quarryAccessibleExcavationModes = QUARRY.intValue("QuarryAccessibleExcavationModes", 3)
        .comment("Quarry Accessible Excavation Modes, 1 - ores only, 2 - all only, 3 - all and ores");

    public static final ConfigValue<List<String>> quarryAdditioanlBlocksToMine = QUARRY.stringListValue("QuarryAdditioanlBlocksToMine", List.of())
        .comment("Additional blocks to mine in ores mode. Block ids (modid:block) or block tags (#modid:tag)");

    public static final ConfigValue<List<String>> quarryBlocksToNeverMine = QUARRY.stringListValue("QuarryBlocksToNeverMine", List.of())
        .comment("Blocks never mined in ores mode, even if they look like ores. Block ids (modid:block) or block tags (#modid:tag)");

    public static void register() {
        RebornCoreConfigApi.register(MACHINES);
    }
}
