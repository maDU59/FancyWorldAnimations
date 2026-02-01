package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrapDoorAnimation extends Animation{
    
    public TrapDoorAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 5 * Curves.getSpeedCoeff(SettingsManager.TRAPDOOR_SPEED.getValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.TRAPDOOR_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.TRAPDOOR_STATE.getValue();
    }

    private double getStartAngle(boolean isOpen, Direction hingeSide){
        if (!isOpen) return 0.0f;
        return (hingeSide == Direction.NORTH || hingeSide == Direction.EAST) ? 90.0f : -90.0f;
    }

    private double getAngle(double nowTick, Direction hingeSide) {
        double angle1 = getStartAngle(this.oldIsOpen, hingeSide);
        double angle2 = getStartAngle(this.newIsOpen, hingeSide);
        return angle1 + (angle2 - angle1) * Curves.ease(getProgress(nowTick), getCurve());
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(TrapDoorBlock.FACING);
        Half half = defaultState.getValue(TrapDoorBlock.HALF);

        VoxelShape collShape = defaultState.getCollisionShape(Minecraft.getInstance().level, BlockPos.ZERO);
        AABB boundingBox = collShape.isEmpty() ? defaultState.getShape(Minecraft.getInstance().level, BlockPos.ZERO).bounds() : collShape.bounds();

        Direction hingeSide = facing.getOpposite();
        double angle = getAngle(nowTick, hingeSide);
        if (half == Half.BOTTOM) angle = -angle;

        float pivotX = (float) ((boundingBox.minX + boundingBox.maxX) * 0.5);
        float pivotZ = (float) ((boundingBox.minZ + boundingBox.maxZ) * 0.5);

        switch (hingeSide) {
            case EAST -> pivotX = (float) boundingBox.maxX;
            case WEST -> pivotX = (float) boundingBox.minX;
            case SOUTH -> pivotZ = (float) boundingBox.maxZ;
            case NORTH -> pivotZ = (float) boundingBox.minZ;
            default -> {}
        }
        float pivotY = (half == Half.TOP) ? (float) boundingBox.maxY : (float) boundingBox.minY;

        float thickness = (float) (boundingBox.maxY - boundingBox.minY);
        float halfThickness = thickness > 0.0f ? thickness * 0.5f : (3.0f / 32.0f);
        float dY = (half == Half.TOP) ? -halfThickness : halfThickness;

        double rad = Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float shiftX = 0.0f;
        float shiftY = 0.0f;
        float shiftZ = 0.0f;
        if (hingeSide == Direction.NORTH || hingeSide == Direction.SOUTH) {
            float rotY = dY * cos;
            float rotZ = dY * sin;
            shiftY = dY - rotY;
            shiftZ = -rotZ;
        } else {
            float rotX = -dY * sin;
            float rotY = dY * cos;
            shiftX = -rotX;
            shiftY = dY - rotY;
        }

        poseStack.translate(shiftX, shiftY, shiftZ);
        poseStack.translate(pivotX, pivotY, pivotZ);
        if (hingeSide == Direction.NORTH || hingeSide == Direction.SOUTH) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float)angle));
        } else {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float)angle));
        }
        poseStack.translate(-pivotX, -pivotY, -pivotZ);
        poseStack.translate(-shiftX, -shiftY, -shiftZ);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
