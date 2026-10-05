package net.quarrymod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.quarrymod.QuarryMod;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.client.gui.QuarryScreen;
import org.jetbrains.annotations.Nullable;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GuiType<T extends BlockEntity> implements IMachineGuiHandler {

    private static final Map<Identifier, GuiType<?>> TYPES = new HashMap<>();
    public static final GuiType<QuarryBlockEntity> QUARRY = register("quarry", () -> () -> QuarryScreen::new);

    private static <T extends BlockEntity> GuiType<T> register(String id,
        Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
        return register(Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, id), factorySupplierMeme);
    }

    private static <T extends BlockEntity> GuiType<T> register(Identifier identifier,
        Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
        if (TYPES.containsKey(identifier)) {
            throw new RuntimeException("Duplicate gui type found");
        }
        GuiType<T> type = new GuiType<>(identifier, factorySupplierMeme);
        TYPES.put(identifier, type);
        return type;
    }

    private final Identifier identifier;
    private final Supplier<Supplier<GuiFactory<T>>> guiFactory;
    private final MenuType<BuiltScreenHandler> screenHandlerType;

    private GuiType(Identifier identifier, Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
        this.identifier = identifier;
        this.guiFactory = factorySupplierMeme;
        this.screenHandlerType = Registry.register(BuiltInRegistries.MENU, identifier,
            new ExtendedMenuType<>(getScreenHandlerFactory(), ScreenHandlerData.PACKET_CODEC));
    }

    private ExtendedMenuType.ExtendedFactory<BuiltScreenHandler, ScreenHandlerData> getScreenHandlerFactory() {
        return (syncId, playerInventory, data) -> {
            final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.pos());
            BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity).createScreenHandler(syncId,
                playerInventory.player);

            //Set the screen handler type, not ideal but works lol
            screenHandler.setType(screenHandlerType);

            return screenHandler;
        };
    }

    public GuiFactory<T> getGuiFactory() {
        return guiFactory.get().get();
    }

    @Override
    public void open(Player player, BlockPos pos, Level world) {
        if (!world.isClientSide()) {
            //This is awful
            player.openMenu(new ExtendedMenuProvider<ScreenHandlerData>() {
                @Override
                public ScreenHandlerData getScreenOpeningData(ServerPlayer serverPlayerEntity) {
                    return new ScreenHandlerData(pos);
                }

                @Override
                public Component getDisplayName() {
                    return Component.nullToEmpty("What is this for?");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    final BlockEntity blockEntity = player.level().getBlockEntity(pos);
                    BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity).createScreenHandler(
                        syncId, player);
                    screenHandler.setType(screenHandlerType);
                    return screenHandler;
                }
            });
        }
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public MenuType<BuiltScreenHandler> getType() {
        return screenHandlerType;
    }

    record ScreenHandlerData(BlockPos pos) {

        static final StreamCodec<RegistryFriendlyByteBuf, ScreenHandlerData> PACKET_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ScreenHandlerData::pos,
            ScreenHandlerData::new);
    }

    @Environment(EnvType.CLIENT)
    public interface GuiFactory<T extends BlockEntity> extends
        MenuScreens.ScreenConstructor<BuiltScreenHandler, AbstractContainerScreen<BuiltScreenHandler>> {

        AbstractContainerScreen<BuiltScreenHandler> create(int syncId, Player playerEntity, T blockEntity);

        @Override
        default AbstractContainerScreen<BuiltScreenHandler> create(BuiltScreenHandler builtScreenHandler,
            Inventory playerInventory, Component text) {
            Player playerEntity = playerInventory.player;
            //noinspection unchecked
            T blockEntity = (T) builtScreenHandler.getBlockEntity();
            return create(builtScreenHandler.containerId, playerEntity, blockEntity);
        }
    }
}
