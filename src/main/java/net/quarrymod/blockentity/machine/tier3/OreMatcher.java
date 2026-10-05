package net.quarrymod.blockentity.machine.tier3;

import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.quarrymod.QuarryMod;
import net.quarrymod.config.QuarryMachineConfig;

public class OreMatcher {

    public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, "ores"));
    public static final TagKey<Block> NEVER_MINE = TagKey.create(Registries.BLOCK,
        Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, "never_mine"));

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
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);

        if (denyFilter == null) {
            denyFilter = BlockFilter.parse(QuarryMachineConfig.quarryBlocksToNeverMine.get());
        }
        if (state.is(NEVER_MINE) || denyFilter.matches(state, id)) {
            return false;
        }

        if (state.is(ORES)) {
            return true;
        }

        if (allowFilter == null) {
            allowFilter = BlockFilter.parse(QuarryMachineConfig.quarryAdditioanlBlocksToMine.get());
        }
        if (allowFilter.matches(state, id)) {
            return true;
        }

        return !(block instanceof EntityBlock) && ORE_NAME.matcher(id.getPath()).find();
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
                        tags.add(TagKey.create(Registries.BLOCK, id));
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
                if (state.is(tag)) {
                    return true;
                }
            }
            return false;
        }
    }
}
