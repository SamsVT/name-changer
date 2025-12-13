package name_changer.mixin;

import name_changer.Name_changerClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void name_changer$getDisplayName(CallbackInfoReturnable<Text> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof PlayerEntity p) {
            String nick = Name_changerClient.NICKS.get(p.getUuid());
            if (nick != null && !nick.isBlank()) {
                cir.setReturnValue(Text.literal(nick));
            }
        }
    }
}
