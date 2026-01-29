package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EndPortalFrameAnimation extends Animation{
    
    public EndPortalFrameAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 10;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) Curves.Classic.LINEAR;
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {
        
    }
}
