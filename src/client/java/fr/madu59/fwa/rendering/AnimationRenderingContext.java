package fr.madu59.fwa.rendering;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class AnimationRenderingContext {
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final double nowTick;
    private final Camera camera;
    private final Vec3 cameraPos;
    private final boolean isShadow;

    public AnimationRenderingContext(PoseStack poseStack, Camera camera, MultiBufferSource bufferSource, double nowTick, boolean isShadow) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.nowTick = nowTick;
        this.camera = camera;
        this.cameraPos = camera.position();
        this.isShadow = isShadow;
    }

    public AnimationRenderingContext(PoseStack poseStack, Vec3 cameraPos, MultiBufferSource bufferSource, double nowTick, boolean isShadow) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.nowTick = nowTick;
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.cameraPos = cameraPos;
        this.isShadow = isShadow;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getBufferSource() {
        return bufferSource;
    }

    public double getNowTick() {
        return nowTick;
    }

    public Vec3 getCameraPos() {
        return cameraPos;
    }

    public boolean isShadowPass(){
        return isShadow;
    }
}