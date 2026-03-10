package name_changer.mixin.compat.plasmovoice;

import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "su.plo.voice.universal.TextBuilder", remap = false)
public abstract class PlasmoVoiceTextBuilderMixin {

    @Redirect(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/StringBuilder;append(C)Ljava/lang/StringBuilder;"
            ),
            remap = false
    )
    private StringBuilder name_changer$appendCodePoint(
            StringBuilder builder,
            char truncatedCharacter,
            int index,
            Style style,
            int codePoint
    ) {
        return builder.appendCodePoint(codePoint);
    }
}
