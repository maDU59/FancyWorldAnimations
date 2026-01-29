package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class Animation {

    protected final BlockPos position;
    protected final double startTick;
    protected final boolean oldIsOpen;
    protected final boolean newIsOpen;
    protected final BlockState defaultState;

    public Animation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        this.position = position;
        this.defaultState = defaultState;
        this.startTick = startTick;
        this.oldIsOpen = oldIsOpen;
        this.newIsOpen = newIsOpen;
    }

    public boolean isUnique() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) Curves.Classic.LINEAR;
    }

    public boolean hideOriginalBlock() {
        return true;
    }

    public boolean hideOriginalBlockEntity() {
        return true;
    }

    public BlockPos getPos() {
        return position;
    }

    public double getStartTick() {
        return startTick;
    }

    public boolean isFinished(double nowTick) {
        return nowTick - startTick >= getLifeSpan();
    }

    public double getAnimDuration() {
        return 0;
    }

    public double getLifeSpan(){
        return getAnimDuration();
    }

    public double getProgress(double nowTick) {
        return Math.clamp((nowTick - this.startTick) / getAnimDuration(), 0.0, 1.0);
    }

    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
