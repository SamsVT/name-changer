package name_changer;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class Name_changer implements ModInitializer {

    public static final int MAX_NICK_LEN = 32;

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("nick")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.usage"), false);
                        return 1;
                    })

                    .then(CommandManager.literal("reset")
                            .executes(ctx -> {
                                var src = ctx.getSource();
                                var player = src.getPlayer();
                                if (player == null) return 0;

                                var st = NickState.get(src.getServer());
                                st.setNick(player.getUuid(), "");

                                NickSync.broadcastNick(src.getServer(), player.getUuid(), st.getNick(player.getUuid()));

                                src.sendFeedback(() -> Text.translatable("text.name_changer.nick_reset"), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("name")
                            .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        var src = ctx.getSource();
                                        var player = src.getPlayer();
                                        if (player == null) return 0;

                                        String raw = StringArgumentType.getString(ctx, "name").trim();
                                        if (raw.isBlank()) {
                                            src.sendFeedback(() -> Text.translatable("text.name_changer.nick_empty"), false);
                                            return 0;
                                        }

                                        if (raw.length() > MAX_NICK_LEN) {
                                            src.sendFeedback(() -> Text.translatable("text.name_changer.nick_too_long", MAX_NICK_LEN), false);
                                            return 0;
                                        }

                                        var st = NickState.get(src.getServer());
                                        st.setNick(player.getUuid(), raw);

                                        NickSync.broadcastNick(src.getServer(), player.getUuid(), raw);

                                        src.sendFeedback(() -> Text.translatable("text.name_changer.nick_set", raw), false);
                                        return 1;
                                    })
                            )
                    )

                    .then(CommandManager.argument("name", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                var src = ctx.getSource();
                                var player = src.getPlayer();
                                if (player == null) return 0;

                                String raw = StringArgumentType.getString(ctx, "name").trim();
                                if (raw.isBlank()) {
                                    src.sendFeedback(() -> Text.translatable("text.name_changer.nick_empty"), false);
                                    return 0;
                                }

                                if (raw.length() > MAX_NICK_LEN) {
                                    src.sendFeedback(() -> Text.translatable("text.name_changer.nick_too_long", MAX_NICK_LEN), false);
                                    return 0;
                                }

                                var st = NickState.get(src.getServer());
                                st.setNick(player.getUuid(), raw);

                                NickSync.broadcastNick(src.getServer(), player.getUuid(), raw);

                                src.sendFeedback(() -> Text.translatable("text.name_changer.nick_set", raw), false);
                                return 1;
                            })
                    )

                    .then(CommandManager.literal("hide")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("on")
                                    .executes(ctx -> {
                                        var src = ctx.getSource();
                                        var st = NickState.get(src.getServer());
                                        st.setHideAll(true);

                                        NickSync.broadcastHideAll(src.getServer(), true);

                                        src.sendFeedback(() -> Text.translatable("text.name_changer.hide_on"), false);
                                        return 1;
                                    })
                            )
                            .then(CommandManager.literal("off")
                                    .executes(ctx -> {
                                        var src = ctx.getSource();
                                        var st = NickState.get(src.getServer());
                                        st.setHideAll(false);

                                        NickSync.broadcastHideAll(src.getServer(), false);

                                        src.sendFeedback(() -> Text.translatable("text.name_changer.hide_off"), false);
                                        return 1;
                                    })
                            )
                    )
            );
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var st = NickState.get(server);
            NickSync.sendAllTo(handler.getPlayer(), st.getAllNicks(), st.isHideAll());
        });
    }
}
