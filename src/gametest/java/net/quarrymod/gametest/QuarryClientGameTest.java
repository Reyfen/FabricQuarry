package net.quarrymod.gametest;

import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

            context.runOnClient(client -> ConnectScreen.connect(client.currentScreen, client,
                ServerAddress.parse(server.address()),
                new ServerInfo("Quarry test server", server.address(), ServerInfo.ServerType.OTHER), false, null));
            context.waitFor(client -> inWorld(client) || isDisconnected(client), TIMEOUT);
            assertConnected(context, server, "joining");

            String player = context.computeOnClient(client -> client.getSession().getUsername());
            server.command("gamemode creative " + player);
            server.command("tp " + player + " 0.5 -60 0.5 0 0");
            server.command("setblock " + QUARRY_POS.getX() + " " + QUARRY_POS.getY() + " " + QUARRY_POS.getZ()
                + " quarrymod:quarry");
            context.waitFor(client -> isDisconnected(client) || inWorld(client)
                && client.world.getBlockState(QUARRY_POS).isOf(QuarryManagerContent.Machine.QUARRY.block)
                && client.player.getBlockPos().getZ() == 0, TIMEOUT);
            assertConnected(context, server, "placing the quarry");

            context.waitFor(client -> client.currentScreen == null || isDisconnected(client), TIMEOUT);
            assertConnected(context, server, "loading terrain");

            context.runOnClient(client -> client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(QUARRY_POS), Direction.NORTH, QUARRY_POS, false)));
            context.waitFor(client -> client.currentScreen instanceof QuarryScreen || isDisconnected(client)
                || !server.isAlive(), TIMEOUT);
            assertConnected(context, server, "opening the quarry");
            context.waitTicks(20);
            assertConnected(context, server, "showing the quarry screen");
            assertTrue(context.computeOnClient(client -> client.currentScreen instanceof QuarryScreen),
                "Quarry screen is not open");
            context.takeScreenshot("quarry_screen_dedicated_server");

            context.runOnClient(client -> {
                client.currentScreen.close();
                client.world.disconnect();
                client.disconnect();
            });
            context.waitFor(client -> client.world == null);
            context.setScreen(TitleScreen::new);
        }
    }

    private static boolean inWorld(MinecraftClient client) {
        return client.world != null && client.player != null;
    }

    private static boolean isDisconnected(MinecraftClient client) {
        return client.currentScreen instanceof DisconnectedScreen;
    }

    private static void assertConnected(ClientGameTestContext context, DedicatedServerProcess server, String step) {
        String disconnected = context.computeOnClient(client ->
            client.currentScreen instanceof DisconnectedScreen screen ? screen.getTitle().getString() : null);
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
