package me.imgalvin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import static net.minecraft.util.HttpUtil.getAvailablePort;

public class Autolan implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("Autolan initialized!");
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			System.out.println("Checking for player and server...");
//			if (client.player != null && client.getCurrentServer() != null) {
//				IntegratedServer server = client.getSingleplayerServer();
//
//				System.out.println("Checking for LAN...");
//                if (server == null) return;
//
//				System.out.println("Opening LAN server");
//                if (server.isSingleplayer() && !server.isPublished()) {
//					int port = getAvailablePort();
//					server.publishServer(GameType.CREATIVE, true, port);
//					client.player.displayClientMessage(Component.literal("§aOpened to LAN on port " + port), false);
//				}
//			}
//		});
	}
}
