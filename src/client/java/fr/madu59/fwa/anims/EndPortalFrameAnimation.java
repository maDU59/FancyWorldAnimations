package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class EndPortalFrameAnimation extends Animation{

    private final RandomSource random;
    private final BakedModel model;
    private final RandomSource eyeRandom;
    private final BakedModel eyeModel;
    private final BlockState eyeState;
    
    public EndPortalFrameAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        eyeState = defaultState.setValue(BlockStateProperties.EYE, true);
        eyeRandom = RandomSource.create(eyeState.getSeed(position));
        eyeModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(eyeState);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.END_PORTAL_FRAME_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.END_PORTAL_FRAME_EASING.getValue();
    }

    @Override
    public boolean isEnabled(BlockState states){
        return SettingsManager.END_PORTAL_FRAME_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock(){
        return newIsOpen;
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.EYE, false);
    }

    public boolean hasInfiniteAnimation(){
        return SettingsManager.END_PORTAL_FRAME_INFINITE.getValue();
    }

    @Override
    public double getLifeSpan(){
        return !(hasInfiniteAnimation()  && !newIsOpen)? getAnimDuration() : Double.MAX_VALUE;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        if(newIsOpen){

            VertexConsumer buffer = RenderHelper.getBuffer();
            RenderHelper.renderModel(buffer, poseStack.last(), model, 1f, 1f, 1f, 1f, light, random, defaultState);
            poseStack.translate(0f,2f/8f - (float)Curves.ease(getProgress(context.getNowTick()), getCurve())/4f,0f);
            if(ModCompat.isEndRemasteredLoaded() && ModCompat.EndRemasteredCompat.isEndRemasteredPortal(defaultState)){
                ModCompat.EndRemasteredCompat.renderEndPortalFrameAnimation(context, poseStack, position, light);
            }
            else{
                renderFilteredQuads(poseStack, buffer, eyeModel.getQuads(eyeState, null, eyeRandom), true, light, 1f, 1f, 1f, 1f);
                for(Direction dir : Direction.values()){
                    renderFilteredQuads(poseStack, buffer, eyeModel.getQuads(eyeState, dir, eyeRandom), true, light, 1f, 1f, 1f, 1f);
                }
            }

        }
        else{

            VertexConsumer buffer = RenderHelper.getBuffer(RenderType.translucentMovingBlock());
            float time = (float)(context.getNowTick() - this.startTick);
            float alpha = 0.1f + Math.abs((float)Math.sin(time * 0.07)) * 0.3f;

            renderFilteredQuads(poseStack, buffer, eyeModel.getQuads(eyeState, null, eyeRandom), true, light, 0f, 1f, 0f, alpha);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, eyeModel.getQuads(eyeState, dir, eyeRandom), true, light, 0f, 1f, 0f, alpha);
            }

        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantEye, int light, float r, float g, float b, float a) {
        for (BakedQuad quad : quads) {
            String path = quad.getSprite().contents().name().getPath();
            if (path.contains("eye") == wantEye) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, a, r, g, b, light);
            }
        }
    }
}
