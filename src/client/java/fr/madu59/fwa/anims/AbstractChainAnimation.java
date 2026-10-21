package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDrivers;
import fr.madu59.fwa.rendering.AnimationBlockData;
import fr.madu59.fwa.rendering.AnimationRenderer;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractChainAnimation extends Animation {

    private final int positionHash;

    protected Boolean isLast;
    protected boolean needUpdate = true;

    protected float tiltX = 0f;
    protected float tiltZ = 0f;
    protected float spin = 0f;
    protected final HangingSwing swing = new HangingSwing();
    protected int chainCount = 0;

    protected boolean updateLights = true;
    protected boolean updatePoses = true;
    protected boolean updateModels = true;
    protected long lastFrame = -1;

    protected int[] lightValues;
    protected PoseStack.Pose[] poses;
    protected BlockState[] states;
    protected BakedQuad[][] quads;

    protected final List<BlockStateModelPart> parts = new ArrayList<>();
    protected final Quaternionf combined = new Quaternionf();

    public AbstractChainAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        positionHash = position.hashCode();
    }

    public boolean isLast(){
        return Boolean.TRUE.equals(isLast);
    }

    @Override
    public boolean isRendering(){
        return this.isLast();
    }

    @Override
    public void updateBoundingBox(){
        this.boundingBox = new AABB(Vec3.atCenterOf(position).add(-0.5, -0.5, -0.5), Vec3.atCenterOf(position.above(chainCount)).add(0.5, 0.5, 0.5));
    }

    @Override
    public void updateModel(){
        updateModels = true;
    }

    @Override
    public boolean isFinished(double nowTick) {
        return false;
    }

    public void needUpdate(){
        needUpdate = true;
    }

    public void update(){
        updateChainCount();
        updateBoundingBox();

        lightValues = new int[chainCount];
        poses = new PoseStack.Pose[chainCount];
        states = new BlockState[chainCount];
        quads = new BakedQuad[chainCount][];

        updateLights = true;
        updatePoses = true;
        updateModels = true;

        needUpdate = false;
    }

    public void setLast(boolean isLast){
        if(this.isLast == null || this.isLast != isLast){
            this.isLast = isLast;
            if (isLast) needUpdate();
        }
    }

    public void updateChainCount(){
        chainCount = SwingingBlockHelper.getChainCount(position);
    }

    public void updateCache(long frameCount){
        if(frameCount == lastFrame) return;
        lastFrame = frameCount;

        long random = frameCount + positionHash;

        double distance = distanceToPlayer();
        if(distance > 128 * 128){
            if(random % 8 == 0) updateLights = true;
        }
        else if(distance > 64 * 64){
            if(random % 4 == 0) updateLights = true;
        }
        else if(distance > 16 * 16){
            if(random % 2 == 0) updateLights = true;
        }
        else updateLights = true;
        
        if(distance > 64 * 64){
            if(random % 4 == 0) updatePoses = true;
        }
        else if(distance > 32 * 32){
            if(random % 2 == 0) updatePoses = true;
        }
        else updatePoses = true;
    }

    public void animate(AnimationRenderingContext context) {
        if(SwingDrivers.getSwing(position, defaultState, context.getNowTick(), swing)){
            this.tiltX = swing.tiltX;
            this.tiltZ = swing.tiltZ;
            this.spin = swing.spin;
        }
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if (!this.isLast()) {
            if(isLast == null) {
                update();
            }
            return;
        }
        if (needUpdate) update();

        updateCache(context.getFrameCount());

        if(updatePoses){
            PoseStack poseStack = new PoseStack();
            float swingScale = 0.7F/(float)Math.sqrt(Math.max(4,chainCount)-3);
            float prevFactor = 0.0F;
            animate(context);
            float degToRad = 0.017453292519943295f;
            float tiltX = this.tiltX * swingScale * degToRad;
            float tiltZ = this.tiltZ * swingScale * degToRad;
            float spin = this.spin * Math.max(0.55F, swingScale) * degToRad;

            poseStack.pushPose();
            poseStack.translate(0.5F, chainCount, 0.5F);
            for (int index = chainCount - 1; index >= 0; --index) {
                float t = (float)(chainCount - index) / (float)chainCount;
                t = Mth.clamp(t, 0.0F, 1.0F);
                float targetFactor = Curves.smooth(t);
                float deltaFactor = targetFactor - prevFactor;
                prevFactor = targetFactor;
                if (deltaFactor != 0.0F) {
                    combined.identity()
                        .rotateZ(tiltZ * deltaFactor)
                        .rotateX(tiltX * deltaFactor)
                        .rotateY(spin * deltaFactor);
                    poseStack.rotate(combined);
                }
                poseStack.pushPose();
                poseStack.translate(-0.5F, -1.0F, -0.5F);
                poses[index] = poseStack.last().copy();
                poseStack.popPose();
                poseStack.translate(0.0F, -1.0F, 0.0F);
            }
            poseStack.popPose();
        }

        PoseStack poseStack = context.getPoseStack();

        ClientLevel level = Minecraft.getInstance().level;
        MutableBlockPos mutable = position.mutable().move(0,chainCount-1,0);
        for (int index = chainCount - 1; index >= 0; --index) {
            assert level != null;
            BlockState state = level.getBlockState(mutable);

            if(updateLights) lightValues[index] = getLight(mutable, state);
            int light = lightValues[index];

            if(updateModels || states[index] == null || states[index] != state){
                RandomSource random = RandomSource.create();
                parts.clear();
                random.setSeed(state.getSeed(mutable));
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).collectParts(random, parts);
                quads[index] = AnimationRenderer.flattenModel(parts);
                states[index] = state;
            }

            PoseStack.Pose pose = poseStack.last().copy();
            pose.mulPose(poses[index].pose());

            AnimationRenderer.renderQuads(pose, quads[index], 1.0f, 1.0f, 1.0f, 1.0f, light, new AnimationBlockData(state, mutable, light));
            mutable.move(0,-1,0);
        }

        updateLights = false;
        updatePoses = false;
        updateModels = false;
    }
    
}
