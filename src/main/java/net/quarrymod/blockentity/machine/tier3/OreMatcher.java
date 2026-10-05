package net.quarrymod.blockentity.machine.tier3;

import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.quarrymod.QuarryMod;
import net.quarrymod.config.QuarryMachineConfig;

public class OreMatcher {

    public static final TagKey<Block> ORES = TagKey.of(RegistryKeys.BLOCK, new Identifier(QuarryMod.MOD_ID, "ores"));
    public static final TagKey<Block> NEVER_MINE = TagKey.of(RegistryKeys.BLOCK,
        new Identifier(QuarryMod.MOD_ID, "never_mine"));

    private static final Pattern ORE_NAME = Pattern.compile("(^|_)ores?($|_)");

    private static final Reference2BooleanOpenHashMap<Block> CACHE = new Reference2BooleanOpenHashMap<>();
    private static BlockFilter allowFilter;
    private static BlockFilter denyFilter;

    private OreMatcher() {
    }

    public static boolean isOre(BlockState state) {
        Block block = state.getBlock();
        synchronized (CACHE) {
            if (CACHE.containsKey(block)) {
                return CACHE.getBoolean(block);
            }
            boolean result = computeIsOre(state);
            CACHE.put(block, result);
            return result;
        }
    }

    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
            allowFilter = null;
            denyFilter = null;
        }
    }

    private static boolean computeIsOre(BlockState state) {
        Block block = state.getBlock();
        Identifier id = Registries.BLOCK.getId(block);

        if (denyFilter == null) {
            denyFilter = BlockFilter.parse(QuarryMachineConfig.quarryBlocksToNeverMine);
        }
        if (state.isIn(NEVER_MINE) || denyFilter.matches(state, id)) {
            return false;
        }

        if (state.isIn(ORES)) {
            return true;
        }

        if (allowFilter == null) {
            allowFilter = BlockFilter.parse(QuarryMachineConfig.quarryAdditioanlBlocksToMine);
        }
        if (allowFilter.matches(state, id)) {
            return true;
        }

        return !(block instanceof BlockEntityProvider) && ORE_NAME.matcher(id.getPath()).find();
    }

    private record BlockFilter(Set<Identifier> ids, List<TagKey<Block>> tags) {

        static BlockFilter parse(List<String> entries) {
            Set<Identifier> ids = new HashSet<>();
            List<TagKey<Block>> tags = new ArrayList<>();
            if (entries != null) {
                for (String entry : entries) {
                    String value = entry.trim();
                    boolean isTag = value.startsWith("#");
                    Identifier id = Identifier.tryParse(isTag ? value.substring(1) : value);
                    if (id == null) {
                        continue;
                    }
                    if (isTag) {
                        tags.add(TagKey.of(RegistryKeys.BLOCK, id));
                    } else {
                        ids.add(id);
                    }
                }
            }
            return new BlockFilter(ids, tags);
        }

        boolean matches(BlockState state, Identifier id) {
            if (ids.contains(id)) {
                return true;
            }
            for (TagKey<Block> tag : tags) {
                if (state.isIn(tag)) {
                    return true;
                }
            }
            return false;
        }
    }
}
