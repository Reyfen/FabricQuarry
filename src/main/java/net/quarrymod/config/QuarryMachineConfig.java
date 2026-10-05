package net.quarrymod.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import reborncore.common.config.Config;

public class QuarryMachineConfig {

    public static final int QUARRY_MINE_ORE_CONFIG = 1;
    public static final int QUARRY_MINE_ALL_CONFIG = 2;

    @Config(config = "machines", category = "quarry", key = "QuarryMaxInput", comment = "Quarry Max Input (Value in EU)")
    public static int quarryMaxInput = 128;

    @Config(config = "machines", category = "quarry", key = "QuarryMaxInputOverclockerMultipier", comment = "Quarry Max Input Multiplier by overclocker upgrades")
    public static double quarryMaxInputOverclockerMultipier = 6;

    @Config(config = "machines", category = "quarry", key = "QuarryMaxEnergy", comment = "Quarry Max Energy (Value in EU)")
    public static int quarryMaxEnergy = 100_000;

    @Config(config = "machines", category = "quarry", key = "QuarryEnergyPerExcavation", comment = "Quarry Energy Per Excavation (Value in EU)")
    public static int quarryEnergyPerExcavation = 6_000;

    @Config(config = "machines", category = "quarry", key = "QuarryTiksPerExcavation", comment = "Quarry Tiks Per Excavation, 20 ticks - 1 second")
    public static int quarryTiksPerExcavation = 60;

    @Config(config = "machines", category = "quarry", key = "QuarryMinTiksPerExcavation", comment = "Quarry Min Tiks Per Excavation (with all 4 upgrdes), 20 ticks - 1 second")
    public static int quarryMinTiksPerExcavation = 8;

    @Config(config = "machines", category = "quarry", key = "QuarryExtenderWorkRadius", comment = "Quarry Extender Work Radius, added radius to SqrWorkRadius for each level, in blocks")
    public static List<Double> quarrySqrWorkRadiusByUpgradeLevel = Arrays.asList(7.0, 12.0, 18.0, 24.0);

    @Config(config = "machines", category = "quarry", key = "QuarryAccessibleExcavationModes", comment = "Quarry Accessible Excavation Modes, 1 - ores only, 2 - all only, 3 - all and ores")
    public static int quarryAccessibleExcavationModes = 3;

    @Config(config = "machines", category = "quarry", key = "QuarryAdditioanlBlocksToMine", comment = "Additional blocks to mine in ores mode. Block ids (modid:block) or block tags (#modid:tag)")
    public static List<String> quarryAdditioanlBlocksToMine = new ArrayList<>();

    @Config(config = "machines", category = "quarry", key = "QuarryBlocksToNeverMine", comment = "Blocks never mined in ores mode, even if they look like ores. Block ids (modid:block) or block tags (#modid:tag)")
    public static List<String> quarryBlocksToNeverMine = new ArrayList<>();
}
