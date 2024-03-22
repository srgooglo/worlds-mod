package com.worlds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.mojang.brigadier.Command;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;

import static com.worlds.WorldsModInitializer.text_plain;
import com.worlds.config.ConfigurationFile;

public class Handlers {
    public static int tp(MinecraftServer mc_server, ServerPlayerEntity player, String[] args) {
        HashMap<String, ServerWorld> worlds = new HashMap<>();

        mc_server.getWorldRegistryKeys().forEach((r) -> {
            ServerWorld world = mc_server.getWorld(r);
            worlds.put(r.getValue().toString(), world);
        });

        String worldID = args[1];

        if (worldID.indexOf(":") == -1) {
            worldID = "worlds_dimensions:" + worldID;
        }

        if (!worlds.containsKey(worldID)) {
            player.sendMessage(text_plain("Cannot find world: " + worldID), false);

            return Command.SINGLE_SUCCESS;
        }

        ServerWorld targetWorld = worlds.get(worldID);
        BlockPos spawnPoint = null;

        String namespace = worldID.split(":")[0];
        String path = worldID.split(":")[1];

        ConfigurationFile worldConfig = ModConfigs.getWorldConfig(namespace, path);

        String savedSpawnPoint = worldConfig.get("spawnpoint");
        String savedGamemode = worldConfig.get("gamemode");

        if (savedSpawnPoint != null) {
            spawnPoint = BlockPos.fromLong(Long.parseLong(savedSpawnPoint));
        } else {
            spawnPoint = Spawn.getDefaultWorldPos(targetWorld);
        }

        spawnPoint = Spawn.findSafePos(targetWorld, spawnPoint);

        TeleportTarget teleportTarget = new TeleportTarget(
                new Vec3d(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ()),
                new Vec3d(1, 1, 1),
                0f,
                0f);

        WorldsModInitializer.LOGGER.info("Player " + player.getGameProfile().getName() + " is traveling to " + worldID);
        player.sendMessage(text_plain("Traveling to " + worldID), false);

        if (savedGamemode != null) {
            ServerPlayerInteractionManager serverPlayerInteractionManager = new ServerPlayerInteractionManager(player);
            GameMode targetGameMode = null;

            if (savedGamemode.equalsIgnoreCase("survival")) {
                WorldsModInitializer.LOGGER.info("Using saved default gamemode 'survival'");
                targetGameMode = GameMode.SURVIVAL;
            }

            if (savedGamemode.equalsIgnoreCase("creative")) {
                WorldsModInitializer.LOGGER.info("Using saved default gamemode 'creative'");
                targetGameMode = GameMode.CREATIVE;
            }

            if (savedGamemode.equalsIgnoreCase("adventure")) {
                WorldsModInitializer.LOGGER.info("Using saved default gamemode 'adventure'");
                targetGameMode = GameMode.ADVENTURE;
            }

            serverPlayerInteractionManager.changeGameMode(targetGameMode);
        }

        FabricDimensions.teleport(player, targetWorld, teleportTarget);

        return Command.SINGLE_SUCCESS;
    }

