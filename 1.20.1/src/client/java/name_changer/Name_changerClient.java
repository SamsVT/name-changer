package name_changer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Name_changerClient implements ClientModInitializer {

    public static final Map<UUID, String> NICKS = new ConcurrentHashMap<>();
    private static volatile boolean HIDE_ALL = false;

    private static KeyBinding REVEAL_KEY;
    private static volatile boolean REVEAL_HELD = false;

    public static boolean isRevealHeld() { return REVEAL_HELD; }
    public static boolean isHideAll() { return HIDE_ALL; }

    @Override
    public void onInitializeClient() {

        REVEAL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.name_changer.reveal",
                GLFW.GLFW_KEY_U,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> REVEAL_HELD = REVEAL_KEY.isPressed());

        ClientPlayNetworking.registerGlobalReceiver(NickSync.S2C_SYNC_ALL, (client, handler, buf, responseSender) -> {
            var all = NickSync.readAll(buf);
            client.execute(() -> {
                NICKS.clear();
                NICKS.putAll(all.nicks());
                HIDE_ALL = all.hideAll();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NickSync.S2C_SET_ONE_NICK, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            String nick = buf.readString(64);

            client.execute(() -> {
                if (nick == null || nick.isBlank()) NICKS.remove(id);
                else NICKS.put(id, nick);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NickSync.S2C_HIDE_ALL, (client, handler, buf, responseSender) -> {
            boolean v = buf.readBoolean();
            client.execute(() -> HIDE_ALL = v);
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((Text message,
                                                        SignedMessage signedMessage,
                                                        GameProfile sender,
                                                        net.minecraft.network.message.MessageType.Parameters params,
                                                        Instant receptionTimestamp) -> {

            if (sender == null) return true;

            UUID id = sender.getId();
            if (id == null) return true;

            String nick = NICKS.get(id);
            if (nick == null || nick.isBlank()) return true;

            Text content = (signedMessage != null) ? signedMessage.getContent() : message;
            Text newMsg = Text.translatable("chat.type.text", Text.literal(nick), content);
            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(newMsg)
            );
            return false;
        });
    }
}
