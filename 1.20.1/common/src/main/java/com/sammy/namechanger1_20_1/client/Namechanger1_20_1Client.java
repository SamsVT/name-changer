package com.sammy.namechanger1_20_1.client;

import com.sammy.namechanger1_20_1.nickname.NicknameNetworking;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class Namechanger1_20_1Client {
    private static final KeyMapping SHOW_HIDDEN_NAMES_KEY = new KeyMapping(
        "key.namechanger1_20_1.reveal_real_names",
        GLFW.GLFW_KEY_U,
        "key.categories.namechanger1_20_1"
    );

    private Namechanger1_20_1Client() {
    }

    public static void init() {
        KeyMappingRegistry.register(SHOW_HIDDEN_NAMES_KEY);

        NetworkManager.registerReceiver(NetworkManager.s2c(), NicknameNetworking.SYNC_STATE_PACKET, (buffer, context) -> {
            NicknameNetworking.SyncState syncState = NicknameNetworking.readSyncState(buffer);
            context.queue(() -> NicknameClientState.applySyncState(syncState));
        });

        ClientTickEvent.CLIENT_POST.register(client ->
            NicknameClientState.setShowHiddenNames(SHOW_HIDDEN_NAMES_KEY.isDown())
        );

        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> NicknameClientState.setShowHiddenNames(false));
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> NicknameClientState.clear());
    }
}
