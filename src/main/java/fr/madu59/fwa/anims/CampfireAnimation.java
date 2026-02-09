package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireAnimation extends Animation{

    private final BlockState oldState;
    private final BlockState newState;
    private final RandomSource random = RandomSource.create(42);
    
    public CampfireAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldBlockState, BlockState newBlockState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        newState = newBlockState;
        oldState = oldBlockState;
    }

    @Override
    public double getAnimDuration() {
        return 15 * SettingsManager.CAMPFIRE_SPEED.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.CAMPFIRE_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlockEntity(){
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) Curves.Door.DEFAULT;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {
        BlockState state;
        if (oldIsOpen) state = oldState;
        else state = newState;

        BlockModelPart part = Minecraft.getInstance().getBlockRenderer().getBlockModel(state).collectParts(random).get(0);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state));

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
        }

        float scaleY;
        if (newIsOpen) {
            scaleY = (float) (1 + 2.70158 * Math.pow(getProgress(nowTick) - 1, 3) + 1.70158 * Math.pow(getProgress(nowTick) - 1, 2));
        }
        else {
            scaleY = 1 - (float) Curves.ease(getProgress(nowTick), Curves.Door.DEFAULT);
        }
        float scaleXZ = Math.min(1.0f, scaleY);

        poseStack.translate(0.5f,1f/16f,0.5f);
        poseStack.scale(scaleXZ,scaleY,scaleXZ);
        poseStack.translate(-0.5f,-1f/16f,-0.5f);

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), true, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), true, light);
        }
    }

    private void renderFilteredQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, boolean wantFire, int light) {
        for (BakedQuad quad : quads) {
            String path = quad.sprite().contents().name().getPath();
            if (path.contains("fire_fire") == wantFire) {
                buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
