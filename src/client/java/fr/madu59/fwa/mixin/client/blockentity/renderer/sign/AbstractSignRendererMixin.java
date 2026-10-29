package fr.madu59.fwa.mixin.client.blockentity.renderer.sign;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.madu59.fwa.rendering.blockentity.sign.HangingSignRenderStateExt;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin<S extends SignRenderState> {
    @WrapMethod(method = "submitSignWithText")
    public void fwa$submitSignWithText(S state, PoseStack poseStack, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        poseStack.pushPose();
        if(state instanceof HangingSignRenderStateExt ext) {
            poseStack.translate(0.5, 1.0, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(ext.fwa$getSwing()));
            poseStack.translate(-0.5, -1.0, -0.5);
        }
        original.call(state, poseStack, breakProgress, submitNodeCollector);
        poseStack.popPose();
    }
}
