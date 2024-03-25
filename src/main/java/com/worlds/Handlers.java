package com.worlds;

import java.io.File;
import java.util.Random;

import com.mojang.brigadier.Command;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
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

import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;

import static com.worlds.WorldsModInitializer.text_plain;
import com.worlds.config.ConfigurationFile;

public class Handlers {
    public static int CMD_TeleportToSpawn(MinecraftServer mc_server, ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        Identifier currentWorldID = Utils.getCurrentPlayerWorldID(mc_server, source);

        if (currentWorldID == null) {
            player.sendMessage(text_plain("Cannot find current world"), false);
            return Command.SINGLE_SUCCESS;
        }

        return CMD_PlayerTeleport(mc_server, source, new String[] { "tp", currentWorldID.toString() });
    }

    public static int CMD_PlayerTeleport(MinecraftServer mc_server, ServerCommandSource source, String[] args) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        String world_id_string = args[1];

        if (world_id_string.indexOf(":") == -1) {
            world_id_string = "worlds_dimensions:" + world_id_string;
        }

        Identifier worldID = new Identifier(world_id_string);

        ServerWorld targetWorld = Utils.getServerWorldByID(worldID);

        if (targetWorld == null) {
            player.sendMessage(text_plain("Cannot find world: " + worldID.toString()), false);

            return Command.SINGLE_SUCCESS;
        }

        BlockPos spawnPoint = null;

        String namespace = world_id_string.split(":")[0];
        String path = world_id_string.split(":")[1];

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

        WorldsModInitializer.LOGGER
                .info("Player " + player.getGameProfile().getName() + " is traveling to " + worldID.toString());
        player.sendMessage(text_plain("Traveling to " + worldID.toString()), false);

        if (savedGamemode != null) {
            ServerPlayerInteractionManager serverPlayerInteractionManager = new ServerPlayerInteractionManager(player);
            GameMode targetGameMode = Utils.getGameModeFromString(savedGamemode);

            WorldsModInitializer.LOGGER.info("Using saved default gamemode " + savedGamemode);

            serverPlayerInteractionManager.changeGameMode(targetGameMode);
        }

        FabricDimensions.teleport(player, targetWorld, teleportTarget);

