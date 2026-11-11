package net.quarrymod.gametest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DedicatedServerProcess implements AutoCloseable {

    private final Process process;
    private final Writer console;
    private final int port;
    private volatile boolean ready;

    private DedicatedServerProcess(Process process, int port, Path log) throws IOException {
        this.process = process;
        this.port = port;
        this.console = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        Writer logWriter = Files.newBufferedWriter(log, StandardCharsets.UTF_8);
        Thread reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)); logWriter) {
                String line;
                while ((line = in.readLine()) != null) {
                    logWriter.write(line);
                    logWriter.write(System.lineSeparator());
                    logWriter.flush();
                    if (line.contains("Done (")) {
                        ready = true;
                    }
                }
            } catch (IOException ignored) {
            }
        }, "Quarry test server output");
        reader.setDaemon(true);
        reader.start();
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));
    }

    public static DedicatedServerProcess start(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(path);
                    }
                }
            }
            Files.createDirectories(dir);
            int port = freePort();
            Files.writeString(dir.resolve("eula.txt"), "eula=true\n");
            Properties properties = new Properties();
            properties.setProperty("online-mode", "false");
            properties.setProperty("server-port", String.valueOf(port));
            properties.setProperty("spawn-protection", "0");
            properties.setProperty("max-players", "1");
            properties.setProperty("level-type", "minecraft:flat");
            properties.setProperty("generate-structures", "false");
            properties.setProperty("spawn-monsters", "false");
            properties.setProperty("view-distance", "4");
            properties.setProperty("simulation-distance", "4");
            properties.setProperty("sync-chunk-writes", "false");
            try (Writer writer = Files.newBufferedWriter(dir.resolve("server.properties"))) {
                properties.store(writer, null);
            }

            String dliConfig = launchConfig().toString();
            List<String> args = List.of(
                "-Xmx2G",
                "-cp", System.getProperty("java.class.path"),
                "-Dfabric.dli.config=" + dliConfig,
                "-Dfabric.dli.env=server",
                "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotServer",
                "net.fabricmc.devlaunchinjector.Main",
                "nogui");
            Path argFile = dir.resolve("jvm-args.txt");
            Files.writeString(argFile, args.stream()
                .map(arg -> "\"" + arg.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(System.lineSeparator())));
            String java = ProcessHandle.current().info().command()
                .orElseThrow(() -> new IllegalStateException("Cannot find the java executable"));

            Process process = new ProcessBuilder(java, "@" + argFile.toAbsolutePath())
                .directory(dir.toFile())
                .redirectErrorStream(true)
                .start();
            return new DedicatedServerProcess(process, port, dir.resolve("server-output.log"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path launchConfig() {
        for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path config = dir.resolve(".gradle/loom-cache/launch.cfg");
            if (Files.isRegularFile(config)) {
                return config;
            }
        }
        throw new IllegalStateException("Cannot find .gradle/loom-cache/launch.cfg, run this test through Loom");
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public String address() {
        return "localhost:" + port;
    }

    public void command(String command) {
        try {
            console.write(command);
            console.write("\n");
            console.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        if (process.isAlive()) {
            try {
                command("stop");
                if (!process.waitFor(60, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (UncheckedIOException e) {
                process.destroyForcibly();
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }
}
