package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RepeaterAnimation extends Animation{

    private final RandomSource random = RandomSource.create(42);
    private final BlockStateModel model;
    private List<BlockStateModelPart> parts = new ArrayList<>();
    
    public RepeaterAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(defaultState);
        model.collectParts(random, parts);
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
    public boolean isEnabled(){
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
        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = RenderHelper.getBuffer();
        BlockStateModelPart part = parts.get(0);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
        }

        float dx = getPosition(context.getNowTick(), newState.getValue(BlockStateProperties.DELAY), oldState.getValue(BlockStateProperties.DELAY));
        dx = (dx-1)*2f/16f * facing.getAxisDirection().getStep();
        if (facing.getAxis() == Axis.X) poseStack.translate(dx,0,0);
        else poseStack.translate(0,0,dx);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantTorch, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.materialInfo().sprite().contents().name().getPath();
            if ((path.contains("redstone_torch") && quad.position0().x() > 5f/16f && quad.position0().x() < 11f/16f && quad.position2().x() > 5f/16f && quad.position2().x() < 11f/16f && quad.position0().z() > 5f/16f && quad.position0().z()  < 11f/16f && quad.position2().z() > 5f/16f && quad.position2().z() < 11f/16f) == wantTorch) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
            }
        }
    }
}
