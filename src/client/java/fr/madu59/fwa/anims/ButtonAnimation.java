package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ButtonAnimation extends Animation{

    private final BakedModel model;
    private final RandomSource random;
    
    public ButtonAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
    }

    @Override
    public double getAnimDuration() {
        return 3 / SettingsManager.BUTTON_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.BUTTON_EASING.getValue();
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.BUTTON_STATE.getValue();
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.POWERED, false);
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
        Direction facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        AttachFace face = defaultState.getValue(BlockStateProperties.ATTACH_FACE);

        float x = 0f;
        float y = (float)Curves.ease(getProgress(context.getNowTick()), getCurve())/16f;
        y = newIsOpen? -y:-1f/16f + y;
        float z = 0f;

        if(face == AttachFace.CEILING) y = -y;
        else if(face == AttachFace.WALL){
            if(facing.getAxis() == Axis.X){
                if(facing == Direction.WEST) y = -y;
                x = y;
                y = 0f;
            }
            if(facing.getAxis() == Axis.Z){
                if(facing == Direction.NORTH) y = -y;
                z = y;
                y = 0f;
            }
        }

        poseStack.translate(x, y, z);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        VertexConsumer buffer = RenderHelper.getBuffer();
        RenderHelper.renderModel(buffer, poseStack.last(), model, 1.0f, 1.0f, 1.0f, 1.0f, light, random, defaultState);
    }
}
