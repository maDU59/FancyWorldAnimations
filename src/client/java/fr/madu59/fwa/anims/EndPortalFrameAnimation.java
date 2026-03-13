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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EndPortalFrameAnimation extends Animation{

    private List<BlockModelPart> parts = new ArrayList<>();
    private final BlockStateModel model;
    private List<BlockModelPart> eyeParts = new ArrayList<>();
    private final BlockStateModel eyeModel;
    
    public EndPortalFrameAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        model.collectParts(random, parts);
        BlockState eyeState = defaultState.setValue(EndPortalFrameBlock.HAS_EYE, true);
        RandomSource eyeRandom = RandomSource.create(eyeState.getSeed(position));
        eyeModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(eyeState);
        eyeModel.collectParts(eyeRandom, eyeParts);
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
    public boolean isEnabled(){
        return SettingsManager.END_PORTAL_FRAME_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock(){
        return newIsOpen;
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.END_PORTAL_FRAME_INFINITE.getValue();
    }

    @Override
    public double getLifeSpan(){
        return !(hasInfiniteAnimation()  && !newIsOpen)? getAnimDuration() : Double.MAX_VALUE;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        BlockModelPart part = eyeParts.get(0);

        if(newIsOpen){

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
            RenderHelper.renderModel(buffer, poseStack.last(), parts, 1f, 1f, 1f, 1f, light);
            poseStack.translate(0f,2f/8f - (float)Curves.ease(getProgress(context.getNowTick()), getCurve())/4f,0f);
            renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light, 1f, 1f, 1f, 1f);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light, 1f, 1f, 1f, 1f);
            }

        }
        else{

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucentMovingBlock());
            float time = (float)(context.getNowTick() - this.startTick);
            float alpha = 0.1f + Math.abs((float)Math.sin(time * 0.07)) * 0.3f;

            renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light, 0f, 1f, 0f, alpha);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light, 0f, 1f, 0f, alpha);
            }

        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantEye, int light, float r, float g, float b, float a) {
        for (BakedQuad quad : quads) {
            String path = quad.sprite().contents().name().getPath();
            if (path.contains("eye") == wantEye) {
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, a, r, g, b, light);
            }
        }
    }
}
