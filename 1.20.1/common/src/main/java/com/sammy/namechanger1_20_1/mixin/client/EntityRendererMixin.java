package com.sammy.namechanger1_20_1.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sammy.namechanger1_20_1.client.NicknameClientState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void namechanger1_20_1$hidePlayerNames(
        Entity entity,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo callbackInfo
    ) {
        if (NicknameClientState.shouldHideOverheadName(entity)) {
            callbackInfo.cancel();
        }
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component namechanger1_20_1$replaceDisplayName(Entity entity) {
        return NicknameClientState.getOverheadDisplayName(entity);
    }
}
