package net.quarrymod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.quarrymod.RegistryManager;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.client.GuiType;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.blocks.BlockMachineBase;
import reborncore.common.util.ItemHandlerUtils;

public class QuarryBlock extends BlockMachineBase {

    public static final EnumProperty<DisplayState> STATE = EnumProperty.of("state", DisplayState.class);

    public QuarryBlock() {
        super(AbstractBlock.Settings.create()
            .sounds(BlockSoundGroup.METAL)
            .mapColor(MapColor.IRON_GRAY)
            .strength(2f, 2f)
            .registryKey(RegistryManager.blockKey("quarry")));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override
    public IMachineGuiHandler getGui() {
        return GuiType.QUARRY;
    }

    public void setState(DisplayState state, World world, BlockPos pos) {
        world.setBlockState(pos, world.getBlockState(pos).with(STATE, state));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, STATE);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof QuarryBlockEntity quarryEntity) {
            quarryEntity.resetOnPlaced();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()
            && worldIn.getBlockEntity(pos) instanceof QuarryBlockEntity quarryEntity) {
            ItemHandlerUtils.dropItemHandler(worldIn, pos, quarryEntity.quarryUpgradesInventory);
            super.onStateReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    public enum DisplayState implements StringIdentifiable {
        Off("off"),
        Mining("mining"),
        ExtractTube("extract_tube"),
        Error("error"),
        Complete("complete");

        private final String name;

        private DisplayState(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        public Formatting getFormatting() {
            return switch (this) {
                case ExtractTube -> Formatting.GREEN;
                case Complete -> Formatting.AQUA;
                default -> Formatting.RED;
            };
        }
    }
}
