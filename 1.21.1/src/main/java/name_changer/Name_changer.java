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

import java.util.HashMap;

public class Name_changer implements ModInitializer {

    private static final int MAX_NICK_LEN = 32;

    @Override
    public void onInitialize() {
        NickPayloads.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NickState st = NickState.get(server);

            var snapshot = new HashMap<>(st.getAllNicks());
            ServerPlayNetworking.send(player, new NickPayloads.SyncAllS2CPayload(snapshot, st.isHideAll()));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("nick")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.usage"), false);
                        return 1;
                    })

                    .then(CommandManager.literal("name")
                            .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        ServerPlayerEntity p = ctx.getSource().getPlayer();
                                        MinecraftServer server = ctx.getSource().getServer();
                                        NickState st = NickState.get(server);

                                        String raw = StringArgumentType.getString(ctx, "name").trim();
                                        if (raw.isBlank()) {
                                            ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.nick_empty"), false);
                                            return 0;
                                        }

                                        if (raw.length() > MAX_NICK_LEN) {
                                            final int max = MAX_NICK_LEN;
                                            ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.nick_too_long", max), false);
                                            return 0;
                                        }

                                        final String nick = raw;

                                        st.setNick(p.getUuid(), nick);
                                        broadcast(server, new NickPayloads.SetOneNickS2CPayload(p.getUuid(), nick));

                                        ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.nick_set", nick), false);
                                        return 1;
                                    })
                            )
                    )

                    .then(CommandManager.literal("reset")
                            .executes(ctx -> {
                                ServerPlayerEntity p = ctx.getSource().getPlayer();
                                MinecraftServer server = ctx.getSource().getServer();
                                NickState st = NickState.get(server);

                                st.setNick(p.getUuid(), "");
                                broadcast(server, new NickPayloads.SetOneNickS2CPayload(p.getUuid(), ""));

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
                                broadcast(server, new NickPayloads.HideAllS2CPayload(true));

                                ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.hide_on"), true);
                                return 1;
                            }))
                            .then(CommandManager.literal("off").executes(ctx -> {
                                MinecraftServer server = ctx.getSource().getServer();
                                NickState st = NickState.get(server);

                                st.setHideAll(false);
                                broadcast(server, new NickPayloads.HideAllS2CPayload(false));

                                ctx.getSource().sendFeedback(() -> Text.translatable("text.name_changer.hide_off"), true);
                                return 1;
                            }))
                    )
            );
        });
    }

    private static void broadcast(MinecraftServer server, net.minecraft.network.packet.CustomPayload payload) {
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, payload);
        }
    }
}
