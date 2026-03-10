package name_changer;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Name_changer implements ModInitializer {

    private static final int MAX_NICK_LEN = 32;

    @Override
    public void onInitialize() {
        NickPayloads.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NickState st = NickState.get(server);
            NickSync.sendAllTo(player, st.getAllNicks(), st.isHideAll());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("nick")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.usage"), false);
                        return 1;
                    })
                    .then(CommandManager.argument("name", StringArgumentType.greedyString())
                            .executes(ctx -> setNickname(
                                    ctx.getSource().getPlayer(),
                                    ctx.getSource().getServer(),
                                    StringArgumentType.getString(ctx, "name"),
                                    ctx.getSource()::sendFeedback
                            )))
                    .then(CommandManager.literal("name")
                            .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(ctx -> setNickname(
                                            ctx.getSource().getPlayer(),
                                            ctx.getSource().getServer(),
                                            StringArgumentType.getString(ctx, "name"),
                                            ctx.getSource()::sendFeedback
                                    ))
                            )
                    )

                    .then(CommandManager.literal("reset")
                            .executes(ctx -> {
                                ServerPlayerEntity p = ctx.getSource().getPlayer();
                                MinecraftServer server = ctx.getSource().getServer();
                                NickState st = NickState.get(server);

                                if (st.getNick(p.getUuid()).isBlank()) {
                                    ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.nick_reset_none"), false);
                                    return 0;
                                }

                                st.setNick(p.getUuid(), "");
                                NickSync.broadcastNick(server, p.getUuid(), "");

                                ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.nick_reset"), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("hide")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("on").executes(ctx -> {
                                MinecraftServer server = ctx.getSource().getServer();
                                NickState st = NickState.get(server);

                                st.setHideAll(true);
                                NickSync.broadcastHideAll(server, true);

                                ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.hide_on"), true);
                                return 1;
                            }))
                            .then(CommandManager.literal("off").executes(ctx -> {
                                MinecraftServer server = ctx.getSource().getServer();
                                NickState st = NickState.get(server);

                                st.setHideAll(false);
                                NickSync.broadcastHideAll(server, false);

                                ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.hide_off"), true);
                                return 1;
                            }))
                    )
            );
        });
    }

    private static int setNickname(
            ServerPlayerEntity player,
            MinecraftServer server,
            String rawNickname,
            java.util.function.BiConsumer<java.util.function.Supplier<Text>, Boolean> feedbackSender
    ) {
        String nickname = rawNickname.trim();
        if (nickname.isBlank()) {
            feedbackSender.accept(() -> Text.translatable("text.name_changer.nick_empty"), false);
            return 0;
        }

        if (nickname.length() > MAX_NICK_LEN) {
            feedbackSender.accept(() -> Text.translatable("text.name_changer.nick_too_long", MAX_NICK_LEN), false);
            return 0;
        }

        NickState state = NickState.get(server);
        state.setNick(player.getUuid(), nickname);
        NickSync.broadcastNick(server, player.getUuid(), nickname);

        feedbackSender.accept(() -> Text.translatable("text.name_changer.nick_set", nickname), false);
        return 1;
    }
}
