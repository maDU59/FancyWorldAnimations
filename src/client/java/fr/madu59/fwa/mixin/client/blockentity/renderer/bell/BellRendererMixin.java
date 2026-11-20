package fr.madu59.fwa.mixin.client.blockentity.renderer.bell;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Mixin(value = BellRenderer.class, priority = 1001)
public abstract class BellRendererMixin {

    @Final
    @Shadow
    private ModelPart bellBody;

    @Final
    @Shadow
    public static Material BELL_RESOURCE_LOCATION;

    @WrapMethod(method = "render")
    public void fwa$submit(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Operation<Void> original) {
        Animation animation = FancyWorldAnimationsClient.animations.getAt(bellBlockEntity.getBlockPos());

        if (bellBlockEntity.clickDirection == null && !bellBlockEntity.shaking && animation != null && animation instanceof BellAnimation bellAnimation) {
            if (bellBody == null) {
                original.call(bellBlockEntity, f, poseStack, multiBufferSource, i, j);
                return;
            }

            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            float time = (float)(nowTick - bellAnimation.getStartTick());
            float rot;
            if (time <= bellAnimation.getAnimDuration()) rot = bellAnimation.animatePlacement(nowTick);
            else rot = bellAnimation.animateIdle(time);

            float px = bellBody.x / 16f;
            float py = bellBody.y / 16f;
            float pz = bellBody.z / 16f;

            float rotationOffset = bellAnimation.attachment == BellAttachType.SINGLE_WALL ? 90.0f : 0.0f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.mulPose(Axis.YP.rotationDegrees(-(bellAnimation.facing.toYRot() + rotationOffset)));
            poseStack.mulPose(Axis.XP.rotation(-rot));
            poseStack.mulPose(Axis.YP.rotationDegrees((bellAnimation.facing.toYRot() + rotationOffset)));
            poseStack.translate(-px, -py, -pz);

            original.call(bellBlockEntity, f, poseStack, multiBufferSource, i, j);
            poseStack.popPose();;
            return;
        }

        original.call(bellBlockEntity, f, poseStack, multiBufferSource, i, j);
    }
}
