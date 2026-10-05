package net.quarrymod.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.quarrymod.RegistryManager;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.client.GuiType;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.blocks.BlockMachineBase;

public class QuarryBlock extends BlockMachineBase {

    public static final EnumProperty<DisplayState> STATE = EnumProperty.create("state", DisplayState.class);

    public QuarryBlock() {
        super(BlockBehaviour.Properties.of()
            .sound(SoundType.METAL)
            .mapColor(MapColor.METAL)
            .strength(2f, 2f)
            .setId(RegistryManager.blockKey("quarry")));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override
    public IMachineGuiHandler getGui() {
        return GuiType.QUARRY;
    }

    public void setState(DisplayState state, Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(STATE, state));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, STATE);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof QuarryBlockEntity quarryEntity) {
            quarryEntity.resetOnPlaced();
        }
    }

    public enum DisplayState implements StringRepresentable {
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
        public String getSerializedName() {
            return name;
        }

        public ChatFormatting getFormatting() {
            return switch (this) {
                case ExtractTube -> ChatFormatting.GREEN;
                case Complete -> ChatFormatting.AQUA;
                default -> ChatFormatting.RED;
            };
        }
    }
}
