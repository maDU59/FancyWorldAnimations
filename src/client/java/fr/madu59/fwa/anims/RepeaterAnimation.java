package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RepeaterAnimation extends Animation{

    private final BlockState oldState;
    private final BlockState newState;
    private final RandomSource random = RandomSource.create(42);
    private final BlockStateModel model;
    
    public RepeaterAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newBlockState, BlockState oldBlockState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);

        newState = newBlockState;
        oldState = oldBlockState;
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
    }

    @Override
    public double getAnimDuration() {
        return 10;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.REPEATER_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.REPEATER_STATE.getValue();
    }

    private float getPosition(double nowTick, int newPos, int oldPos){
        return (float)(oldPos + (newPos-oldPos) * Curves.ease(getProgress(nowTick), getCurve()));
    }


    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(RepeaterBlock.FACING);
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(defaultState));
        BlockModelPart part = model.collectParts(random).get(0);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
        }

        float dx = getPosition(nowTick, newState.getValue(RepeaterBlock.DELAY), oldState.getValue(RepeaterBlock.DELAY));
        dx = (dx-1)*2f/16f * facing.getAxisDirection().getStep();
        if (facing.getAxis() == Axis.X) poseStack.translate(dx,0,0);
        else poseStack.translate(0,0,dx);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantTorch, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.sprite().contents().name().getPath();
            if ((path.contains("redstone_torch") && quad.position0().x() > 5f/16f && quad.position0().x() < 11f/16f && quad.position2().x() > 5f/16f && quad.position2().x() < 11f/16f && quad.position0().z() > 5f/16f && quad.position0().z()  < 11f/16f && quad.position2().z() > 5f/16f && quad.position2().z() < 11f/16f) == wantTorch) {
                buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
