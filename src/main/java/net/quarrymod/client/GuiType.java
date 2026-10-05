package net.quarrymod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        return register(Identifier.of(QuarryMod.MOD_ID, id), factorySupplierMeme);
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
    private final ScreenHandlerType<BuiltScreenHandler> screenHandlerType;

    private GuiType(Identifier identifier, Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
        this.identifier = identifier;
        this.guiFactory = factorySupplierMeme;
        this.screenHandlerType = Registry.register(Registries.SCREEN_HANDLER, identifier,
            new ExtendedScreenHandlerType<>(getScreenHandlerFactory(), ScreenHandlerData.PACKET_CODEC));
    }

    private ExtendedScreenHandlerType.ExtendedFactory<BuiltScreenHandler, ScreenHandlerData> getScreenHandlerFactory() {
        return (syncId, playerInventory, data) -> {
            final BlockEntity blockEntity = playerInventory.player.getWorld().getBlockEntity(data.pos());
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
    public void open(PlayerEntity player, BlockPos pos, World world) {
        if (!world.isClient) {
            //This is awful
            player.openHandledScreen(new ExtendedScreenHandlerFactory<ScreenHandlerData>() {
                @Override
                public ScreenHandlerData getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
                    return new ScreenHandlerData(pos);
                }

                @Override
                public Text getDisplayName() {
                    return Text.of("What is this for?");
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    final BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
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

    public ScreenHandlerType<BuiltScreenHandler> getType() {
        return screenHandlerType;
    }

    record ScreenHandlerData(BlockPos pos) {

        static final PacketCodec<RegistryByteBuf, ScreenHandlerData> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ScreenHandlerData::pos,
            ScreenHandlerData::new);
    }

    @Environment(EnvType.CLIENT)
    public interface GuiFactory<T extends BlockEntity> extends
        HandledScreens.Provider<BuiltScreenHandler, HandledScreen<BuiltScreenHandler>> {

        HandledScreen<BuiltScreenHandler> create(int syncId, PlayerEntity playerEntity, T blockEntity);

        @Override
        default HandledScreen<BuiltScreenHandler> create(BuiltScreenHandler builtScreenHandler,
            PlayerInventory playerInventory, Text text) {
            PlayerEntity playerEntity = playerInventory.player;
            //noinspection unchecked
            T blockEntity = (T) builtScreenHandler.getBlockEntity();
            return create(builtScreenHandler.syncId, playerEntity, blockEntity);
        }
    }
}
