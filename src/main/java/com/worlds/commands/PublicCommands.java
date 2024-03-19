package com.worlds.commands;

import com.worlds.Handlers;
import com.worlds.WorldsModInitializer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PublicCommands {
    public PublicCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal(WorldsModInitializer.public_cmd_prefix)
                        // .requires((s) -> s.hasPermissionLevel(1))
                        .then(argument("message", greedyString()).executes((ctx) -> {
                            return handleCommand(getString(ctx, "message").split(" "), ctx.getSource().getPlayer());
                        })));
    }

    private int handleCommand(String[] args, ServerPlayerEntity player) {
        if (args[0].equalsIgnoreCase("list")) {
            return Handlers.list(WorldsModInitializer.server_instance, player);
        }

        if (args[0].equalsIgnoreCase("tp")) {
            return Handlers.tp(WorldsModInitializer.server_instance, player, args);
        }

        if (args[0].equalsIgnoreCase("current")) {
            return Handlers.sendCurrentPlayerWorld(WorldsModInitializer.server_instance, player);
        }

        player.sendMessage(WorldsModInitializer.text_plain("Unknown command: " + args[0]), false);

        return Command.SINGLE_SUCCESS;
    }
}
