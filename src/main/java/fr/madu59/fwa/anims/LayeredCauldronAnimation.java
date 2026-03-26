package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.mixin.GetContentHeightInvoker;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LayeredCauldronAnimation extends Animation{

    private final BlockState oldBlockState;
    private final BlockState newBlockState;
    private final boolean isInverted;
    private final RandomSource random;
    private final BakedModel model;
    
    public LayeredCauldronAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        if (newState.getBlock() instanceof CauldronBlock){
            newBlockState = oldState;
            oldBlockState = newState;
            isInverted = true;
        }
        else{
            newBlockState = newState;
            oldBlockState = oldState;
            isInverted = false;
        }

        random = RandomSource.create(newBlockState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(newBlockState);
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.CAULDRON_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.CAULDRON_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.CAULDRON_STATE.getValue();
    }

    private float getPosition(double nowTick, double newPos, double oldPos){
        if (isInverted) return (float)((oldPos-newPos) * Curves.ease(getProgress(nowTick), getCurve()));
        else return (float)((oldPos-newPos)-(oldPos-newPos) * Curves.ease(getProgress(nowTick), getCurve()));
    }

    private double getHeight(BlockState state){
        if (state.getBlock() instanceof LayeredCauldronBlock) return ((GetContentHeightInvoker) state.getBlock()).fwa$getContentHeight(state);
        if (state.getBlock() instanceof LavaCauldronBlock) return 15.0/16.0;
        return 4.0/16.0;
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

        float dy = getPosition(context.getNowTick(), getHeight(newBlockState), getHeight(oldBlockState));
        poseStack.translate(0,dy,0);

        renderFilteredQuads(poseStack, buffer, model.getQuads(newState, null, random), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, model.getQuads(newState, dir, random), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantLiquid, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.getSprite().contents().name().getPath();
            String last = path.split("/")[path.split("/").length-1];
            if ((path.contains("cauldron") && !last.contains("liquid") && !last.contains("water")) != wantLiquid){
                float r = 1.0f, g = 1.0f, b = 1.0f;

                if (quad.isTinted()) {
                    int color = Minecraft.getInstance().getBlockColors().getColor(newBlockState, Minecraft.getInstance().level, position, quad.getTintIndex());
                    r = (float) (color >> 16 & 255) / 255.0F;
                    g = (float) (color >> 8 & 255) / 255.0F;
                    b = (float) (color & 255) / 255.0F;
                }

                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, r, g, b, light);
            }
        }
    }
}
