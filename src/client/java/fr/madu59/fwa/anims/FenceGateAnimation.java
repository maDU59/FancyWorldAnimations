package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.ModelSplitHelper;
import fr.madu59.fwa.utils.ModelSplitHelper.FenceGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FenceGateAnimation extends Animation{

    private List<BlockStateModelPart> parts = new ArrayList<>();
    private final BlockStateModel model;


    public FenceGateAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(defaultState);
        model.collectParts(random, parts);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.FENCEGATE_SPEED.getValue();
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
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        Direction facing = defaultState.getValue(FenceGateBlock.FACING);

        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = context.getBufferSource().getBuffer(RenderTypes.cutoutMovingBlock());

        BlockStateModelPart part = parts.get(0);

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            quads.addAll(part.getQuads(dir));
        }
        quads.addAll(part.getQuads(null));

        FenceGate fenceGate = splitFenceGateQuads(quads, facing);

        renderQuads(poseStack, buffer, fenceGate.postQuadList(), light);

        boolean onAxisZ = (facing.getAxis() == Axis.Z);
        float leftPivotX = onAxisZ ? (1.0f / 16.0f) : 0.5f;
        float leftPivotZ = onAxisZ ? 0.5f : (1.0f / 16.0f);
        float rightPivotX = onAxisZ ? (15.0f / 16.0f) : 0.5f;
        float rightPivotZ = onAxisZ ? 0.5f : (15.0f / 16.0f);

        float angle = (float)getAngle(context.getNowTick(), facing);
        float leftAngle = onAxisZ ? -angle : angle;
        float rightAngle = onAxisZ ? angle : -angle;

        poseStack.translate(leftPivotX, 0.0f, leftPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(leftAngle));
        poseStack.translate(-leftPivotX, 0.0f, -leftPivotZ);
        renderQuads(poseStack, buffer, fenceGate.leftQuadList(), light);

        poseStack.translate(leftPivotX, 0.0f, leftPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-leftAngle));
        poseStack.translate(-leftPivotX, 0.0f, -leftPivotZ);

        poseStack.translate(rightPivotX, 0.0f, rightPivotZ);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rightAngle));
        poseStack.translate(-rightPivotX, 0.0f, -rightPivotZ);
        renderQuads(poseStack, buffer, fenceGate.rightQuadList(), light);
    }

    private void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, int light) {
        for (BakedQuad quad : quads) {
            RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
        }
    }

    public FenceGate splitFenceGateQuads(List<BakedQuad> quads, Direction facing){

        List<BakedQuad> post = new ArrayList<>();
        List<BakedQuad> left = new ArrayList<>();
        List<BakedQuad> right = new ArrayList<>();

        for (BakedQuad quad : quads) {
            
            Vector3fc pos1 = quad.position0();
            Vector3fc pos2 = quad.position1();
            Vector3fc pos3 = quad.position2();
            Vector3fc pos4 = quad.position3();

            float min, max;
            if(facing.getAxis() == Axis.X){
                min = Math.min(pos1.z(), Math.min(pos2.z(), Math.min(pos3.z(), pos4.z())));
                max = Math.max(pos1.z(), Math.max(pos2.z(), Math.max(pos3.z(), pos4.z())));
            }
            else{
                min = Math.min(pos1.x(), Math.min(pos2.x(), Math.min(pos3.x(), pos4.x())));
                max = Math.max(pos1.x(), Math.max(pos2.x(), Math.max(pos3.x(), pos4.x())));
            }

            if (ModelSplitHelper.gte(min, 0.125f) && ModelSplitHelper.lte(max, 0.875f) && !(ModelSplitHelper.is(max,min) && (ModelSplitHelper.lte(max,0.125f) || ModelSplitHelper.gte(max, 0.875f)))) {
                if(ModelSplitHelper.is(min,0.5f) && ModelSplitHelper.is(max,0.5f)){
                    right.add(quad);
                    left.add(quad);
                }
                else if(ModelSplitHelper.gte(min,0.5f) && ModelSplitHelper.gte(max,0.5f)){
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
}
