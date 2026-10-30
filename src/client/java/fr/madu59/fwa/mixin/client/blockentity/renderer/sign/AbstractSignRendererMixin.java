package fr.madu59.fwa.mixin.client.blockentity.renderer.sign;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDrivers;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {

    @WrapMethod(method = "renderSignWithText")
    public void fwa$submitSignWithText(SignBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BlockState blockState, SignBlock signBlock, WoodType woodType, Model model, Operation<Void> original) {
        poseStack.pushPose();
        if(SettingsManager.HANGING_SIGN_STATE.getValue()){
            if(blockEntity.getBlockState().getBlock() instanceof CeilingHangingSignBlock) {
                HangingSwing swing = new HangingSwing();
                if(SwingDrivers.getSwing(blockEntity.getBlockPos(), blockEntity.getBlockState(), FancyWorldAnimationsClient.getPartialTick(), swing)){
                    float signRotation = RotationSegment.convertToDegrees(blockEntity.getBlockState().getValue(CeilingHangingSignBlock.ROTATION));

                    poseStack.translate(0.5, 1.0, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-signRotation));
                    poseStack.mulPose(Axis.XP.rotationDegrees(swing.tiltX));
                    poseStack.mulPose(Axis.YP.rotationDegrees(signRotation));
                    poseStack.translate(-0.5, -1.0, -0.5);
                }
            }
        }
        original.call(blockEntity, poseStack, multiBufferSource, i, j, blockState, signBlock, woodType, model);
        poseStack.popPose();
    }
}
