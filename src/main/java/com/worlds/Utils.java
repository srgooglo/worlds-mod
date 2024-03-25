package com.worlds;

import java.util.HashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class Utils {
    // Dimension Ids
    public static final Identifier OVERWORLD_ID = new Identifier("overworld");
    public static final Identifier THE_NETHER_ID = new Identifier("the_nether");
    public static final Identifier THE_END_ID = new Identifier("the_end");

    public static Difficulty getDifficultyFromString(String difficulty) {
        if (difficulty == null) {
            return Difficulty.NORMAL;
        }

        return switch (difficulty.toLowerCase()) {
            case "normal" -> Difficulty.NORMAL;
            case "hard" -> Difficulty.HARD;
            case "easy" -> Difficulty.EASY;
            case "peaceful" -> Difficulty.PEACEFUL;
            default -> Difficulty.NORMAL;
        };
    }

    public static GameMode getGameModeFromString(String gamemode) {
        if (gamemode == null) {
            return GameMode.SURVIVAL;
        }

        return switch (gamemode.toLowerCase()) {
            case "survival" -> GameMode.SURVIVAL;
            case "creative" -> GameMode.CREATIVE;
            case "adventure" -> GameMode.ADVENTURE;
            case "spectator" -> GameMode.SPECTATOR;
            default -> GameMode.SURVIVAL;
        };
    }

    public static ChunkGenerator getWorldChunkGeneratorFromString(String generationType) {
        if (generationType == null) {
            return WorldsModInitializer.server_instance.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator();
        }

        return switch (generationType.toLowerCase()) {
            case "minecraft:overworld" ->
                WorldsModInitializer.server_instance.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator();
            case "minecraft:the_nether" ->
                WorldsModInitializer.server_instance.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
            case "minecraft:the_end" ->
                WorldsModInitializer.server_instance.getWorld(World.END).getChunkManager().getChunkGenerator();
            default ->
                WorldsModInitializer.server_instance.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator();
        };
    }

    public static Identifier getDimensionIdentifierFromString(String dimension) {
        if (dimension == null) {
            return OVERWORLD_ID;
        }

        return switch (dimension.toLowerCase()) {
            case "minecraft:overworld" -> OVERWORLD_ID;
            case "minecraft:the_nether" -> THE_NETHER_ID;
            case "minecraft:the_end" -> THE_END_ID;
            default -> OVERWORLD_ID;
        };
    }

    public static String getDimensionStringFromShortString(String type) {
        if (type == null) {
            return "normal";
        }

        return switch (type.toLowerCase()) {
            case "normal" -> "minecraft:overworld";
            case "nether" -> "minecraft:the_nether";
            case "end" -> "minecraft:the_end";
            default -> "normal";
        };
    }

    static public World getCurrentPlayerWorld(MinecraftServer mc_server, ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return null;
        }

        return player.getWorld();
    }

    static public BlockPos getCurrentPlayerPos(MinecraftServer mc_server, ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            return null;
        }

        return player.getBlockPos();
    }

    static public Identifier getCurrentPlayerWorldID(MinecraftServer mc_server, ServerCommandSource source) {
        World world = getCurrentPlayerWorld(mc_server, source);

        if (world == null) {
            return null;
        }

        return world.getRegistryKey().getValue();
    }

    static public HashMap<Identifier, ServerWorld> getServerWorlds() {
        HashMap<Identifier, ServerWorld> worlds = new HashMap<>();

        WorldsModInitializer.server_instance.getWorldRegistryKeys().forEach((r) -> {
            ServerWorld world = WorldsModInitializer.server_instance.getWorld(r);
            worlds.put(r.getValue(), world);
        });

        return worlds;
    }

    static public ServerWorld getServerWorldByID(Identifier id) {
        HashMap<Identifier, ServerWorld> worlds = getServerWorlds();

        return worlds.get(id);
    }
}