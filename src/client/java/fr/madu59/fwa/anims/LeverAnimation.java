package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Backport;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.ModelSplitHelper;
import fr.madu59.fwa.utils.ModelSplitHelper.Lever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class LeverAnimation extends Animation{
    
    private final BlockStateModel model;
    private List<BlockModelPart> parts = new ArrayList<>();

    public LeverAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        model.collectParts(random, parts);
    }

    @Override
    public double getAnimDuration() {
        return 5 / SettingsManager.LEVER_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.LEVER_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.LEVER_STATE.getValue();
    }

    private double getStartAngle(boolean isOpen){
        if (!isOpen) return 0f;
        return 90f;
    }

    private double getAngle(double nowTick, Direction facing) {
        double angle1 = getStartAngle(this.oldIsOpen);
        double angle2 = getStartAngle(this.newIsOpen);
        double finalAngle = angle1 + (angle2 - angle1) * Curves.ease(getProgress(nowTick), getCurve());
        if(facing == Direction.NORTH || facing == Direction.EAST){
            finalAngle = -finalAngle;
        }
        return finalAngle;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        Direction facing = defaultState.getValue(LeverBlock.FACING);
        AttachFace face = defaultState.getValue(LeverBlock.FACE);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = context.getBufferSource().getBuffer(RenderType.cutoutMipped());
        BlockModelPart part = parts.get(0);

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            quads.addAll(part.getQuads(dir));
        }
        quads.addAll(part.getQuads(null));

        Lever lever = splitLeverQuads(quads, facing, face);

        RenderHelper.renderQuads(buffer, poseStack.last(), lever.baseQuadList(), 1f, 1f, 1f, 1f, light);

        double angle = getAngle(context.getNowTick(), facing);

        float pivotX = 0.5f;
        float pivotY = lever.pivot();
        float pivotZ = 0.5f;
        Axis axis = Axis.XP;
        if (face == AttachFace.FLOOR){
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZP;
                    break;
                default:
                    break;
            }
        }
        else if (face == AttachFace.CEILING){
            pivotY = 1f - pivotY;
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZN;
                    break;
                default:
                    axis = Axis.XN;
                    break;
            }
        }
        else if (face == AttachFace.WALL){
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZP;
                    pivotX = (facing == Direction.WEST) ? 1f - pivotY : pivotY;
                    pivotZ = 0.5f;
                    break;
                default:
                    axis = Axis.XP;
                    pivotX = 0.5f;
                    pivotZ = (facing == Direction.NORTH) ? 1f - pivotY : pivotY;
                    break;
            }
            pivotY = 0.5f;
        }

        poseStack.translate(pivotX, pivotY, pivotZ);

        poseStack.mulPose(axis.rotationDegrees((float)angle));
            
        poseStack.translate(-pivotX, -pivotY, -pivotZ);

        RenderHelper.renderQuads(buffer, poseStack.last(), lever.handleQuadList(), 1f, 1f, 1f, 1f, light);
    }

    public Lever splitLeverQuads(List<BakedQuad> quads, Direction facing, AttachFace face){

        List<BakedQuad> base = new ArrayList<>();
        List<BakedQuad> handle = new ArrayList<>();

        if (SettingsManager.LEVER_SPLIT.getValue() == ModelSplitHelper.SPLIT_METHOD.MODEL){

            for (BakedQuad quad : quads) {
                
                Vector3fc pos1 = Backport.getPos(quad, 0);
                Vector3fc pos2 = Backport.getPos(quad, 1);

                Vector3f edge1 = new Vector3f();

                pos2.sub(pos1, edge1);

                edge1.normalize();

                if (ModelSplitHelper.isAxisAligned(edge1)){
                    base.add(quad);
                }
                else{
                    handle.add(quad);
                }
            }
        }
        else{
            for (BakedQuad quad : quads) {
                String path = quad.sprite().contents().name().getPath();
                if ((path.contains("lever") && !path.contains("base") && !path.contains("cobblestone") && !path.contains("side")) || path.contains("handle")) {
                    handle.add(quad);
                }
                else{
                    base.add(quad);
                }
            }
        }

        float pivot = 0.0625f;

        if (handle.size() > 4 && handle.size() < 7){

            Vector3f middlePoint = null;
            Vector3f handleVector = null;

            for (BakedQuad quad : handle) {
                Vector3f edge1 = new Vector3f();
                Vector3f edge2 = new Vector3f();

                Backport.getPos(quad, 1).sub(Backport.getPos(quad, 0), edge1);
                Backport.getPos(quad, 2).sub(Backport.getPos(quad, 1), edge2);

                if(Math.abs(edge1.lengthSquared() - edge2.lengthSquared()) < 0.001f){
                    middlePoint = ModelSplitHelper.middlePoint(quad);
                }
                else{
                    if (edge1.lengthSquared() > edge2.lengthSquared()) handleVector = edge1;
                    else handleVector = edge2;
                }
            }

            if (handleVector != null && middlePoint != null){

                if (face == AttachFace.FLOOR){
                    float t;
                    if(Math.abs(handleVector.z()) < 0.001f){
                        t = (0.5f - middlePoint.x()) / handleVector.x();
                    }
                    else{
                        t = (0.5f - middlePoint.z()) / handleVector.z();
                    }
                    handleVector.mul(t).add(middlePoint);
                    pivot = handleVector.y;
                }
                else if (face == AttachFace.CEILING){
                    float t;
                    if(Math.abs(handleVector.z()) < 0.001f){
                        t = (0.5f - middlePoint.x()) / handleVector.x();
                    }
                    else{
                        t = (0.5f - middlePoint.z()) / handleVector.z();
                    }
                    handleVector.mul(t).add(middlePoint);
                    pivot = 1-handleVector.y;
                }
                else{
                    if (facing.getAxis() == net.minecraft.core.Direction.Axis.X){
                        float t;
                        if(Math.abs(handleVector.z()) < 0.001f){
                            t = (0.5f - middlePoint.y()) / handleVector.y();
                        }
                        else{
                            t = (0.5f - middlePoint.z()) / handleVector.z();
                        }
                        handleVector.mul(t).add(middlePoint);
                        if(facing == Direction.WEST) pivot = 1-handleVector.x;
                        else pivot = handleVector.x;
                    }
                    else{
                        float t;
                        if(Math.abs(handleVector.y()) < 0.001f){
                            t = (0.5f - middlePoint.x()) / handleVector.x();
                        }
                        else{
                            t = (0.5f - middlePoint.y()) / handleVector.y();
                        }
                        handleVector.mul(t).add(middlePoint);
                        if(facing == Direction.NORTH) pivot = 1-handleVector.z;
                        else pivot = handleVector.z;
                    }
                }
            }
        }

        return new Lever(base, handle, pivot);
    }

    public static Vector3f[] getFurthestPair(List<Vector3f> centers) {
        Vector3f point1 = null;
        Vector3f point2 = null;
        float maxDistSq = 0f;

        for (int i = 0; i < centers.size(); i++) {
            for (int j = i + 1; j < centers.size(); j++) {
                Vector3f pA = centers.get(i);
                Vector3f pB = centers.get(j);
                
                float distSq = pA.distanceSquared(pB);
                
                if (distSq > maxDistSq) {
                    maxDistSq = distSq;
                    point1 = pA;
                    point2 = pB;
                }
            }
        }

        return new Vector3f[]{point1, point2};
    }
}
