package com.sammy.namechanger1_20_1.mixin.client;

import com.sammy.namechanger1_20_1.client.NicknameClientState;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
    private void namechanger1_20_1$replaceTabName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        Component nickname = NicknameClientState.getTabDisplayName(playerInfo);
        if (nickname != null) {
            cir.setReturnValue(nickname);
        }
    }
}
