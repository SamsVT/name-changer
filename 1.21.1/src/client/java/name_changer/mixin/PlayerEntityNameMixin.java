package name_changer.mixin;

import name_changer.Name_changerClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityNameMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void name_changer$getName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity p = (PlayerEntity) (Object) this;

        Text nick = Name_changerClient.getNickOrNull(p.getUuid());
        if (nick != null) {
            cir.setReturnValue(nick);
        }
    }
}
