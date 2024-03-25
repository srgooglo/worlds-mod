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

public class AdminCommands {
    public AdminCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(WorldsModInitializer.admin_cmd_prefix)
                        .requires((s) -> s.hasPermissionLevel(1))
                        .then(argument("message", greedyString()).executes((ctx) -> {
                            return handleCommand(getString(ctx, "message").split(" "), ctx.getSource());
                        })));
    }

    private int handleCommand(String[] args, ServerCommandSource source) {
        if (args[0].equalsIgnoreCase("create")) {
            return Handlers.CMD_CreateNewWorld(WorldsModInitializer.server_instance, source, args);
        }

        if (args[0].equalsIgnoreCase("delete")) {
            return Handlers.CMD_DeleteWorld(WorldsModInitializer.server_instance, source, args);
        }

        if (args[0].equalsIgnoreCase("setWorldSpawn")) {
            return Handlers.CMD_SetWorldSpawn(WorldsModInitializer.server_instance, source);
        }

        source.sendMessage(WorldsModInitializer.text_plain("Unknown command: " + args[0]));

        return Command.SINGLE_SUCCESS;
    }
}
