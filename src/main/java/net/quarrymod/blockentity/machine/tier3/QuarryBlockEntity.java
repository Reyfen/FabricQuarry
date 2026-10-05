package net.quarrymod.blockentity.machine.tier3;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.quarrymod.block.QuarryBlock;
import net.quarrymod.block.QuarryBlock.DisplayState;
import net.quarrymod.block.misc.BlockDrillTube;
import net.quarrymod.blockentity.utils.SlotGroup;
import net.quarrymod.config.QuarryMachineConfig;
import net.quarrymod.init.QuarryManagerContent;
import net.quarrymod.init.QuarryModBlockEntities;
import net.quarrymod.items.IQuarryUpgrade;
import org.jetbrains.annotations.Nullable;
import reborncore.api.IToolDrop;
import reborncore.api.blockentity.InventoryProvider;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.RedstoneConfiguration;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.screen.slot.BaseSlot;
import reborncore.common.util.RebornInventory;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.quarrymod.blockentity.machine.tier3.ExcavationState.*;
import static net.quarrymod.blockentity.machine.tier3.ExcavationWorkType.ExtractTube;
import static net.quarrymod.config.QuarryMachineConfig.*;
import static reborncore.common.util.WorldUtils.isChunkLoaded;

public class QuarryBlockEntity extends PowerAcceptorBlockEntity implements IToolDrop, InventoryProvider,
    BuiltScreenHandlerProvider {

    private int rangeExtenderLevel;
    private int fortuneLevel;
    private boolean isSilkTouch;

    public final RebornInventory<QuarryBlockEntity> inventory = new RebornInventory<>(12, "QuarryBlockEntity", 64,
        this);
    public final RebornInventory<QuarryBlockEntity> quarryUpgradesInventory = new RebornInventory<>(2, "QuarryUpgrades",
        1,
        this);

    private long miningEnergySpend = 0;
    private ExcavationState excavationState = InProgress;
    private ExcavationWorkType excavationWorkType = ExcavationWorkType.Mining;
    private boolean isMineAll = false;

    private BlockPos targetBlockPosition;
    private int currentTickTime = 0;

    private final SlotGroup<QuarryBlockEntity> holeFillerSlotGroup = new SlotGroup<>(inventory, new int[] {0, 1, 2, 3});
    private final SlotGroup<QuarryBlockEntity> drillTubeSlotGroup = new SlotGroup<>(inventory, new int[] {4, 5});
    private final SlotGroup<QuarryBlockEntity> outputSlotGroup = new SlotGroup<>(inventory, new int[] {6, 7, 8, 9, 10});
    private final SlotGroup<QuarryBlockEntity> quarryUpgradesSlotGroup = new SlotGroup<>(quarryUpgradesInventory,
        new int[] {0, 1});
    private int currentRadius;
    private int currentY;
    private Queue<BlockPos> remainingBlocks = new LinkedList<>();


    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(QuarryModBlockEntities.QUARRY, pos, state);
    }

    public void setSilkTouch(boolean isSilkTouch) {
        this.isSilkTouch = isSilkTouch;
    }

    public void setFortuneLevel(int newLevel) {
        this.fortuneLevel = newLevel;
    }

    public void setRangeExtenderLevel(int rangeLevel) {
        this.rangeExtenderLevel = rangeLevel;
    }

    public int getProgressScaled(int scale) {
        if (miningEnergySpend != 0) {
            return (int) Math.min(miningEnergySpend * scale / getEnergyPerExcavation(), 100);
        }
        return 0;
    }

    public ExcavationState getExcavationState() {
        return excavationState;
    }

    public boolean getMineAll() {
        return switch (quarryAccessibleExcavationModes) {
            // ores only
            case QUARRY_MINE_ORE_CONFIG -> false;
            // all only
            case QUARRY_MINE_ALL_CONFIG -> true;
            default -> isMineAll;
        };
    }

    public void setMineAll(boolean mineAll) {
        isMineAll = switch (quarryAccessibleExcavationModes) {
            // ores only
            case QUARRY_MINE_ORE_CONFIG -> false;
            // all only
            case QUARRY_MINE_ALL_CONFIG -> true;
            default -> mineAll;
        };
    }

    private void setExcavationState(ExcavationState state) {
        if (excavationState == state) {
            return;
        }

        excavationState = state;
        refreshProperty();
    }

    private void setExcavationWorkType(ExcavationWorkType workType) {
        if (excavationWorkType == workType) {
            return;
        }

        excavationWorkType = workType;
        refreshProperty();
    }

    private void refreshProperty() {
        if (world != null && !world.isClient) {
            ((QuarryBlock) world.getBlockState(pos).getBlock()).setState(getDisplayState(), world, pos);
        }
    }

    public DisplayState getDisplayState() {
        DisplayState state = DisplayState.Off;
        if (getExcavationState() == ExcavationState.Complete) {
            state = DisplayState.Complete;
        } else if (getExcavationState().isError()) {
            state = DisplayState.Error;
        } else if (getExcavationState() == InProgress) {
            state = excavationWorkType == ExcavationWorkType.Mining ? DisplayState.Mining : DisplayState.ExtractTube;
        }

        return state;
    }

    public String getStateName() {
        if (excavationWorkType == ExtractTube && excavationState == InProgress) {
            return ExtractTube.toString();
        } else {
            return excavationState.toString();
        }
    }

    public void resetOnPlaced() {
        setExcavationState(InProgress);
        setExcavationWorkType(ExcavationWorkType.Mining);
        setProgress(0);
        this.currentRadius = 0;
        this.currentY = pos.getY();
        this.currentTickTime = 0;
        this.remainingBlocks = new LinkedList<>();
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity2) {
        super.tick(world, pos, state, blockEntity2);

        if (world == null || world.isClient) {
            return;
        }

        this.charge(11);
        refreshUpgrades();
        tickQuarryLogic();

        currentTickTime++;
    }

    private void refreshUpgrades() {
        rangeExtenderLevel = 0;
        fortuneLevel = 0;
        isSilkTouch = false;
        quarryUpgradesSlotGroup.executeForAll(stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof IQuarryUpgrade upgradeItem) {
                upgradeItem.process(this, stack);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void tickQuarryLogic() {
        if (excavationState == ExcavationState.Complete) {
            return;
        }
        if (!isActive(RedstoneConfiguration.Element.POWER_IO)) {
            setExcavationState(ExcavationState.NoEnergyIncome);
            return;
        }
        if (miningEnergySpend < getEnergyPerExcavation()) {
            performEnergyReduction();
        } else if (miningEnergySpend >= getEnergyPerExcavation()) {
            performQuarryLogic();
        }

        if (excavationState == InProgress && currentTickTime % 20 == 0) {
            RecipeCrafter.soundHandler.playSound(false, this);
        }

    }

    private void performQuarryLogic() {
        if (excavationWorkType == ExtractTube) {
            drillUp();
            return;
        }

        // Find block to mine
        if (!hasTargetBlock() && hasOneOfExcavationStates(InProgress, NoOreInCurrentPos, CannotOutputMineDrop)) {
            targetBlockPosition = findMiningPosition();
        }

        // Drill downwards
        if (!hasTargetBlock() && hasOneOfExcavationStates(NoOresInCurrentDepth, NotEnoughDrillTube)) {
            drillDown();
        } else if (hasTargetBlock() && hasOneOfExcavationStates(InProgress, CannotOutputMineDrop)) {
            performMineLogic(targetBlockPosition, !getMineAll());
        }

        if (excavationState == InProgress) {
            miningEnergySpend -= getEnergyPerExcavation();
        }
    }

    private boolean hasTargetBlock() {
        return targetBlockPosition != null;
    }

    private boolean hasOneOfExcavationStates(ExcavationState... states) {
        for (ExcavationState state : states) {
            if (this.excavationState == state) {
                return true;
            }
        }

        return false;
    }

    private void performEnergyReduction() {
        final long euNeeded = getEnergyPerExcavation() / getTicksPerExcavation();
        final long euAvailable = Math.min(euNeeded, getStored());
        if (euAvailable > 0d) {
            useEnergy(euAvailable);
            miningEnergySpend += euAvailable;
            setExcavationState(InProgress);
        } else {
            setExcavationState(ExcavationState.NoEnergyIncome);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private BlockPos findMiningPosition() {
        updateRemainingBlocks();

        while (!remainingBlocks.isEmpty()) {
            BlockPos blockPos = remainingBlocks.poll();

            if (!isChunkLoaded(world, blockPos)) {
                continue;
            }

            if (canMineBlock(blockPos)) {
                setExcavationState(InProgress);
                return blockPos;
            }
        }

        setExcavationState(NoOresInCurrentDepth);
        return null;
    }

    private void updateRemainingBlocks() {
        final int calculatedY = calculateCurrentDrillTubeDepth();
        final int radius = (int) Math.round(
            QuarryMachineConfig.quarrySqrWorkRadiusByUpgradeLevel.get(rangeExtenderLevel));
        if (currentRadius != radius || currentY != calculatedY || remainingBlocks.isEmpty()) {
            currentRadius = radius;
            currentY = calculatedY;
            remainingBlocks = createMiningArea();
        }
    }

    private Queue<BlockPos> createMiningArea() {
        Queue<BlockPos> blocks = new LinkedList<>();
        // No area to check when the height is the same as the one of the miner.
        if (currentY == pos.getY()) {
            return blocks;
        }

        return MiningUtil.createMiningPosition(currentRadius, new BlockPos(pos.getX(), currentY, pos.getZ()));
    }

    @SuppressWarnings("ConstantConditions")
    private void performMineLogic(BlockPos blockPos, boolean fillHole) {
        if (!canMineBlock(blockPos)) {
            setExcavationState(NoOreInCurrentPos);
            targetBlockPosition = null;
            return;
        }

        List<ItemStack> drop = getDroppedStacks(world.getBlockState(blockPos), blockPos);

        if (outputSlotGroup.hasSpace(drop)) {
            outputSlotGroup.addStacks(drop);
            world.removeBlock(blockPos, false);
            if (fillHole) {
                tryFillHole(blockPos);
            }
            targetBlockPosition = null;
            setExcavationState(InProgress);
        } else {
            setExcavationState(CannotOutputMineDrop);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void tryFillHole(BlockPos blockPos) {
        ItemStack blockToPlace = holeFillerSlotGroup.consumeAny(1, QuarryBlockEntity::holeFillerFilter);
        if (!blockToPlace.isEmpty() && blockToPlace.getItem() instanceof BlockItem blockItem) {
            world.setBlockState(blockPos, blockItem.getBlock().getDefaultState());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void drillDown() {
        int newDepth = currentY - 1;

        if (newDepth <= world.getBottomY()) {
            setExcavationWorkType(ExtractTube);
            setExcavationState(InProgress);
            return;
        }

        BlockPos blockPos = new BlockPos(pos.getX(), newDepth, pos.getZ());
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack drillTubeItem = new ItemStack(QuarryManagerContent.DRILL_TUBE.asItem());

        if (blockState.getHardness(world, blockPos) < 0f) {
            setExcavationWorkType(ExtractTube);
            setExcavationState(InProgress);
            return;
        }

        if (!drillTubeSlotGroup.canConsume(drillTubeItem)) {
            setExcavationState(NotEnoughDrillTube);
            return;
        }

        if (canMineBlock(blockPos)) {
            performMineLogic(blockPos, false);
            return;
        }

        if (!getMineAll()) {
            List<ItemStack> blockDrops = getDroppedStacks(blockState, blockPos);
            if (blockDrops.size() == 1) {
                ItemStack blockDrop = blockDrops.get(0);
                if (holeFillerSlotGroup.hasSpace(blockDrop) && holeFillerFilter(blockDrop)) {
                    holeFillerSlotGroup.addStack(blockDrop);
                }
            }
        }

        drillTubeSlotGroup.consume(drillTubeItem);
        setExcavationState(InProgress);
        world.setBlockState(blockPos, QuarryManagerContent.DRILL_TUBE.getDefaultState());
    }

    @SuppressWarnings("ConstantConditions")
    private void drillUp() {
        int tubeDepth = calculateCurrentDrillTubeDepth();
        if (tubeDepth == pos.getY()) {
            setExcavationState(ExcavationState.Complete);
            return;
        }

        ItemStack tubeItem = new ItemStack((QuarryManagerContent.DRILL_TUBE).asItem());

        if (drillTubeSlotGroup.hasSpace(tubeItem)) {
            BlockPos blockPos = new BlockPos(pos.getX(), tubeDepth, pos.getZ());
            world.removeBlock(blockPos, false);

            if (!getMineAll()) {
                tryFillHole(blockPos);
            }

            drillTubeSlotGroup.addStack(tubeItem);
        } else {
            setExcavationState(ExcavationState.CannotOutputDrillTube);
        }
    }

    private int getTicksPerExcavation() {
        return Math.max((int) (QuarryMachineConfig.quarryTiksPerExcavation * (1d - getSpeedMultiplier())),
            QuarryMachineConfig.quarryMinTiksPerExcavation);
    }

    private long getEnergyPerExcavation() {
        return (long) (QuarryMachineConfig.quarryEnergyPerExcavation * getPowerMultiplier());
    }

    @SuppressWarnings("ConstantConditions")
    private boolean canMineBlock(BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        return !state.isAir()
            && !(block instanceof FluidBlock)
            && state.getHardness(world, blockPos) >= 0f
            && !isDrillTube(state)
            && (getMineAll() || OreMatcher.isOre(state));
    }

    @SuppressWarnings("ConstantConditions")
    private List<ItemStack> getDroppedStacks(BlockState blockState, BlockPos blockPos) {
        ItemStack item = Items.NETHERITE_PICKAXE.getDefaultStack();
        Registry<Enchantment> enchantments = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        item.addEnchantment(enchantments.getOrThrow(Enchantments.FORTUNE), fortuneLevel);
        item.addEnchantment(enchantments.getOrThrow(Enchantments.SILK_TOUCH), isSilkTouch ? 1 : 0);
        return Block.getDroppedStacks(blockState, (ServerWorld) world, blockPos, world.getBlockEntity(blockPos),
            null, item);
    }

    @Override
    public long getBaseMaxPower() {
        return QuarryMachineConfig.quarryMaxEnergy;
    }

    @Override
    public boolean canProvideEnergy(@Nullable Direction side) {
        return false;
    }

    @Override
    public long getBaseMaxOutput() {
        return 0;
    }

    @Override
    public long getBaseMaxInput() {
        return (long) (QuarryMachineConfig.quarryMaxInput * (1d
            + getSpeedMultiplier() * QuarryMachineConfig.quarryMaxInputOverclockerMultipier));
    }

    @Override
    public ItemStack getToolDrop(PlayerEntity entityPlayer) {
        return QuarryManagerContent.Machine.QUARRY.getStack();
    }

    @Override
    public RebornInventory<QuarryBlockEntity> getInventory() {
        return inventory;
    }

    @Override
    public BuiltScreenHandler createScreenHandler(int syncID, PlayerEntity player) {
        ScreenHandlerBuilder screenHandler = new ScreenHandlerBuilder("quarry").player(player.getInventory())
            .inventory().hotbar().addInventory()
            .blockEntity(this)
            .filterSlot(0, 30, 20, QuarryBlockEntity::holeFillerFilter)
            .filterSlot(1, 48, 20, QuarryBlockEntity::holeFillerFilter)
            .filterSlot(2, 66, 20, QuarryBlockEntity::holeFillerFilter)
            .filterSlot(3, 84, 20, QuarryBlockEntity::holeFillerFilter)
            .filterSlot(4, 121, 20, QuarryBlockEntity::drillTubeFilter)
            .filterSlot(5, 139, 20, QuarryBlockEntity::drillTubeFilter)
            .outputSlot(6, 55, 66)
            .outputSlot(7, 75, 66)
            .outputSlot(8, 95, 66)
            .outputSlot(9, 115, 66)
            .outputSlot(10, 135, 66)
            .energySlot(11, 8, 72)
            .syncEnergyValue()
            .sync(PacketCodecs.VAR_LONG, this::getProgress, this::setProgress)
            .sync(PacketCodecs.INTEGER, this::getState, this::setState)
            .sync(PacketCodecs.INTEGER, this::getWorkType, this::setWorkType)
            .sync(PacketCodecs.INTEGER, this::getMiningAll, this::setMiningAll)
            .addInventory();

        try {
            createUiUpgradeSlots(screenHandler);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return screenHandler.create(this, syncID);
    }


    @SuppressWarnings( {"java:S3011", "unchecked"})
    private void createUiUpgradeSlots(ScreenHandlerBuilder screenHandler) throws NoSuchFieldException, IllegalAccessException {
        // Add additional slot group for the upgrades in hidden field
        Field slotsField = ScreenHandlerBuilder.class.getDeclaredField("slots");
        slotsField.setAccessible(true);
        List<Slot> slots = (List<Slot>) slotsField.get(screenHandler);
        slots.add(new BaseSlot(quarryUpgradesInventory, 0, -42, 30, QuarryBlockEntity::isQuarryUpgrade));
        slots.add(new BaseSlot(quarryUpgradesInventory, 1, -42, 48, QuarryBlockEntity::isQuarryUpgrade));
    }

    @Override
    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);
        NbtCompound data = tag.getCompound("Quarry");
        setState(data.getInt("state"));
        setWorkType(data.getInt("workType"));
        setProgress(data.getLong("progress"));
        setMiningAll(data.getInt("mineAll"));
        quarryUpgradesInventory.read(tag, "quarryUpgradesInventory", registryLookup);
    }

    @Override
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(tag, registryLookup);
        NbtCompound data = new NbtCompound();
        data.putInt("state", getState());
        data.putInt("workType", getWorkType());
        data.putLong("progress", getProgress());
        data.putInt("mineAll", getMiningAll());
        tag.put("Quarry", data);
        tag.put("quarryUpgradesInventory", quarryUpgradesInventory.serializeNBT(registryLookup));
    }

    private long getProgress() {
        return miningEnergySpend;
    }

    private void setProgress(long progress) {
        miningEnergySpend = progress;
    }

    private int getState() {
        return excavationState.ordinal();
    }

    private void setState(int state) {
        setExcavationState(ExcavationState.values()[state]);
    }

    private int getWorkType() {
        return excavationWorkType.ordinal();
    }

    private void setWorkType(int state) {
        excavationWorkType = ExcavationWorkType.values()[state];
    }

    private int getMiningAll() {
        return getMineAll() ? 1 : 0;
    }

    private void setMiningAll(int mineAll) {
        setMineAll(mineAll == 1);
    }

    private static boolean holeFillerFilter(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock().getDefaultState().getBlock().equals(Blocks.STONE);
        }
        return false;
    }

    private static boolean drillTubeFilter(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof BlockDrillTube;
        }
        return false;
    }

    private static boolean isQuarryUpgrade(ItemStack stack) {
        return stack.getItem() instanceof IQuarryUpgrade;
    }

    @SuppressWarnings("ConstantConditions")
    private int calculateCurrentDrillTubeDepth() {
        boolean hasTubes = false;

        for (int y = pos.getY() - 1; y >= world.getBottomY(); y--) {
            if (isDrillTube(world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())))) {
                hasTubes = true;
            } else {
                return y + 1;
            }
        }

        return hasTubes ? world.getBottomY() : pos.getY();
    }

    private boolean isDrillTube(BlockState blockState) {
        return (blockState.getBlock() instanceof BlockDrillTube);
    }

}
