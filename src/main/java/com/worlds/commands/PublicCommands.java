package com.worlds.commands;

import com.worlds.Handlers;
import com.worlds.WorldsModInitializer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PublicCommands {
    public PublicCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(WorldsModInitializer.public_cmd_prefix)
                        .then(argument("message", greedyString()).executes((ctx) -> {
                            return handleCommand(getString(ctx, "message").split(" "), ctx.getSource());
                        })));
    }

    private int handleCommand(String[] args, ServerCommandSource source) {
        if (args[0].equalsIgnoreCase("list")) {
            return Handlers.CMD_ListWorlds(WorldsModInitializer.server_instance, source);
        }

        if (args[0].equalsIgnoreCase("tp")) {
            return Handlers.CMD_PlayerTeleport(WorldsModInitializer.server_instance, source, args);
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            return Handlers.CMD_TeleportToSpawn(WorldsModInitializer.server_instance, source);
        }

        if (args[0].equalsIgnoreCase("current")) {
            return Handlers.CMD_SendCurrentPlayerWorld(WorldsModInitializer.server_instance, source);
        }

        source.sendMessage(WorldsModInitializer.text_plain("Unknown command: " + args[0]));

        return Command.SINGLE_SUCCESS;
    }
}
