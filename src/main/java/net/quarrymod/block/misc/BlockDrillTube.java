package net.quarrymod.block.misc;

import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;
import net.quarrymod.RegistryManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class BlockDrillTube extends Block {

    public BlockDrillTube() {
        super(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.BLACK)
                    .strength(2.0F, 3.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .registryKey(RegistryManager.blockKey("drill_tube")));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0.31f, 0f, 0.31f, 0.69f, 1.0f, 0.69f);
    }
}
