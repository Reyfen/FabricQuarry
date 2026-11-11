package net.quarrymod.gametest;

import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.quarrymod.client.gui.QuarryScreen;
import net.quarrymod.init.QuarryManagerContent;

public class QuarryClientGameTest implements FabricClientGameTest {

    private static final int SERVER_START_TIMEOUT = 20 * 300;
    private static final int TIMEOUT = 20 * 60;
    private static final BlockPos QUARRY_POS = new BlockPos(0, -60, 3);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (DedicatedServerProcess server = DedicatedServerProcess.start(Path.of("dedicated-server"))) {
            context.waitFor(client -> server.isReady() || !server.isAlive(), SERVER_START_TIMEOUT);
            assertTrue(server.isReady(), "Dedicated server did not start, see dedicated-server/server-output.log");

            context.runOnClient(client -> ConnectScreen.startConnecting(client.gui.screen(), client,
                ServerAddress.parseString(server.address()),
                new ServerData("Quarry test server", server.address(), ServerData.Type.OTHER), false, null));
            context.waitFor(client -> inWorld(client) || isDisconnected(client), TIMEOUT);
            assertConnected(context, server, "joining");

            String player = context.computeOnClient(client -> client.getUser().getName());
            server.command("gamemode creative " + player);
            server.command("tp " + player + " 0.5 -60 0.5 0 0");
            server.command("setblock " + QUARRY_POS.getX() + " " + QUARRY_POS.getY() + " " + QUARRY_POS.getZ()
                + " quarrymod:quarry");
            context.waitFor(client -> isDisconnected(client) || inWorld(client)
                && client.level.getBlockState(QUARRY_POS).is(QuarryManagerContent.Machine.QUARRY.block)
                && client.player.blockPosition().getZ() == 0, TIMEOUT);
            assertConnected(context, server, "placing the quarry");

            context.waitFor(client -> client.gui.screen() == null || isDisconnected(client), TIMEOUT);
            assertConnected(context, server, "loading terrain");

            context.runOnClient(client -> client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(QUARRY_POS), Direction.NORTH, QUARRY_POS, false)));
            context.waitFor(client -> client.gui.screen() instanceof QuarryScreen || isDisconnected(client)
                || !server.isAlive(), TIMEOUT);
            assertConnected(context, server, "opening the quarry");
            context.waitTicks(20);
            assertConnected(context, server, "showing the quarry screen");
            assertTrue(context.computeOnClient(client -> client.gui.screen() instanceof QuarryScreen),
                "Quarry screen is not open");
            context.takeScreenshot("quarry_screen_dedicated_server");

            context.runOnClient(client -> {
                client.gui.screen().onClose();
                client.level.disconnect(Component.literal("Disconnecting"));
                client.disconnectWithSavingScreen();
            });
            context.waitFor(client -> client.level == null);
            context.setScreen(TitleScreen::new);
        }
    }

    private static boolean inWorld(Minecraft client) {
        return client.level != null && client.player != null;
    }

    private static boolean isDisconnected(Minecraft client) {
        return client.gui.screen() instanceof DisconnectedScreen;
    }

    private static void assertConnected(ClientGameTestContext context, DedicatedServerProcess server, String step) {
        String disconnected = context.computeOnClient(client ->
            client.gui.screen() instanceof DisconnectedScreen screen ? screen.getTitle().getString() : null);
        assertTrue(disconnected == null, "Disconnected while " + step + ": " + disconnected);
        assertTrue(server.isAlive(), "Dedicated server stopped while " + step
            + ", see dedicated-server/server-output.log");
        assertTrue(context.computeOnClient(QuarryClientGameTest::inWorld), "Not in a world while " + step);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
