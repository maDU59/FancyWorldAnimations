package fr.madu59.fwa.mixin.client.blockentity.renderer.bell;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Mixin(value = BellRenderer.class, priority = 1001)
public abstract class BellRendererMixin {

    @Shadow
    public static Material BELL_RESOURCE_LOCATION;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void fwa$submit(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci) {
        if(bellBlockEntity == null) return;
        Animation animation = FancyWorldAnimationsClient.animations.getAt(bellBlockEntity.getBlockPos());

        if (bellBlockEntity.clickDirection != null && !bellBlockEntity.shaking  && animation != null && animation instanceof BellAnimation bellAnimation) {
            bellAnimation.bellModel.setupAnim(bellBlockEntity, f);
            ModelPart bellBody = bellAnimation.bellModel.allParts().get(0).getChild("bell_body");

            double nowTick = FancyWorldAnimationsClient.getPartialTick();
            float time = (float)(nowTick - bellAnimation.getStartTick());
            float rot;
            if (time <= bellAnimation.getAnimDuration()) rot = bellAnimation.animatePlacement(nowTick);
            else rot = bellAnimation.animateIdle(time);
            bellAnimation.rotateBell(bellBody, rot);

            VertexConsumer vertexConsumer = BELL_RESOURCE_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);

            bellBody.render(poseStack, vertexConsumer, bellAnimation.getLight(), OverlayTexture.NO_OVERLAY, -1);
            ci.cancel();
        }
    }
}
