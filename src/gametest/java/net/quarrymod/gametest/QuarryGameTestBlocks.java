package net.quarrymod.gametest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class QuarryGameTestBlocks implements ModInitializer {

    public static final String MOD_ID = "quarrymod-gametest";

    public static final Block TAGGED_ROCK = new Block(AbstractBlock.Settings.create());
    public static final Block DENIED_ORE = new Block(AbstractBlock.Settings.create());
    public static final Block ORE_SHINY = new Block(AbstractBlock.Settings.create());
    public static final Block ORE_MACHINE = new MachineBlock(AbstractBlock.Settings.create());
    public static final Block PLAIN_ROCK = new Block(AbstractBlock.Settings.create());

    @Override
    public void onInitialize() {
        register("tagged_rock", TAGGED_ROCK);
        register("denied_ore", DENIED_ORE);
        register("ore_shiny", ORE_SHINY);
        register("ore_machine", ORE_MACHINE);
        register("plain_rock", PLAIN_ROCK);
    }

    private static void register(String name, Block block) {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
    }

    private static class MachineBlock extends Block implements BlockEntityProvider {

        MachineBlock(Settings settings) {
            super(settings);
        }

        @Nullable
        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return null;
        }
    }
}
