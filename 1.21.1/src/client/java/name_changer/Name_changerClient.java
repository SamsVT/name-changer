package name_changer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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

    public static Text getNickOrNull(UUID id) {
        String nick = NICKS.get(id);
        if (nick == null || nick.isBlank()) return null;
        return Text.literal(nick);
    }

    // แชท: ถ้ามี nick -> ใช้ nick, ไม่มี -> ใช้ชื่อจริง
    public static Text getChatShownName(UUID id, GameProfile profile) {
        Text nick = getNickOrNull(id);
        if (nick != null) return nick;
        return Text.literal(profile.getName());
    }

    @Override
    public void onInitializeClient() {

        REVEAL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.name_changer.reveal",
                GLFW.GLFW_KEY_U,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> REVEAL_HELD = REVEAL_KEY.isPressed());

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_SYNC_ALL, (payload, context) -> {
            context.client().execute(() -> {
                NICKS.clear();
                NICKS.putAll(payload.nicks());
                HIDE_ALL = payload.hideAll();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_SET_ONE_NICK, (payload, context) -> {
            context.client().execute(() -> {
                String nick = payload.nick();
                if (nick == null || nick.isBlank()) NICKS.remove(payload.uuid());
                else NICKS.put(payload.uuid(), nick);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(NickPayloads.S2C_HIDE_ALL, (payload, context) -> {
            context.client().execute(() -> HIDE_ALL = payload.hideAll());
        });

        // แชท: แสดงชื่อเป็น Nick (ถ้ามี) เสมอ
        ClientReceiveMessageEvents.ALLOW_CHAT.register((Text message,
                                                        SignedMessage signedMessage,
                                                        GameProfile sender,
                                                        net.minecraft.network.message.MessageType.Parameters params,
                                                        Instant receptionTimestamp) -> {

            if (sender == null) return true;

            UUID id = sender.getId();
            Text content = (signedMessage != null) ? signedMessage.getContent() : message;

            Text shownName = getChatShownName(id, sender);
            Text newMsg = Text.translatable("chat.type.text", shownName, content);

            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(newMsg)
            );

            return false;
        });
    }
}
