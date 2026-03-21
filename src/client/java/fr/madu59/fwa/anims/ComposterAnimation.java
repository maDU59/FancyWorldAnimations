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
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ComposterAnimation extends Animation{

    private final BlockState oldState;
    private final BlockState newState;
    private final BlockStateModel model;
    private List<BlockModelPart> parts = new ArrayList<>();

    
    public ComposterAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newBlockState, BlockState oldBlockState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);

        newState = newBlockState;
        oldState = oldBlockState;

        RandomSource random = RandomSource.create(newState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(newState);
        model.collectParts(random, parts);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.COMPOSTER_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.COMPOSTER_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.COMPOSTER_STATE.getValue();
    }

    private float getPosition(double nowTick, double newPos, double oldPos){
        return (float)((oldPos-newPos)-(oldPos-newPos) * Curves.ease(getProgress(nowTick), getCurve()));
    }

    private double getHeight(BlockState state){
        return Math.min((state.getValue(BlockStateProperties.LEVEL_COMPOSTER) * 2.0 + 1.0) / 16.0, 15.0/16.0);
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = context.getBufferSource().getBuffer(RenderType.cutoutMipped());
        
        for(BlockModelPart part: parts){
            renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
            }

            float dy = getPosition(context.getNowTick(), getHeight(newState), getHeight(oldState));
            poseStack.translate(0,dy,0);

            renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light);
            }
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantCompost, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.sprite().contents().name().getPath();
            if ((path.endsWith("_compost") || path.contains("_ready")) == wantCompost) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
            }
        }
    }
}
