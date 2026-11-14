package fr.madu59.fwa.mixin.compat.scholar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.LecternAnimation;
import io.github.mortuusars.scholar.client.ColoredBookModel;
import net.minecraft.client.model.BookModel;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = ColoredBookModel.class, remap = false)
public class ColoredBookModelMixin {
    @WrapOperation(
            method = "renderOnLectern",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/BookModel;setupAnim(FFFF)V",
                    remap = true
            )
    )
    private static void fwa$replaceBookState(BookModel instance, float f, float g, float h, float i, Operation<Void> original, @Local LecternBlockEntity be) {
        Animation animation = FancyWorldAnimationsClient.animations.getAt(be.getBlockPos());
        if(animation instanceof LecternAnimation lecternAnimation){
            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            lecternAnimation.animate(instance, nowTick);
        }
        else{
            original.call(instance, f, g, h, i);
        }
    }
}