package fr.madu59.fwa.mixin.client.blockentity.renderer.bell;

import net.minecraft.client.model.BellModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import fr.madu59.fwa.rendering.blockentity.bell.BellRenderStateExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Mixin(value = BellRenderer.class, priority = 1001)
public abstract class BellRendererMixin {

    @Shadow
    public static Material BELL_RESOURCE_LOCATION;

    @Unique
    private final ResourceLocation atlasId = ResourceLocation.tryParse("minecraft:blocks");

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void fwa$submit(final BellRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera, CallbackInfo ci) {
        if(((BellRenderStateExt)state).fwa$getPosition() == null) return;
        Animation animation = FancyWorldAnimationsClient.animations.getAt(((BellRenderStateExt)state).fwa$getPosition());
        BellModel.State modelState = new BellModel.State(state.ticks, state.shakeDirection);

        if (modelState.shakeDirection() == null && animation != null && animation instanceof BellAnimation bellAnimation) {
            bellAnimation.bellModel.setupAnim(modelState);
            ModelPart bellBody = bellAnimation.bellModel.getChildPart("bell_body");

            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            float time = (float)(nowTick - bellAnimation.getStartTick());
            float rot;
            if (time <= bellAnimation.getAnimDuration()) rot = bellAnimation.animatePlacement(nowTick);
            else rot = bellAnimation.animateIdle(time);
            bellAnimation.rotateBell(bellBody, rot);

            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlasId).getSprite(BELL_RESOURCE_LOCATION.texture());

            RenderType renderType = BELL_RESOURCE_LOCATION.renderType(RenderType::entitySolid);

            submitNodeCollector.submitModelPart(bellBody, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, sprite);;
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void obe$cancelExtract(CallbackInfo ci, @Local BellRenderState state, @Local BellBlockEntity be){
        ((BellRenderStateExt)state).fwa$setPosition(be.getBlockPos());
    }
}
