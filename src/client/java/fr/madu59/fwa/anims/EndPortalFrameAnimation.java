package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EndPortalFrameAnimation extends Animation{

    private final RandomSource random = RandomSource.create(42);
    
    public EndPortalFrameAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 10 * SettingsManager.END_PORTAL_FRAME_SPEED.getValue();
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
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double nowTick) {

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        BlockState eyeState = defaultState.setValue(EndPortalFrameBlock.HAS_EYE, true);

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(eyeState);

        if(newIsOpen){

            VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(eyeState, true));
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0f,2f/8f - (float)Curves.ease(getProgress(nowTick), getCurve())/4f,0f);
            renderFilteredQuads(poseStack, buffer, model.getQuads(eyeState, null, random), true, light, 1f, 1f, 1f, 1f);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, model.getQuads(eyeState, dir, random), true, light, 1f, 1f, 1f, 1f);
            }

        }
        else{

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucentMovingBlock());
            float time = (float)(nowTick - this.startTick);
            float alpha = 0.1f + Math.abs((float)Math.sin(time * 0.07)) * 0.3f;

            renderFilteredQuads(poseStack, buffer, model.getQuads(eyeState, null, random), true, light, 0f, 1f, 0f, alpha);
            for(Direction dir : Direction.values()){
                renderFilteredQuads(poseStack, buffer, model.getQuads(eyeState, dir, random), true, light, 0f, 1f, 0f, alpha);
            }

        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantEye, int light, float r, float g, float b, float a) {
        for (BakedQuad quad : quads) {
            String path = quad.getSprite().contents().name().getPath();
            if (path.contains("eye") == wantEye) {
                buffer.putBulkData(poseStack.last(), quad, r, g, b, a, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
