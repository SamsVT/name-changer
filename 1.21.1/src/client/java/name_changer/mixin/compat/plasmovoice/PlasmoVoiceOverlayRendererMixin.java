package name_changer.mixin.compat.plasmovoice;

import name_changer.NicknameClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.entity.player.McGameProfile;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

import java.util.UUID;

@Pseudo
@Mixin(targets = "su.plo.voice.client.render.voice.OverlayRenderer", remap = false)
public abstract class PlasmoVoiceOverlayRendererMixin {

    @Redirect(
            method = "onRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lsu/plo/slib/api/entity/player/McGameProfile;getName()Ljava/lang/String;"
            ),
            remap = false
    )
    private String name_changer$replaceOverlayName(McGameProfile profile) {
        return resolveName(profile.getId(), profile.getName());
    }

    @Inject(method = "getSourceSenderId", at = @At("HEAD"), cancellable = true, remap = false)
    private void name_changer$replaceSourceSenderId(SourceInfo sourceInfo, CallbackInfoReturnable<UUID> cir) {
        UUID playerId = resolvePlayerId(sourceInfo);
        if (playerId != null) {
            cir.setReturnValue(playerId);
        }
    }

    @Inject(method = "getSourceSenderName", at = @At("RETURN"), cancellable = true, remap = false)
    private void name_changer$replaceSourceSenderName(
            SourceInfo sourceInfo,
            ClientSourceLine line,
            CallbackInfoReturnable<McTextComponent> cir
    ) {
        UUID playerId = resolvePlayerId(sourceInfo);
        if (playerId == null) {
            return;
        }

        String realName = resolveRealName(playerId, sourceInfo);
        if (realName == null || realName.isEmpty()) {
            return;
        }

        cir.setReturnValue(McTextComponent.Companion.literal(resolveName(playerId, realName)));
    }

    private String resolveName(UUID playerId, String realName) {
        if (playerId == null) {
            return realName;
        }

        return NicknameClientState.getNicknameOrRealName(playerId, realName);
    }

    private UUID resolvePlayerId(SourceInfo sourceInfo) {
        if (sourceInfo instanceof DirectSourceInfo directSourceInfo) {
            McGameProfile sender = directSourceInfo.getSender();
            if (sender != null && sender.getId() != null) {
                return sender.getId();
            }

            if (sender != null) {
                return NicknameClientState.findPlayerIdByProfileName(sender.getName());
            }

            return null;
        }

        if (sourceInfo instanceof PlayerSourceInfo playerSourceInfo) {
            VoicePlayerInfo playerInfo = playerSourceInfo.getPlayerInfo();
            if (playerInfo == null) {
                return null;
            }

            if (playerInfo.getPlayerId() != null) {
                return playerInfo.getPlayerId();
            }

            return NicknameClientState.findPlayerIdByProfileName(playerInfo.getPlayerNick());
        }

        if (sourceInfo instanceof EntitySourceInfo entitySourceInfo) {
            return NicknameClientState.findPlayerIdByEntityId(entitySourceInfo.getEntityId());
        }

        return null;
    }

    private String resolveRealName(UUID playerId, SourceInfo sourceInfo) {
        String profileName = NicknameClientState.getProfileName(playerId);
        if (profileName != null && !profileName.isEmpty()) {
            return profileName;
        }

        if (sourceInfo instanceof DirectSourceInfo directSourceInfo) {
            McGameProfile sender = directSourceInfo.getSender();
            if (sender != null && sender.getName() != null && !sender.getName().isEmpty()) {
                return sender.getName();
            }
        }

        if (sourceInfo instanceof PlayerSourceInfo playerSourceInfo) {
            VoicePlayerInfo playerInfo = playerSourceInfo.getPlayerInfo();
            if (playerInfo != null && playerInfo.getPlayerNick() != null && !playerInfo.getPlayerNick().isEmpty()) {
                return playerInfo.getPlayerNick();
            }
        }

        String sourceName = sourceInfo.getName();
        if (sourceName != null && !sourceName.isEmpty()) {
            return sourceName;
        }

        return null;
    }
}
