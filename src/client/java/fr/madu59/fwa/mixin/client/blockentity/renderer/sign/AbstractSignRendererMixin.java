package fr.madu59.fwa.mixin.client.blockentity.renderer.sign;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDrivers;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.blockentity.sign.SignRenderStateExt;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void fwa$extractRenderState(SignBlockEntity blockEntity, SignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        if (state instanceof SignRenderStateExt ext) {
            if(blockEntity.getBlockState().getBlock() instanceof CeilingHangingSignBlock){
                HangingSwing fwa$swing = new HangingSwing();
                if(SwingDrivers.getSwing(blockEntity.getBlockPos(), blockEntity.getBlockState(), FancyWorldAnimationsClient.getPartialTick(), fwa$swing)){
                    ext.fwa$setSwing(fwa$swing);
                    ext.fwa$setDirection(blockEntity.getBlockState().getValue(CeilingHangingSignBlock.ROTATION));
                    return;
                }
            }
            ext.fwa$setSwing(null);
            ext.fwa$setDirection(-1);
        }
    }

    @WrapMethod(method = "submitSignWithText")
    public void fwa$submitSignWithText(SignRenderState state, PoseStack poseStack, BlockState blockState, SignBlock signBlock, WoodType woodType, Model.Simple simple, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        poseStack.pushPose();
        if(SettingsManager.HANGING_SIGN_STATE.getValue() && state instanceof SignRenderStateExt ext && ext.fwa$getSwing() != null){
            float signRotation = RotationSegment.convertToDegrees(ext.fwa$getDirection());

            poseStack.translate(0.5, 1.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(- signRotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(ext.fwa$getSwing().tiltX));
            poseStack.mulPose(Axis.YP.rotationDegrees(signRotation));
            poseStack.translate(-0.5, -1.0, -0.5);
        }
        original.call(state, poseStack, blockState, signBlock, woodType, simple, crumblingOverlay, submitNodeCollector);
        poseStack.popPose();
    }
}
