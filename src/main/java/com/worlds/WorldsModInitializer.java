package com.worlds;

import java.io.File;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.mojang.brigadier.CommandDispatcher;

import com.worlds.commands.*;

public class WorldsModInitializer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("worlds");

	public static String public_cmd_prefix = "worlds";
	public static String admin_cmd_prefix = "aworlds";

	public static MinecraftServer server_instance;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing worlds mod...");

		// register configs
		ModConfigs.initialize();

		// when server starts set server_instance
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			server_instance = server;

			// try to read configs
			if (ModConfigs.dimensions_dir.exists()) {
				for (File dimension_dir : ModConfigs.dimensions_dir.listFiles()) {
					for (File dimension_file : dimension_dir.listFiles()) {
						String id = dimension_dir.getName() + ":" + dimension_file.getName().replace(".json", "");
						System.out.println("Found saved world " + id);

						Handlers.regenerateDimensionsFromFile(server, dimension_file);
					}
				}
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			WorldsModInitializer.registerCommands(dispatcher);
		});
	}

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		new PublicCommands(dispatcher);
		new AdminCommands(dispatcher);
	}

	public static Text text_plain(String txt) {
		return Text.of(txt);
	}
}