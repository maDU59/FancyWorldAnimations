package fr.madu59.fwa.mixin.client.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;

@Mixin(ShadowRenderer.class)
public abstract class ShadowRendererMixin {
    
    @Inject(at = @At("HEAD"), method = "renderEntities")
    public void fwa$renderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, MultiBufferSource.BufferSource bufferSource, PoseStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Integer> ci){
        FancyWorldAnimationsClient.render(modelView, new Vec3(cameraX, cameraY, cameraZ), bufferSource);
    }
}