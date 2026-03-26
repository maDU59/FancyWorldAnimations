package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TripWireHookAnimation extends Animation{

    private final BlockStateModel model;
    private List<BlockStateModelPart> parts = new ArrayList<>();
    
    public TripWireHookAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(defaultState);
        model.collectParts(random, parts);
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
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.ATTACHED, false);
    }

    @Override
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
        //Direction facing = defaultState.getValue(TripWireHookBlock.FACING);

        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position);
        context.getSubmitNodeCollector().submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), parts, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
    }
}
