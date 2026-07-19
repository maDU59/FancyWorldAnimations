package fr.madu59.fwa.mixin.client.blockentity.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import fr.madu59.fwa.rendering.blockentity.BlockEntityRenderStateExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Mixin(value = BellRenderer.class, priority = 1001)
public class BellRendererMixin {

    @Shadow
    public static Material BELL_TEXTURE;

    private final Identifier atlasId = Identifier.tryParse("minecraft:blocks");

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void fwa$submit(final BellRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera, CallbackInfo ci) {
        Animation animation = FancyWorldAnimationsClient.animations.getAt(((BlockEntityRenderStateExt)state).getPosition());
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

            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlasId).getSprite(BELL_TEXTURE.texture());

            submitNodeCollector.submitModelPart(bellBody, poseStack, RenderTypes.cutoutMovingBlock(), state.lightCoords, OverlayTexture.NO_OVERLAY, sprite);
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void obe$cancelExtract(CallbackInfo ci, @Local BellRenderState state, @Local BellBlockEntity be){
        ((BlockEntityRenderStateExt)state).setPosition(be.getBlockPos());
    }
}
