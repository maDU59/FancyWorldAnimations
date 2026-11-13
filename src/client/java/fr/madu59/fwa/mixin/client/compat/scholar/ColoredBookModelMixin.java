package fr.madu59.fwa.mixin.client.compat.scholar;

import com.llamalad7.mixinextras.sugar.Local;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.LecternAnimation;
import fr.madu59.fwa.rendering.blockentity.lectern.LecternRenderStateExt;
import io.github.mortuusars.scholar.client.ColoredBookModel;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(value = ColoredBookModel.class, remap = false)
public class ColoredBookModelMixin {
    @ModifyVariable(
            method = "submitOnLectern",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static BookModel.State fwa$replaceBookState(BookModel.State original, @Local LecternRenderState state) {
        if (((LecternRenderStateExt) state).fwa$getPosition() == null) return original;
        Animation animation = FancyWorldAnimationsClient.animations.getAt(((LecternRenderStateExt) state).fwa$getPosition());
        if (animation instanceof LecternAnimation lecternAnimation) {
            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            return lecternAnimation.animate(nowTick);
        }
        return original;
    }
}
