package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TripWireHookAnimation extends Animation{

    private final BakedModel model;
    private final RandomSource random;
    
    public TripWireHookAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
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

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY);
    }
}
