package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDrivers;
import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationBlockData;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.AnimationRenderer;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LanternAnimation extends Animation{

    private float tiltX = 0f;
    private float tiltZ = 0f;
    private float spin = 0f;
    private final HangingSwing swing = new HangingSwing();
    private final List<BlockStateModelPart> parts = new ArrayList<>();
    private int chainCount;
    private final List<BlockStateModelPart> chainParts = new ArrayList<>();
    private final Quaternionf combined = new Quaternionf();
    private int[] lightValues;
    private PoseStack.Pose[] poses;
    private boolean updateLights = true;
    private boolean updatePoses = true;
    
    public LanternAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        chainCount = SwingingBlockHelper.getChainCount(position);
    }

    @Override
    public boolean isFinished(double nowTick) {
        return false;
    }

    public boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlockEntity() {
        return !ModCompat.WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(defaultState.getBlock()));
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public void updateBoundingBox(){
        this.boundingBox = new AABB(Vec3.atCenterOf(position).add(-0.5, -0.5, -0.5), Vec3.atCenterOf(position.above(chainCount)).add(0.5, 0.5, 0.5));
    }

    @Override
    public void setLast(boolean isLast){
        if(this.isLast == null || this.isLast != isLast){
            super.setLast(isLast);
            if (isLast) needUpdate();
        }
    }

    public void update(){
        if(!SettingsManager.CHAIN_STATE.getValue() && !SettingsManager.LANTERN_OVERRIDE.getValue()) chainCount = 1;
        else chainCount = SwingingBlockHelper.getChainCount(position);
        updateBoundingBox();

        lightValues = new int[chainCount];
        poses = new PoseStack.Pose[chainCount];
        updateLights = true;
        updatePoses = true;

        needUpdate = false;
    }

    public void updateCache(){
        updateLights = true;
        updatePoses = true;
    }

    @Override 
    public void tickVisible(double nowTick) {
        super.tickVisible(nowTick);
        double distance = distanceToPlayer();
        if(distance > 128 * 128){
            if(getFrameCount() % 8 == 0) updateLights = true;
        }
        else if(distance > 64 * 64){
            if(getFrameCount() % 4 == 0) updateLights = true;
        }
        else if(distance > 16 * 16){
            if(getFrameCount() % 2 == 0) updateLights = true;
        }
        else updateLights = true;
        
        if(distance > 64 * 64){
            if(getFrameCount() % 4 == 0) updatePoses = true;
        }
        else if(distance > 32 * 32){
            if(getFrameCount() % 2 == 0) updatePoses = true;
        }
        else updatePoses = true;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if (needUpdate) update();

        if(updatePoses == true){
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
        RandomSource random = RandomSource.create();
        MutableBlockPos mutable = position.mutable().move(0,chainCount-1,0);
        for (int index = chainCount - 1; index >= 1; --index) {
            BlockState chainState = level.getBlockState(mutable);

            if(updateLights) lightValues[index] = getLight(mutable, chainState);
            int light = lightValues[index];
            
            chainParts.clear();
            random.setSeed(chainState.getSeed(mutable));
            BlockStateModel chainModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(chainState);
            chainModel.collectParts(random, chainParts);
            PoseStack.Pose pose = poseStack.last().copy();
            pose.mulPose(poses[index].pose());
            AnimationRenderer.renderModel(pose, chainParts, 1.0f, 1.0f, 1.0f, 1.0f, light, new AnimationBlockData(chainState, mutable, light));
        }

        if(updateLights) lightValues[0] = getLight(position, defaultState);
        int light = lightValues[0];

        random.setSeed(defaultState.getSeed(position));
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(defaultState);
        parts.clear();
        model.collectParts(random, parts);
        PoseStack.Pose pose = poseStack.last().copy();
        pose.mulPose(poses[0].pose());
        AnimationRenderer.renderModel(pose, parts, 1.0f, 1.0f, 1.0f, 1.0f, light, getAnimationBlockData());

        updateLights = false;
        updatePoses = false;
    }

    public void animate(AnimationRenderingContext context) {
        if(SwingDrivers.getSwing(position, defaultState, context.getNowTick(), swing)){
            this.tiltX = swing.tiltX;
            this.tiltZ = swing.tiltZ;
            this.spin = swing.spin;
        }
    }
}
