package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EndPortalFrameAnimation extends Animation{

    private final RandomSource random = RandomSource.create(42);
    
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
        return (T) Curves.Door.DEFAULT;
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);

        Direction facing = defaultState.getValue(EndPortalFrameBlock.FACING);

        BlockState eyeState = defaultState.setValue(EndPortalFrameBlock.HAS_EYE, true);

        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(eyeState);
        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(eyeState));
        BlockModelPart part = model.collectParts(random).get(0);
        poseStack.translate(0f,1f/16f - (float)Curves.ease(getProgress(nowTick), getCurve())/16f,0f);
        renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantEye, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.sprite().contents().name().getPath();
            if (path.contains("eye") == wantEye) {
                buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
