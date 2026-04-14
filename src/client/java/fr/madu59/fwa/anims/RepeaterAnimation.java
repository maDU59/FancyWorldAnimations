package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Backport;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RepeaterAnimation extends Animation{

    private final RandomSource random;
    private final BakedModel model;
    
    public RepeaterAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.REPEATER_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.REPEATER_EASING.getValue();
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.REPEATER_STATE.getValue();
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.DELAY, 1);
    }

    private float getPosition(double nowTick, int newPos, int oldPos){
        return (float)(oldPos + (newPos-oldPos) * Curves.ease(getProgress(nowTick), getCurve()));
    }


    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        Direction facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = RenderHelper.getBuffer();

        renderFilteredQuads(poseStack, buffer, model.getQuads(defaultState, null, random), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, model.getQuads(defaultState, dir, random), false, light);
        }

        float dx = getPosition(context.getNowTick(), newState.getValue(BlockStateProperties.DELAY), oldState.getValue(BlockStateProperties.DELAY));
        dx = (dx-1)*2f/16f * facing.getAxisDirection().getStep();
        if (facing.getAxis() == Axis.X) poseStack.translate(dx,0,0);
        else poseStack.translate(0,0,dx);

        renderFilteredQuads(poseStack, buffer, model.getQuads(defaultState, null, random), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, model.getQuads(defaultState, dir, random), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantTorch, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.getSprite().contents().name().getPath();
            if ((path.contains("redstone_torch") && (Backport.getPos(quad, 0).x() > 5f/16f && Backport.getPos(quad, 0).x() < 11f/16f || Backport.getPos(quad, 2).x() > 5f/16f && Backport.getPos(quad, 2).x() < 11f/16f) && (Backport.getPos(quad, 0).z() > 5f/16f && Backport.getPos(quad, 0).z()  < 11f/16f || Backport.getPos(quad, 2).z() > 5f/16f && Backport.getPos(quad, 2).z() < 11f/16f)) == wantTorch) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
            }
        }
    }
}
