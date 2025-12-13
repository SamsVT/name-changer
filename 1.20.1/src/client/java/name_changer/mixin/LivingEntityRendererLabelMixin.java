package name_changer.mixin;

import name_changer.Name_changerClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLabelMixin {

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void name_changer$hasLabel(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!Name_changerClient.isHideAll()) return;
        if (Name_changerClient.isRevealHeld()) return;

        if (entity instanceof PlayerEntity) {
            cir.setReturnValue(false);
        }
    }
}
