package fr.madu59.fwa.anims;

import java.util.List;

import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Backport;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FenceGateAnimation extends Animation{

    private final RandomSource random = RandomSource.create(42);
    private final float EPSILON = 0.0001f;

    public FenceGateAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 10 * SettingsManager.FENCEGATE_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.FENCEGATE_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.FENCEGATE_STATE.getValue();
    }

    private double getStartAngle(boolean isOpen){
        if (!isOpen) return 0f;
        return 90f;
    }

    private double getAngle(double nowTick, Direction facing) {
        double angle1 = getStartAngle(this.oldIsOpen);
        double angle2 = getStartAngle(this.newIsOpen);
        double finalAngle = angle1 + (angle2 - angle1) * Curves.ease(getProgress(nowTick), getCurve());
        if(facing == Direction.NORTH || facing == Direction.WEST){
            finalAngle = -finalAngle;
        }
        return finalAngle;
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(FenceGateBlock.FACING);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(defaultState));

        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        BlockModelPart part = model.collectParts(random).get(0);

        List<BakedQuad> quads = new java.util.ArrayList<>();
        for (Direction dir : Direction.values()) {
            quads.addAll(part.getQuads(dir));
        }
        quads.addAll(part.getQuads(null));

        FenceGate fenceGate = splitFenceGateQuads(quads, facing);

        for (BakedQuad quad : fenceGate.postQuadList) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }

        boolean onAxisZ = (facing.getAxis() == Axis.Z);
        float leftPivotX = onAxisZ ? (1.0f / 16.0f) : 0.5f;
        float leftPivotZ = onAxisZ ? 0.5f : (1.0f / 16.0f);
        float rightPivotX = onAxisZ ? (15.0f / 16.0f) : 0.5f;
        float rightPivotZ = onAxisZ ? 0.5f : (15.0f / 16.0f);

        float angle = (float)getAngle(nowTick, facing);
        float leftAngle = onAxisZ ? -angle : angle;
        float rightAngle = onAxisZ ? angle : -angle;

        poseStack.translate(leftPivotX, 0.0f, leftPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(leftAngle));
        poseStack.translate(-leftPivotX, 0.0f, -leftPivotZ);
        for(BakedQuad quad : fenceGate.leftQuadList) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }

        poseStack.translate(leftPivotX, 0.0f, leftPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-leftAngle));
        poseStack.translate(-leftPivotX, 0.0f, -leftPivotZ);

        poseStack.translate(rightPivotX, 0.0f, rightPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rightAngle));
        poseStack.translate(-rightPivotX, 0.0f, -rightPivotZ);
        for(BakedQuad quad : fenceGate.rightQuadList) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public FenceGate splitFenceGateQuads(List<BakedQuad> quads, Direction facing){

        List<BakedQuad> post = new java.util.ArrayList<>();
        List<BakedQuad> left = new java.util.ArrayList<>();
        List<BakedQuad> right = new java.util.ArrayList<>();

        for (BakedQuad quad : quads) {
            
            Vector3fc pos1 = Backport.getPos(quad, 0);
            Vector3fc pos2 = Backport.getPos(quad, 1);
            Vector3fc pos3 = Backport.getPos(quad, 2);
            Vector3fc pos4 = Backport.getPos(quad, 3);

            float min, max;
            if(facing.getAxis() == Axis.X){
                min = Math.min(pos1.z(), Math.min(pos2.z(), Math.min(pos3.z(), pos4.z())));
                max = Math.max(pos1.z(), Math.max(pos2.z(), Math.max(pos3.z(), pos4.z())));
            }
            else{
                min = Math.min(pos1.x(), Math.min(pos2.x(), Math.min(pos3.x(), pos4.x())));
                max = Math.max(pos1.x(), Math.max(pos2.x(), Math.max(pos3.x(), pos4.x())));
            }

            if (gte(min, 0.125f) && lte(max, 0.875f) && !(is(max,min) && (lte(max,0.125f) || gte(max, 0.875f)))) {
                if(is(min,0.5f) && is(max,0.5f)){
                    right.add(quad);
                    left.add(quad);
                }
                else if(gte(min,0.5f) && gte(max,0.5f)){
                    right.add(quad);
                }
                else{
                    left.add(quad);
                }
            }
            else{
                post.add(quad);
            }
        }

        return new FenceGate(post, left, right);
    }

    private boolean is(float value, float target) {
        return Math.abs(value - target) < EPSILON;
    }

    private boolean gte(float value, float target) {
        return value > target - EPSILON;
    }

    private boolean lte(float value, float target) {
        return value < target + EPSILON;
    }

    public class FenceGate{
        List<BakedQuad> postQuadList;
        List<BakedQuad> leftQuadList;
        List<BakedQuad> rightQuadList;

        public FenceGate(List<BakedQuad> postQuadList, List<BakedQuad> leftQuadList, List<BakedQuad> rightQuadList){
            this.postQuadList = postQuadList;
            this.leftQuadList = leftQuadList;
            this.rightQuadList = rightQuadList;
        }
    }
}
