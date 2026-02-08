package me.imgalvin.Mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(MinecraftServer.class)
public abstract class PublishLANServerMixin {
    @Shadow
    public abstract boolean publishServer(@Nullable GameType gameMode, boolean allowCommands, int port);

    @Shadow
    @Final
    private static Logger LOGGER;
    // Scheduler to check for connection readiness without blocking the main thread
    @Unique
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    // Inject at the end of loadLevel
    @Inject(at = @At("TAIL"), method = "loadLevel")
    private void init(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        attemptLanPublish(server);
    }

    @Unique
    private void attemptLanPublish(MinecraftServer server) {
        // Schedule a check every 500ms
        SCHEDULER.scheduleAtFixedRate(() -> {
            // Check if the connection is ready
            if (Minecraft.getInstance().getConnection() != null) {
                // Back to the main server thread for the actual logic
                server.execute(() -> {
                    int port = HttpUtil.getAvailablePort();
                    boolean success = publishServer(GameType.CREATIVE, true, port);

                    if (success) {
                        server.getPlayerList().broadcastSystemMessage(
                                Component.literal("§aOpened to LAN on port " + port), false);
                        LOGGER.info("Successfully opened to LAN on port {}", port);
                    } else {
                        server.getPlayerList().broadcastSystemMessage(
                                Component.literal("§cFailed to open to LAN!"), false);
                        LOGGER.error("Failed to open to LAN on port {}", port);
                    }
                });

                // End scheduler
                throw new RuntimeException("attemptLanPublish Complete");
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
}