package me.imgalvin.autolan;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import static net.minecraft.util.NetworkUtils.findLocalPort;

public class Autolan implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		System.out.println("Hi");
	}
}
