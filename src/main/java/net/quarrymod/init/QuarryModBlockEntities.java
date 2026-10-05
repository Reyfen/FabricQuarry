package net.quarrymod.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.quarrymod.QuarryMod;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class QuarryModBlockEntities {

    private static final List<BlockEntityType<?>> TYPES = new ArrayList<>();

    private QuarryModBlockEntities() {
    }

    public static final BlockEntityType<QuarryBlockEntity> QUARRY = register(QuarryBlockEntity::new, "quarry",
        QuarryManagerContent.Machine.QUARRY);


    public static <T extends BlockEntity> BlockEntityType<T> register(BiFunction<BlockPos, BlockState, T> supplier,
        String name, ItemLike... items) {
        return register(supplier, name,
            Arrays.stream(items).map(itemConvertible -> Block.byItem(itemConvertible.asItem()))
                .toArray(Block[]::new));
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(BiFunction<BlockPos, BlockState, T> supplier,
        String name, Block... blocks) {
        Validate.isTrue(blocks.length > 0, "no blocks for blockEntity entity type!");
        return register(Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, name).toString(),
            FabricBlockEntityTypeBuilder.create(supplier::apply, blocks));
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String id,
        FabricBlockEntityTypeBuilder<T> builder) {
        BlockEntityType<T> blockEntityType = builder.build(null);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.parse(id), blockEntityType);
        QuarryModBlockEntities.TYPES.add(blockEntityType);
        return blockEntityType;
    }

    public static void init() {
        //Force loads the block entities at the right time
        TYPES.toString();
    }
}
