package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JukeBoxAnimation extends Animation{

    BlockState newState;
    private final ItemStackRenderState discState = new ItemStackRenderState();
    private final Minecraft client = Minecraft.getInstance();
    
    public JukeBoxAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.newState = newState;
    }

    @Override
    public double getAnimDuration() {
        return 10 / SettingsManager.JUKEBOX_SPEED.getValue();
    }

    @Override
    public double getLifeSpan(){
        return hasInfiniteAnimation()? Double.MAX_VALUE : getAnimDuration();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.JUKEBOX_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.JUKEBOX_STATE.getValue();
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.JUKEBOX_INFINITE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    private float getDeltaY(double nowTick){
        float progress = (float)Curves.ease(getProgress(nowTick), getCurve());
        if (hasInfiniteAnimation()) return 3f * progress/4f;
        else return progress;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double nowTick) {

        float scale = 0.67f;

        JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity) client.level.getBlockEntity(position);
        ItemStack discItemStack = new ItemStack(Items.MUSIC_DISC_13);
        if(jukeboxBlockEntity != null){
            discItemStack = jukeboxBlockEntity.getTheItem();

            if (discItemStack.isEmpty()) {
                discItemStack = new ItemStack(Items.MUSIC_DISC_13);
            }
        }

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) client.level, position.above());

        float dy = getDeltaY(nowTick);
        dy = newIsOpen? 1f - dy : dy;

        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.scale(scale, scale, 1);
        poseStack.translate(-23f / 32f, 19f/16f + dy, 8f/16f);

        client.getItemModelResolver().updateForTopItem(discState, discItemStack, ItemDisplayContext.FIXED, client.player.level(), null, position.hashCode());

        discState.render(poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }
}
