package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class LeverAnimation extends Animation{
    
    public LeverAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
    }

    @Override
    public double getAnimDuration() {
        return 5;
    }

    private double getStartAngle(boolean isOpen){
        if (!isOpen) return 0f;
        return 90f;
    }

    private double getAngle(double nowTick, Direction facing) {
        double angle1 = getStartAngle(this.oldIsOpen);
        double angle2 = getStartAngle(this.newIsOpen);
        double finalAngle = angle1 + (angle2 - angle1) * getProgress(nowTick);
        if(facing == Direction.NORTH || facing == Direction.EAST){
            finalAngle = -finalAngle;
        }
        return finalAngle;
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {
        RandomSource random = RandomSource.create(42);

        Direction facing = defaultState.getValue(LeverBlock.FACING);
        AttachFace face =defaultState.getValue(LeverBlock.FACE);
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(defaultState));
        BlockModelPart part = model.collectParts(random).get(0);
        renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
        }

        double angle = getAngle(nowTick, facing);

        float pivotX = 0.5f;
        float pivotY = 0.0625f;
        float pivotZ = 0.5f;
        Axis axis = Axis.XP;
        if (face == AttachFace.FLOOR){
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZP;
                    break;
                default:
                    break;
            }
        }
        else if (face == AttachFace.CEILING){
            pivotY = 1f - 0.0625f;
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZN;
                    break;
                default:
                    axis = Axis.XN;
                    break;
            }
        }
        else if (face == AttachFace.WALL){
            pivotY = 0.5f;
            switch (facing) {
                case EAST, WEST:
                    axis = Axis.ZP;
                    pivotX = (facing == Direction.WEST) ? 1f - 0.0625f : 0.0625f;
                    pivotZ = 0.5f;
                    break;
                default:
                    axis = Axis.XP;
                    pivotX = 0.5f;
                    pivotZ = (facing == Direction.NORTH) ? 1f - 0.0625f : 0.0625f;
                    break;
            }
        }

        poseStack.translate(pivotX, pivotY, pivotZ);

        poseStack.mulPose(axis.rotationDegrees((float)angle));
            
        poseStack.translate(-pivotX, -pivotY, -pivotZ);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantHandle, int light) {
    for (BakedQuad quad : quads) {
        String path = quad.sprite().contents().name().getPath();
        if (path.contains("lever") == wantHandle) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
}
