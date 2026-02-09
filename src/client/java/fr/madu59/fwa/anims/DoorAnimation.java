package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.AABB;

public class DoorAnimation extends Animation{

    public DoorAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 10 * SettingsManager.DOOR_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.DOOR_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.DOOR_STATE.getValue();
    }

    private double getStartAngle(boolean isOpen, DoorHingeSide hinge){
        if (!isOpen) return 0.0f;
        return (hinge == DoorHingeSide.RIGHT) ? -90.0f : 90.0f;
    }

    private double getAngle(double nowTick, DoorHingeSide hinge) {
        double angle1 = getStartAngle(this.oldIsOpen, hinge);
        double angle2 = getStartAngle(this.newIsOpen, hinge);
        return angle1 + (angle2 - angle1) * Curves.ease(getProgress(nowTick), getCurve());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = defaultState.getValue(DoorBlock.HINGE);

        Direction hingeSide = (hinge == DoorHingeSide.RIGHT)
                ? facing.getClockWise(Direction.Axis.Y)
                : facing.getCounterClockWise(Direction.Axis.Y);

        AABB boundingBox = defaultState.getShape(Minecraft.getInstance().level, BlockPos.ZERO).bounds();

        float pivotX = (float) ((boundingBox.minX + boundingBox.maxX) * 0.5);
        float pivotZ = (float) ((boundingBox.minZ + boundingBox.maxZ) * 0.5);

        switch (hingeSide) {
            case EAST -> pivotX = (float) boundingBox.maxX;
            case WEST -> pivotX = (float) boundingBox.minX;
            case SOUTH -> pivotZ = (float) boundingBox.maxZ;
            case NORTH -> pivotZ = (float) boundingBox.minZ;
            default -> {}
        }

        float thickness = (facing == Direction.NORTH || facing == Direction.SOUTH)
                ? (float) (boundingBox.maxZ - boundingBox.minZ)
                : (float) (boundingBox.maxX - boundingBox.minX);

        float dX = 0.0f;
        float dZ = 0.0f;
        switch (facing) {
            case NORTH -> dZ = thickness * 0.5f;
            case SOUTH -> dZ = -thickness * 0.5f;
            case WEST -> dX = thickness * 0.5f;
            case EAST -> dX = -thickness * 0.5f;
            default -> {}
        }

        double angle = getAngle(nowTick, hinge);
        double rad = Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float rotDX = dX * cos - dZ * sin;
        float rotDZ = dX * sin + dZ * cos;
        float shiftX = dX - rotDX;
        float shiftZ = dZ - rotDZ;

        poseStack.translate(shiftX, 0.0f, shiftZ);
        poseStack.translate(pivotX, 0.0f, pivotZ);
        poseStack.mulPose(Axis.YP.rotationDegrees((float)angle));
        poseStack.translate(-pivotX, 0.0f, -pivotZ);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
