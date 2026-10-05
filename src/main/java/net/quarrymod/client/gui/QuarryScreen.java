package net.quarrymod.client.gui;

import net.quarrymod.QuarryMod;
import net.quarrymod.block.QuarryBlock.DisplayState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.config.QuarryMachineConfig;
import net.quarrymod.packets.QuarryManagerServerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import reborncore.client.gui.GuiBase;
import reborncore.client.gui.GuiBuilder;
import reborncore.client.gui.widget.GuiButtonExtended;
import reborncore.common.screen.BuiltScreenHandler;

public class QuarryScreen extends GuiBase<BuiltScreenHandler> {

    public static final Identifier defaultTextureSheet = Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID,
        "textures/gui/guielements.png");

    private final QuarryBlockEntity blockEntity;
    private GuiButtonExtended mineAllButton;
    private GuiButtonExtended mineOresButton;

    public QuarryScreen(int syncID, final Player player, final QuarryBlockEntity blockEntity) {
        super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
        this.blockEntity = blockEntity;
    }

    @Override
    public void init() {
        super.init();
        mineAllButton = addRenderableWidget(
            new GuiButtonExtended(leftPos + 29, topPos + 39, 54, 20, Component.translatable("gui.quarrymod.quarry.mine_all"),
                (Button buttonWidget) -> changeMineAll(false)));
        mineOresButton = addRenderableWidget(
            new GuiButtonExtended(leftPos + 29, topPos + 39, 54, 20, Component.translatable("gui.quarrymod.quarry.mine_ores"),
                (Button buttonWidget) -> changeMineAll(true)));
        mineAllButton.visible = false;
        mineOresButton.visible = false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor drawContext, final int mouseX, final int mouseY, final float f) {
        super.extractBackground(drawContext, mouseX, mouseY, f);
        final Layer layer = Layer.BACKGROUND;

        drawSlot(drawContext, 8, 72, layer);

        drawSlot(drawContext, 30, 20, layer);
        drawSlot(drawContext, 48, 20, layer);
        drawSlot(drawContext, 66, 20, layer);
        drawSlot(drawContext, 84, 20, layer);

        drawSlot(drawContext, 121, 20, layer);
        drawSlot(drawContext, 139, 20, layer);

        // upgrades
        drawContext.blit(RenderPipelines.GUI_TEXTURED, defaultTextureSheet, leftPos - 48, topPos + 24, 0, 0, 27, 46, 256, 256);

        drawOutputSlotBar(drawContext, 54, 65, 5, layer);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor drawContext, final int mouseX, final int mouseY) {
        final Layer layer = Layer.FOREGROUND;
        final DisplayState displayState = blockEntity.getDisplayState();

        mineAllButton.visible = blockEntity.getMineAll() && QuarryMachineConfig.quarryAccessibleExcavationModes >= 3;
        mineOresButton.visible = !blockEntity.getMineAll() && QuarryMachineConfig.quarryAccessibleExcavationModes >= 3;

        if (displayState != DisplayState.Off && displayState != DisplayState.Mining) {
                if (displayState == DisplayState.Error) {
                drawContext.blit(RenderPipelines.GUI_TEXTURED, defaultTextureSheet, 86, 42, 28, 0, 15, 16, 256, 256);
            } else {
                drawContext.blit(RenderPipelines.GUI_TEXTURED, defaultTextureSheet, 86, 42, 44, 0, 15, 15, 256, 256);
            }
        }

        if (blockEntity.getMineAll()) {
            builder.drawDefaultBackground(drawContext, 28, 25, 77, 6);
        }

        super.extractLabels(drawContext, mouseX, mouseY);

        builder.drawProgressBar(drawContext, this, blockEntity.getProgressScaled(100), 100, 33, 65, mouseX, mouseY,
            GuiBuilder.ProgressDirection.UP, layer);
        builder.drawMultiEnergyBar(drawContext, this, 9, 19, (int) blockEntity.getEnergy(),
            (int) blockEntity.getMaxStoredPower(), mouseX, mouseY, 0, layer);
    }

    public void changeMineAll(boolean mineAll) {
        ClientPlayNetworking.send(QuarryManagerServerPacket.createPacketQuarryMineAll(blockEntity, mineAll));
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor drawContext, int mouseX, int mouseY) {
        final DisplayState displayState = blockEntity.getDisplayState();

        if (isHovering(28, 18, 80, 19, mouseX, mouseY)
            && this.hoveredSlot != null
            && !this.hoveredSlot.hasItem()) {
            drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("gui.quarrymod.quarry.filler_blocks"), mouseX, mouseY);
        }

        if (isHovering(118, 18, 38, 19, mouseX, mouseY)
            && this.hoveredSlot != null
            && !this.hoveredSlot.hasItem()) {
            drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("gui.quarrymod.quarry.drill_tubes"), mouseX, mouseY);
        }

        if (isHovering(-42, 30, 19, 38, mouseX, mouseY)
            && this.hoveredSlot != null
            && !this.hoveredSlot.hasItem()) {
            drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("gui.quarrymod.quarry.drill_upgrades"), mouseX, mouseY);
        }

        if (isHovering(86, 42, 15, 16, mouseX, mouseY)
            && displayState != DisplayState.Off && displayState != DisplayState.Mining) {
            drawContext.setTooltipForNextFrame(Minecraft.getInstance().font,
                Component.translatable(
                    "gui.quarrymod.quarry.state_" + blockEntity.getStateName().toLowerCase()).withStyle(
                    displayState.getFormatting()),
                mouseX, mouseY);
        }

        super.extractTooltip(drawContext, mouseX, mouseY);
    }
}
