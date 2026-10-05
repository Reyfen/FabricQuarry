package net.quarrymod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.quarrymod.QuarryMod;
import net.quarrymod.block.QuarryBlock.DisplayState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.config.QuarryMachineConfig;
import net.quarrymod.packets.QuarryManagerServerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import reborncore.client.gui.GuiBase;
import reborncore.client.gui.GuiBuilder;
import reborncore.client.gui.widget.GuiButtonExtended;
import reborncore.common.screen.BuiltScreenHandler;

public class QuarryScreen extends GuiBase<BuiltScreenHandler> {

    public static final Identifier defaultTextureSheet = Identifier.of(QuarryMod.MOD_ID,
        "textures/gui/guielements.png");

    private final QuarryBlockEntity blockEntity;
    private GuiButtonExtended mineAllButton;
    private GuiButtonExtended mineOresButton;

    public QuarryScreen(int syncID, final PlayerEntity player, final QuarryBlockEntity blockEntity) {
        super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
        this.blockEntity = blockEntity;
    }

    @Override
    public void init() {
        super.init();
        mineAllButton = addDrawableChild(
            new GuiButtonExtended(x + 29, y + 39, 54, 20, Text.translatable("gui.quarrymod.quarry.mine_all"),
                (ButtonWidget buttonWidget) -> changeMineAll(false)));
        mineOresButton = addDrawableChild(
            new GuiButtonExtended(x + 29, y + 39, 54, 20, Text.translatable("gui.quarrymod.quarry.mine_ores"),
                (ButtonWidget buttonWidget) -> changeMineAll(true)));
        mineAllButton.visible = false;
        mineOresButton.visible = false;
    }

    @Override
    protected void drawBackground(DrawContext drawContext, final float f, final int mouseX, final int mouseY) {
        super.drawBackground(drawContext, f, mouseX, mouseY);
        final Layer layer = Layer.BACKGROUND;

        drawSlot(drawContext, 8, 72, layer);

        drawSlot(drawContext, 30, 20, layer);
        drawSlot(drawContext, 48, 20, layer);
        drawSlot(drawContext, 66, 20, layer);
        drawSlot(drawContext, 84, 20, layer);

        drawSlot(drawContext, 121, 20, layer);
        drawSlot(drawContext, 139, 20, layer);

        // upgrades
        drawContext.drawTexture(RenderLayer::getGuiTextured, defaultTextureSheet, x - 48, y + 24, 0, 0, 27, 46, 256, 256);

        drawOutputSlotBar(drawContext, 54, 65, 5, layer);
    }

    @Override
    protected void drawForeground(DrawContext drawContext, final int mouseX, final int mouseY) {
        final Layer layer = Layer.FOREGROUND;
        final DisplayState displayState = blockEntity.getDisplayState();

        mineAllButton.visible = blockEntity.getMineAll() && QuarryMachineConfig.quarryAccessibleExcavationModes >= 3;
        mineOresButton.visible = !blockEntity.getMineAll() && QuarryMachineConfig.quarryAccessibleExcavationModes >= 3;

        if (displayState != DisplayState.Off && displayState != DisplayState.Mining) {
                if (displayState == DisplayState.Error) {
                drawContext.drawTexture(RenderLayer::getGuiTextured, defaultTextureSheet, 86, 42, 28, 0, 15, 16, 256, 256);
            } else {
                drawContext.drawTexture(RenderLayer::getGuiTextured, defaultTextureSheet, 86, 42, 44, 0, 15, 15, 256, 256);
            }
        }

        if (blockEntity.getMineAll()) {
            builder.drawDefaultBackground(drawContext, 28, 25, 77, 6);
        }

        super.drawForeground(drawContext, mouseX, mouseY);

        builder.drawProgressBar(drawContext, this, blockEntity.getProgressScaled(100), 100, 33, 65, mouseX, mouseY,
            GuiBuilder.ProgressDirection.UP, layer);
        builder.drawMultiEnergyBar(drawContext, this, 9, 19, (int) blockEntity.getEnergy(),
            (int) blockEntity.getMaxStoredPower(), mouseX, mouseY, 0, layer);
    }

    public void changeMineAll(boolean mineAll) {
        ClientPlayNetworking.send(QuarryManagerServerPacket.createPacketQuarryMineAll(blockEntity, mineAll));
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext drawContext, int mouseX, int mouseY) {
        final DisplayState displayState = blockEntity.getDisplayState();

        if (isPointWithinBounds(28, 18, 80, 19, mouseX, mouseY)
            && this.focusedSlot != null
            && !this.focusedSlot.hasStack()) {
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("gui.quarrymod.quarry.filler_blocks"), mouseX, mouseY);
        }

        if (isPointWithinBounds(118, 18, 38, 19, mouseX, mouseY)
            && this.focusedSlot != null
            && !this.focusedSlot.hasStack()) {
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("gui.quarrymod.quarry.drill_tubes"), mouseX, mouseY);
        }

        if (isPointWithinBounds(-42, 30, 19, 38, mouseX, mouseY)
            && this.focusedSlot != null
            && !this.focusedSlot.hasStack()) {
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("gui.quarrymod.quarry.drill_upgrades"), mouseX, mouseY);
        }

        if (isPointWithinBounds(86, 42, 15, 16, mouseX, mouseY)
            && displayState != DisplayState.Off && displayState != DisplayState.Mining) {
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer,
                Text.translatable(
                    "gui.quarrymod.quarry.state_" + blockEntity.getStateName().toLowerCase()).formatted(
                    displayState.getFormatting()),
                mouseX, mouseY);
        }

        super.drawMouseoverTooltip(drawContext, mouseX, mouseY);
    }
}
