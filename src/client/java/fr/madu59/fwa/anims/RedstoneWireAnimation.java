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
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RedstoneWireAnimation extends Animation{

    private List<BlockModelPart> parts = new ArrayList<>();
    private final BlockStateModel model;
    private final float newR;
    private final float newG;
    private final float newB;
    private final float oldR;
    private final float oldG;
    private final float oldB;
    
    public RedstoneWireAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        model.collectParts(random, parts);

        int newColor = RedStoneWireBlock.getColorForPower(newState.getValue(BlockStateProperties.POWER));
        newR = (float) (newColor >> 16 & 255) / 255.0F;
        newG = (float) (newColor >> 8 & 255) / 255.0F;
        newB = (float) (newColor & 255) / 255.0F;
        int oldColor = RedStoneWireBlock.getColorForPower(oldState.getValue(BlockStateProperties.POWER));
        oldR = (float) (oldColor >> 16 & 255) / 255.0F;
        oldG = (float) (oldColor >> 8 & 255) / 255.0F;
        oldB = (float) (oldColor & 255) / 255.0F;
    }

    @Override
    public double getAnimDuration() {
        return 3 / SettingsManager.REDSTONE_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.REDSTONE_EASING.getValue();
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.REDSTONE_STATE.getValue();
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.POWER, 0);
    }

    private float[] getColor(double nowTick, float newR, float newG, float newB, float oldR, float oldG, float oldB){
        double progress = Curves.ease(getProgress(nowTick), getCurve());
        return new float[]{
            (float)(oldR + (newR-oldR) * progress),
            (float)(oldG + (newG-oldG) * progress),
            (float)(oldB + (newB-oldB) * progress)
        };
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
    
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        VertexConsumer buffer = RenderHelper.getBuffer();

        float[] color = getColor(context.getNowTick(), newR, newG, newB, oldR, oldG, oldB);

        RenderHelper.renderModel(buffer, poseStack.last(), parts, 1.0f, color[0], color[1], color[2], light);
    }
}