    public static int list(MinecraftServer mc_server, ServerPlayerEntity player) {
        mc_server.getWorlds().forEach((world) -> {
            String name = world.getRegistryKey().getValue().toString();

            player.sendMessage(text_plain("- " + name), false);
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int createNew(MinecraftServer mc_server, ServerPlayerEntity player, String[] args) {
        if (args.length == 1 || args.length == 2) {
            player.sendMessage(text_plain("Missing arguments; Use <world_name> <type> <seed[optional]>"), false);
            return Command.SINGLE_SUCCESS;
        }

        String dimensionType = Utils.getDimensionStringFromShortString(args[2]);

        String worldID = args[1];
        ChunkGenerator gen = Utils.getWorldChunkGeneratorFromString(dimensionType);
        Identifier dim = Utils.getDimensionIdentifierFromString(dimensionType);
        long seed = 0;

        // if id is incomplete, add "worlds_dimensions:"
        if (worldID.indexOf(":") == -1) {
            worldID = "worlds_dimensions:" + worldID;
        }

        // If user not specified seed, generate a random one
        if (args[3] != null) {
            seed = Long.parseLong(args[3]);
        } else {
            seed = new Random().nextLong();
        }

        WorldsModInitializer.LOGGER
                .info("Creating new world with params > worldID:" + worldID + " type:" + args[2] + " seed:" + seed);
        player.sendMessage(text_plain("Creating new world, please wait..."), false);

        Fantasy fantasy = Fantasy.get(mc_server);

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setDifficulty(Difficulty.NORMAL)
                .setGenerator(gen)
                .setSeed(seed)
                .setShouldTickTime(true);

        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(worldID), config);

        ServerWorld world = worldHandle.asWorld();

        saveNewConfig(world, dim, seed);

        WorldsModInitializer.LOGGER.info("World created and saved...");
        player.sendMessage(text_plain("World created !"), false);

        return Command.SINGLE_SUCCESS;
    }

    public static int setWorldSpawn(MinecraftServer mc_server, ServerPlayerEntity player) {
        World currentWorld = player.getWorld();
        BlockPos currentPos = player.getBlockPos();

        Identifier registryValue = currentWorld.getRegistryKey().getValue();

        String namespace = registryValue.getNamespace().toString();
        String path = registryValue.getPath().toString();

        ConfigurationFile worldConfig = ModConfigs.getWorldConfig(namespace, path);

        Long positionAsLong = currentPos.asLong();

        worldConfig.set("spawnpoint", Long.toString(positionAsLong));
        worldConfig.save();

        WorldsModInitializer.LOGGER.info("World spawn updated to " + currentPos.toString() + "");
        player.sendMessage(text_plain("World spawn updated to " + currentPos.toString() + ""), false);

        return Command.SINGLE_SUCCESS;
    }

    public static int regenerateDimensionsFromFile(MinecraftServer mc_server, File file) {
        ConfigurationFile worldConfig = new ConfigurationFile(file);

        Identifier worldID = new Identifier(worldConfig.get("namespace") + ":" + worldConfig.get("path"));

        String dimensionType = worldConfig.get("environment");
        long seed = Long.parseLong(worldConfig.get("seed"));
        Difficulty difficulty = Utils.getDifficultyFromString(worldConfig.get("difficulty"));

        ChunkGenerator gen = Utils.getWorldChunkGeneratorFromString(dimensionType);
        Identifier dim = Utils.getDimensionIdentifierFromString(dimensionType);

        if (gen == null || dim == null) {
            WorldsModInitializer.LOGGER.warn("Failed to regenerate world dimension [" + worldID
                    + "] from file. Bad environment type > " + dimensionType);
            return 0;
        }

        Fantasy fantasy = Fantasy.get(mc_server);

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setDifficulty(difficulty)
                .setGenerator(gen)
                .setSeed(seed)
                .setShouldTickTime(true);

        fantasy.getOrOpenPersistentWorld(worldID, config);

        WorldsModInitializer.LOGGER.info("Dimension [" + worldID + "] regenerated from file.");

        return 0;
    }

    static public int sendCurrentPlayerWorld(MinecraftServer mc_server, ServerPlayerEntity player) {
        World currentWorld = player.getWorld();
        BlockPos currentPos = player.getBlockPos();

        String namespace = currentWorld.getRegistryKey().getValue().getNamespace().toString();
        String path = currentWorld.getRegistryKey().getValue().getPath().toString();

        player.sendMessage(text_plain("Current world: " + namespace + ":" + path), false);
        player.sendMessage(text_plain("Current position: " + currentPos.toString()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static ConfigurationFile saveNewConfig(ServerWorld world, Identifier dim, long seed) {
        Identifier id = world.getRegistryKey().getValue();

        String namespace = id.getNamespace();
        String path = id.getPath();

        ConfigurationFile worldConfig = ModConfigs.getWorldConfig(namespace, path);

        worldConfig.set("namespace", namespace);
        worldConfig.set("path", path);
        worldConfig.set("environment", dim.toString());
        worldConfig.set("seed", Long.toString(seed));

        worldConfig.save();

        return worldConfig;
    }

    private static RegistryKey<DimensionType> dim_of(Identifier id) {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id);
    }
}
