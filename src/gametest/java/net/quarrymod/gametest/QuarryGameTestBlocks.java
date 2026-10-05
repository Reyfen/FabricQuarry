package net.quarrymod.gametest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuarryGameTestBlocks implements ModInitializer {

    public static final String MOD_ID = "quarrymod-gametest";

    public static final Block TAGGED_ROCK = new Block(settings("tagged_rock"));
    public static final Block DENIED_ORE = new Block(settings("denied_ore"));
    public static final Block ORE_SHINY = new Block(settings("ore_shiny"));
    public static final Block ORE_MACHINE = new MachineBlock(settings("ore_machine"));
    public static final Block PLAIN_ROCK = new Block(settings("plain_rock"));

    @Override
    public void onInitialize() {
        register("tagged_rock", TAGGED_ROCK);
        register("denied_ore", DENIED_ORE);
        register("ore_shiny", ORE_SHINY);
        register("ore_machine", ORE_MACHINE);
        register("plain_rock", PLAIN_ROCK);
    }

    private static ResourceKey<Block> key(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private static BlockBehaviour.Properties settings(String name) {
        return BlockBehaviour.Properties.of().setId(key(name));
    }

    private static void register(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, key(name), block);
    }

    private static class MachineBlock extends Block implements EntityBlock {

        MachineBlock(Properties settings) {
            super(settings);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return null;
        }
    }
}
