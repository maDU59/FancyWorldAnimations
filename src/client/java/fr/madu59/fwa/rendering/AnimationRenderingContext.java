package fr.madu59.fwa.rendering;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class AnimationRenderingContext {
    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;
    private final double nowTick;
    private final Camera camera;
    private final Vec3 cameraPos;
    private final boolean isShadow;
    private final Frustum frustum;
    private final CameraRenderState cameraRenderState;

    public AnimationRenderingContext(PoseStack poseStack, Camera camera, SubmitNodeCollector submitNodeCollector, Frustum frustum, CameraRenderState cameraRenderState, double nowTick, boolean isShadow) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.nowTick = nowTick;
        this.camera = camera;
        this.cameraPos = camera.position();
        this.isShadow = isShadow;
        this.frustum = frustum;
        this.cameraRenderState = cameraRenderState;
    }

    public AnimationRenderingContext(PoseStack poseStack, Vec3 cameraPos, SubmitNodeCollector submitNodeCollector, Frustum frustum, double nowTick, boolean isShadow) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.nowTick = nowTick;
        this.camera = Minecraft.getInstance().gameRenderer.mainCamera();
        this.cameraPos = cameraPos;
        this.isShadow = isShadow;
        this.frustum = frustum;
        CameraRenderState cameraRenderState = new CameraRenderState();
        Minecraft.getInstance().gameRenderer.mainCamera().extractRenderState(cameraRenderState, 0);
        this.cameraRenderState = cameraRenderState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    public double getNowTick() {
        return nowTick;
    }

    public Vec3 getCameraPos() {
        return cameraPos;
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean isShadow(){
        return isShadow;
    }

    public Frustum getFrustum(){
        return frustum;
    }

    public CameraRenderState getCameraRenderState(){
        return cameraRenderState;
    }
}