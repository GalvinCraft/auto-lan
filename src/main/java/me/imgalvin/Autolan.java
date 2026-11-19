package me.imgalvin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Autolan implements ClientModInitializer {
	public static final String MOD_ID = "AutoLan";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("AutoLan initialized!");
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			MinecraftServer server = client.getSingleplayerServer();
			if (server == null) return;

			server.execute(() -> {
				int port = HttpUtil.getAvailablePort();
				if (server.publishServer(MinecraftServer.MultiplayerScope.LAN, true, port)) {
					server.getPlayerList().broadcastSystemMessage(
							Component.literal("Opened to LAN on port " + port).withStyle(ChatFormatting.GREEN), false);
					LOGGER.info("Successfully opened to LAN on port {}", port);
				} else {
					server.getPlayerList().broadcastSystemMessage(
							Component.literal("Failed to open to LAN!").withStyle(ChatFormatting.RED), false);
					LOGGER.error("Failed to open to LAN on port {}", port);
				}
			});
		});
	}
}
