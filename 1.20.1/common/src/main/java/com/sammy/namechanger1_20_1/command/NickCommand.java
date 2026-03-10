package com.sammy.namechanger1_20_1.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.sammy.namechanger1_20_1.nickname.NicknameNetworking;
import com.sammy.namechanger1_20_1.nickname.NicknameService;

public final class NickCommand {
    public static final int MAX_NICKNAME_LENGTH = 32;

    private NickCommand() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, context, selection) ->
            dispatcher.register(
                Commands.literal("nick")
                    .then(Commands.literal("name")
                        .requires(NickCommand::isPlayer)
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                            .executes(commandContext -> setNickname(
                                commandContext.getSource(),
                                StringArgumentType.getString(commandContext, "name")
                            ))
                        )
                    )
                    .then(Commands.literal("reset")
                        .requires(NickCommand::isPlayer)
                        .executes(commandContext -> resetNickname(commandContext.getSource()))
                    )
                    .then(Commands.literal("hide")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("on")
                            .executes(commandContext -> setHideAllNames(commandContext.getSource(), true))
                        )
                        .then(Commands.literal("off")
                            .executes(commandContext -> setHideAllNames(commandContext.getSource(), false))
                        )
                    )
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .requires(NickCommand::isPlayer)
                        .executes(commandContext -> setNickname(
                            commandContext.getSource(),
                            StringArgumentType.getString(commandContext, "name")
                        ))
                    )
            )
        );
    }

    private static boolean isPlayer(CommandSourceStack source) {
        return source.getPlayer() != null;
    }

    private static int setNickname(CommandSourceStack source, String rawNickname) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String nickname = rawNickname.trim();

        if (nickname.isEmpty()) {
            source.sendFailure(Component.translatable("command.namechanger1_20_1.nick.error.empty"));
            return 0;
        }

        if (nickname.codePointCount(0, nickname.length()) > MAX_NICKNAME_LENGTH) {
            source.sendFailure(Component.translatable("command.namechanger1_20_1.nick.error.too_long", MAX_NICKNAME_LENGTH));
            return 0;
        }

        MinecraftServer server = source.getServer();
        NicknameService.setNickname(server, player.getUUID(), nickname);
        NicknameNetworking.syncAll(server);
        source.sendSuccess(() -> Component.translatable("command.namechanger1_20_1.nick.set.success", Component.literal(nickname)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetNickname(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();

        if (!NicknameService.hasNickname(server, player.getUUID())) {
            source.sendFailure(Component.translatable("command.namechanger1_20_1.nick.reset.error.none"));
            return 0;
        }

        NicknameService.resetNickname(server, player.getUUID());
        NicknameNetworking.syncAll(server);
        source.sendSuccess(() -> Component.translatable("command.namechanger1_20_1.nick.reset.success"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setHideAllNames(CommandSourceStack source, boolean enabled) {
        MinecraftServer server = source.getServer();
        NicknameService.setHideAllNames(server, enabled);
        NicknameNetworking.syncAll(server);
        source.sendSuccess(() -> Component.translatable(
            enabled ? "command.namechanger1_20_1.nick.hide.on" : "command.namechanger1_20_1.nick.hide.off"
        ), true);
        return Command.SINGLE_SUCCESS;
    }
}
