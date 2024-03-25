package com.worlds;

import java.io.File;
import java.util.HashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

import com.mojang.brigadier.CommandDispatcher;

import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.Fantasy;

import com.worlds.commands.*;

public class WorldsModInitializer implements ModInitializer {
	public static Boolean initialized = false;

	public static final String public_cmd_prefix = "worlds";
	public static final String admin_cmd_prefix = "aworlds";

	public static final Logger LOGGER = LoggerFactory.getLogger("worlds");

	public static MinecraftServer server_instance;

	public static Fantasy fantasy;

	public static HashMap<Identifier, RuntimeWorldHandle> worlds_handlers = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing worlds mod...");

		// register configs
		ModConfigs.initialize();

		// when server starts set server_instance
		ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
			server_instance = server;
			fantasy = Fantasy.get(server);
		});

		ServerWorldEvents.LOAD.register((server, world) -> {
			if (initialized) {
				return;
			}

			if (ModConfigs.dimensions_dir.exists()) {
				for (File dimension_dir : ModConfigs.dimensions_dir.listFiles()) {
					for (File dimension_file : dimension_dir.listFiles()) {
						String id = dimension_dir.getName() + ":" + dimension_file.getName().replace(".json", "");

						if (id.startsWith("minecraft:")) {
							continue;
						}

						Handlers.regenerateDimensionsFromFile(server, dimension_file);
					}
				}
			}

			initialized = true;
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