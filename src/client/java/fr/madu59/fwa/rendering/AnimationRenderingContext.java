package fr.madu59.fwa.rendering;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

public class AnimationRenderingContext {
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final SubmitNodeCollector submitNodeCollector;
    private final double nowTick;
    private final Camera camera;
    private final Vec3 cameraPos;

    public AnimationRenderingContext(PoseStack poseStack, Camera camera, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.submitNodeCollector = submitNodeCollector;
        this.nowTick = nowTick;
        this.camera = camera;
        this.cameraPos = camera.position();
    }

    public AnimationRenderingContext(PoseStack poseStack, Vec3 cameraPos, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.submitNodeCollector = submitNodeCollector;
        this.nowTick = nowTick;
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.cameraPos = cameraPos;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getBufferSource() {
        return bufferSource;
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
}