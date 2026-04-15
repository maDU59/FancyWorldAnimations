package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DripleafAnimation extends Animation{

    private final BakedModel model;
    private final RandomSource random;

    
    public DripleafAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        random = RandomSource.create(newState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(newState);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.DRIPLEAF_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.DRIPLEAF_EASING.getValue();
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.DRIPLEAF_STATE.getValue();
    }

    private float getRotation(double nowTick, double newRot, double oldRot){
        return (float)((oldRot-newRot)-(oldRot-newRot) * Curves.ease(getProgress(nowTick), getCurve()));
    }

    private double getRotation(BlockState state){
        switch(state.getValue(BlockStateProperties.TILT)){
            case UNSTABLE: return 0;
            case PARTIAL: return 22.5;
            case FULL: return 45;
            default: return 0;
        }
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = RenderHelper.getBuffer();

        renderFilteredQuads(poseStack, buffer, model.getQuads(newState, null, random), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, model.getQuads(newState, dir, random), false, light);
        }

        float tiltAngle = getRotation(context.getNowTick(), getRotation(newState), getRotation(oldState));
        Direction facing = newState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        poseStack.translate(0.5, 0.5, 0.5);
        float yRot = -facing.toYRot(); 
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0.5, 0.9375, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(tiltAngle));
        poseStack.translate(-0.5, -0.9375, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.translate(-0.5, -0.5, -0.5);

        renderFilteredQuads(poseStack, buffer, model.getQuads(newState, null, random), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, model.getQuads(newState, dir, random), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantLeaf, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.getSprite().contents().name().getPath();
            if (!path.contains("_stem") == wantLeaf) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
            }
        }
    }
}
