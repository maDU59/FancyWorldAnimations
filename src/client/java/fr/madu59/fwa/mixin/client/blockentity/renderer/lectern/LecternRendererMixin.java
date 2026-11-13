package fr.madu59.fwa.mixin.client.blockentity.renderer.lectern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.LecternAnimation;
import fr.madu59.fwa.rendering.blockentity.lectern.LecternRenderStateExt;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternRenderer.class)
public abstract class LecternRendererMixin {

    @ModifyExpressionValue(
            method = "submit",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/blockentity/LecternRenderer;bookState:Lnet/minecraft/client/model/BookModel$State;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BookModel.State fwa$replaceBookState(BookModel.State original, @Local LecternRenderState state) {
        if(((LecternRenderStateExt)state).fwa$getPosition() == null) return original;
        Animation animation = FancyWorldAnimationsClient.animations.getAt(((LecternRenderStateExt)state).fwa$getPosition());
        if(animation instanceof LecternAnimation lecternAnimation){
            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            return lecternAnimation.animate(nowTick);
        }
        return original;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void obe$cancelExtract(CallbackInfo ci, @Local LecternRenderState state, @Local LecternBlockEntity be){
        ((LecternRenderStateExt)state).fwa$setPosition(be.getBlockPos());
    }
}
