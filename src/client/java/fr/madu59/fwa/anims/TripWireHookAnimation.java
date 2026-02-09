package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.config.SettingsManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TripWireHookAnimation extends Animation{
    
    public TripWireHookAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
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
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {
        //Direction facing = defaultState.getValue(TripWireHookBlock.FACING);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