        return Command.SINGLE_SUCCESS;
    }

    public static int CMD_ListWorlds(MinecraftServer mc_server, ServerCommandSource source) {
        source.sendMessage(text_plain("Avaliable Worlds:"));

        Utils.getServerWorlds().forEach((worldID, world) -> {
            source.sendMessage(text_plain("- " + worldID.toString()));
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int CMD_CreateNewWorld(MinecraftServer mc_server, ServerCommandSource source, String[] args) {
        if (args.length == 1 || args.length == 2) {
            source.sendMessage(text_plain("Missing arguments; Use <world_name> <type> <seed[optional]>"));
            return Command.SINGLE_SUCCESS;
        }

        String dimensionType = Utils.getDimensionStringFromShortString(args[2]);

        String world_id_string = args[1];
        ChunkGenerator gen = Utils.getWorldChunkGeneratorFromString(dimensionType);
        Identifier dim = Utils.getDimensionIdentifierFromString(dimensionType);
        long seed = 0;

        // if id is incomplete, add "worlds_dimensions:"
        if (world_id_string.indexOf(":") == -1) {
            world_id_string = "worlds_dimensions:" + world_id_string;
        }

        Identifier worldID = new Identifier(world_id_string);

        // If user not specified seed, generate a random one
        if (args[3] != null) {
            seed = Long.parseLong(args[3]);
        } else {
            seed = new Random().nextLong();
        }

        WorldsModInitializer.LOGGER.info("Creating new world with params >");

        WorldsModInitializer.LOGGER.info("ID: " + worldID);
        WorldsModInitializer.LOGGER.info("Seed: " + seed);
        WorldsModInitializer.LOGGER.info("Dimension: " + dim);
        WorldsModInitializer.LOGGER.info("Generator: " + gen);

        source.sendMessage(text_plain("Creating new world, please wait..."));

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setDifficulty(Difficulty.NORMAL)
                .setGenerator(gen)
                .setSeed(seed)
                .setShouldTickTime(true);

        RuntimeWorldHandle worldHandle = WorldsModInitializer.fantasy.getOrOpenPersistentWorld(worldID, config);

        WorldsModInitializer.worlds_handlers.put(worldID, worldHandle);

        saveNewConfig(worldHandle.asWorld(), dim, seed);

        WorldsModInitializer.LOGGER.info("World created and saved...");
        source.sendMessage(text_plain("World created !"));

        return Command.SINGLE_SUCCESS;
    }

    public static int CMD_DeleteWorld(MinecraftServer mc_server, ServerCommandSource source, String[] args) {
        if (args[1] == null) {
            source.sendMessage(text_plain("Missing arguments; Use <world_name>"));
            return Command.SINGLE_SUCCESS;
        }

        String namespace = args[1].split(":")[0];
        String path = args[1].split(":")[1];

        ConfigurationFile worldConfig = ModConfigs.getWorldConfig(namespace, path);

        if (worldConfig == null) {
            source.sendMessage(text_plain("World not found"));
            return Command.SINGLE_SUCCESS;
        }

        Identifier worldID = new Identifier(namespace, path);

        RuntimeWorldHandle worldHandle = WorldsModInitializer.worlds_handlers.get(worldID);

        if (worldHandle == null) {
            source.sendMessage(text_plain("World not found"));
            return Command.SINGLE_SUCCESS;
        }

        worldHandle.delete();

        ModConfigs.removeWorldConfig(namespace, path);

        WorldsModInitializer.worlds_handlers.remove(worldID);

        WorldsModInitializer.LOGGER.info("World [" + worldID + "] deleted...");
        source.sendMessage(text_plain("World deleted !"));

        return Command.SINGLE_SUCCESS;
    }

    public static int CMD_SetWorldSpawn(MinecraftServer mc_server, ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

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
        source.sendMessage(text_plain("World spawn updated to " + currentPos.toString() + ""));

        return Command.SINGLE_SUCCESS;
    }

    static public int CMD_SendCurrentPlayerWorld(MinecraftServer mc_server, ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        World currentWorld = Utils.getCurrentPlayerWorld(mc_server, source);
        BlockPos currentPos = Utils.getCurrentPlayerPos(mc_server, source);

        String namespace = currentWorld.getRegistryKey().getValue().getNamespace().toString();
        String path = currentWorld.getRegistryKey().getValue().getPath().toString();

        player.sendMessage(text_plain("Current world: " + namespace + ":" + path), false);
        player.sendMessage(text_plain("Current position: " + currentPos.toString()), false);

        return Command.SINGLE_SUCCESS;
    }

    public static int regenerateDimensionsFromFile(MinecraftServer mc_server, File file) {
        ConfigurationFile worldConfig = new ConfigurationFile(file);

        Identifier worldID = new Identifier(worldConfig.get("namespace") + ":" + worldConfig.get("path"));

        String dimensionType = worldConfig.get("environment");
        Long seed = Long.parseLong(worldConfig.get("seed"));
        Difficulty difficulty = Utils.getDifficultyFromString(worldConfig.get("difficulty"));

        ChunkGenerator gen = Utils.getWorldChunkGeneratorFromString(dimensionType);
        Identifier dim = Utils.getDimensionIdentifierFromString(dimensionType);

        if (gen == null || dim == null) {
            WorldsModInitializer.LOGGER.warn("Failed to regenerate world dimension [" + worldID
                    + "] from file. Bad environment type > " + dimensionType);
            return 0;
        }

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setSeed(seed)
                .setDimensionType(dim_of(dim))
                .setDifficulty(difficulty)
                .setGenerator(gen)
                .setSeed(seed)
                .setShouldTickTime(true);

        WorldsModInitializer.LOGGER.info("Regenerating dimension [" + worldID + "] from file...");
        WorldsModInitializer.LOGGER.info("Environment: " + dimensionType);
        WorldsModInitializer.LOGGER.info("Seed: " + config.getSeed());
        WorldsModInitializer.LOGGER.info("Difficulty: " + config.getDifficulty().toString());

        RuntimeWorldHandle handler = WorldsModInitializer.fantasy.getOrOpenPersistentWorld(worldID, config);

        WorldsModInitializer.worlds_handlers.put(worldID, handler);

        WorldsModInitializer.LOGGER.info("Dimension [" + worldID + "] regenerated from file.");

        return 0;
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
