package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CampfireAnimation extends Animation{

    private final BlockStateModel model;
    private List<BlockModelPart> parts = new ArrayList<>();
    
    public CampfireAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        BlockState state;
        if (oldIsOpen) state = oldState;
        else state = newState;
        RandomSource random = RandomSource.create(state.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        model.collectParts(random, parts);
    }

    @Override
    public double getAnimDuration() {
        return 15 / SettingsManager.CAMPFIRE_SPEED.getValue();
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
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.LIT, false);
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        BlockModelPart part = parts.get(0);
        
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        VertexConsumer buffer = RenderHelper.getBuffer();

        renderFilteredQuads(poseStack, buffer, part.getQuads(null), false, light);
        for(Direction dir : Direction.values()){
            renderFilteredQuads(poseStack, buffer, part.getQuads(dir), false, light);
        }

        float scaleY;
        if (newIsOpen) {
            scaleY = (float) (1 + 2.70158 * Math.pow(getProgress(context.getNowTick()) - 1, 3) + 1.70158 * Math.pow(getProgress(context.getNowTick()) - 1, 2));
        }
        else {
            scaleY = 1 - (float) Curves.ease(getProgress(context.getNowTick()), Curves.Door.DEFAULT);
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
                RenderHelper.renderQuad(buffer, poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light);
            }
        }
    }
}
