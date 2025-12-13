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
        String nick = Name_changerClient.NICKS.get(p.getUuid());
        if (nick != null && !nick.isBlank()) {
            cir.setReturnValue(Text.literal(nick));
        }
    }
}
