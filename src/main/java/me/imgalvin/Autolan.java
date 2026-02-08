package me.imgalvin;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Autolan implements ClientModInitializer {
	public static final String MOD_ID = "AutoLan";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("AutoLan initialized!");
	}
}
