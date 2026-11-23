package fr.madu59.fwa.mixin.client.blockentity.renderer.bell;

import com.mojang.math.Axis;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import fr.madu59.fwa.rendering.blockentity.bell.BellRenderStateExt;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Mixin(value = BellRenderer.class, priority = 1001)
public abstract class BellRendererMixin {

    @Final
    @Shadow
    private BellModel model;

    @Final
    @Shadow
    public static SpriteId BELL_TEXTURE;

    @Unique
    private final Identifier atlasId = Identifier.tryParse("minecraft:blocks");

    @WrapMethod(method = "submit")
    public void fwa$submit(BellRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original) {
        if (((BellRenderStateExt) state).fwa$getPosition() == null) {
            original.call(state, poseStack, submitNodeCollector, camera);
            return;
        }
        Animation animation = FancyWorldAnimationsClient.animations.getAt(((BellRenderStateExt)state).fwa$getPosition());

        if (state.shakeDirection == null && animation instanceof BellAnimation bellAnimation) {
            ModelPart bellBody = model.getChildPart("bell_body");
            if (bellBody == null) {
                original.call(state, poseStack, submitNodeCollector, camera);
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

            original.call(state, poseStack, submitNodeCollector, camera);
            poseStack.popPose();
            return;
        }

        original.call(state, poseStack, submitNodeCollector, camera);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void obe$cancelExtract(CallbackInfo ci, @Local BellRenderState state, @Local BellBlockEntity be){
        ((BellRenderStateExt)state).fwa$setPosition(be.getBlockPos());
    }
}
