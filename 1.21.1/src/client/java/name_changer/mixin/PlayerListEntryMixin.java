package name_changer.mixin;

import com.mojang.authlib.GameProfile;
import name_changer.Name_changerClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow public abstract GameProfile getProfile();

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void name_changer$getDisplayName(CallbackInfoReturnable<Text> cir) {
        var profile = getProfile();
        if (profile == null || profile.getId() == null) return;

        Text nick = Name_changerClient.getNickOrNull(profile.getId());
        if (nick != null) {
            cir.setReturnValue(nick);
        }
    }
}
