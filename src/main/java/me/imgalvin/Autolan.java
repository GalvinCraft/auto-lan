package me.imgalvin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import static net.minecraft.util.NetworkUtils.findLocalPort;

public class Autolan implements ClientModInitializer {
	private boolean opened = false;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!opened && client.player != null && client.getServer() != null) {
				IntegratedServer server = client.getServer();

				if (server.isSingleplayer()) {
					int port = findLocalPort();
					server.openToLan(GameMode.CREATIVE, true, port);
					client.player.sendMessage(Text.of("§aOpened to LAN on port " + port), false);
					opened = true;
				}
			}
		});
	}
}
