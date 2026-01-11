package me.imgalvin.autolan.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.NetworkUtils.findLocalPort;

@Mixin(ClientPlayNetworkHandler.class)
public class AutoLanMixin {

    @Unique
    private boolean autolan$opened = false;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void autolan$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (autolan$opened) return;

        MinecraftClient client = MinecraftClient.getInstance();
        IntegratedServer server = client.getServer();

        if (server == null || !server.isSingleplayer() || client.player == null) {
            return;
        }

        // Get mode of the world
        GameMode gameMode = null;
        try {
            gameMode = server.getSaveProperties().getGameMode();
        } catch (Exception e) {
            try {
                client.player.sendMessage(
                        Text.literal("Failed to get game mode: " + e.getMessage()).formatted(net.minecraft.util.Formatting.RED),
                        false
                );
            } catch (Throwable ignored) {
            }
        }

        int port = findLocalPort();
        try {
            server.openToLan(gameMode, true, port);
            autolan$opened = true;
            client.player.sendMessage(
                    Text.literal("Opened to LAN on port " + port + " in mode: " + gameMode + ". Note: LAN worlds automatically open with cheats enabled. This will be addressed in a future update.").formatted(net.minecraft.util.Formatting.GREEN),
                    false
            );
        } catch (Exception e) {
            try {
                client.player.sendMessage(
                        Text.literal("Failed to open to LAN: " + e.getMessage()).formatted(net.minecraft.util.Formatting.RED),
                        false
                );
            } catch (Throwable ignored) {
            }
        }
    }
}
