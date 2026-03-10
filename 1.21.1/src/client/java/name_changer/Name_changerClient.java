package name_changer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;

public class Name_changerClient implements ClientModInitializer {
    private static KeyBinding SHOW_HIDDEN_NAMES_KEY;

    @Override
    public void onInitializeClient() {
        SHOW_HIDDEN_NAMES_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.name_changer.show_hidden_names",
                GLFW.GLFW_KEY_U,
                "key.categories.name_changer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client ->
                NicknameClientState.setShowHiddenNames(SHOW_HIDDEN_NAMES_KEY.isPressed())
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NicknameClientState.clear());

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_SYNC_ALL, (payload, context) ->
                context.client().execute(() ->
                        NicknameClientState.applySyncState(payload.nicks(), payload.hideAll())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_SET_ONE_NICK, (payload, context) ->
                context.client().execute(() ->
                        NicknameClientState.setNickname(payload.uuid(), payload.nick())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_HIDE_ALL, (payload, context) ->
                context.client().execute(() ->
                        NicknameClientState.setHideAllNames(payload.hideAll())
                )
        );

        ClientReceiveMessageEvents.ALLOW_CHAT.register((Text message,
                                                        SignedMessage signedMessage,
                                                        GameProfile sender,
                                                        net.minecraft.network.message.MessageType.Parameters params,
                                                        Instant receptionTimestamp) -> {
            if (sender == null) {
                return true;
            }

            Text content = signedMessage != null ? signedMessage.getContent() : message;
            Text shownName = NicknameClientState.getChatDisplayName(sender.getId(), sender.getName());
            Text rewrittenMessage = Text.translatable("chat.type.text", shownName, content);

            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(rewrittenMessage)
            );

            return false;
        });
    }
}
