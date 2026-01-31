package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class ButtonAnimation extends Animation{
    
    public ButtonAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 3;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.BUTTON_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.BUTTON_STATE.getValue();
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {
        Direction facing = defaultState.getValue(ButtonBlock.FACING);
        AttachFace face = defaultState.getValue(ButtonBlock.FACE);

        float x = 0f;
        float y = (float)Curves.ease(getProgress(nowTick), getCurve())/16f;
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
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
