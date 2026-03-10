package com.sammy.namechanger1_20_1.mixin.client;

import com.sammy.namechanger1_20_1.client.NicknameClientState;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @Redirect(
        method = "handlePlayerChatMessage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/ChatType$Bound;decorate(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component namechanger1_20_1$decorateWithNickname(
        ChatType.Bound bound,
        Component message,
        PlayerChatMessage chatMessage,
        GameProfile profile,
        ChatType.Bound originalBound
    ) {
        return NicknameClientState.getChatDisplayName(bound, chatMessage.sender()).decorate(message);
    }
}
