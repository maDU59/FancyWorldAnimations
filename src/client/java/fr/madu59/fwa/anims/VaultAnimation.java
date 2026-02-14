package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VaultAnimation extends Animation{

    BlockState newState;
    private final ItemStackRenderState keyState = new ItemStackRenderState();
    
    public VaultAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newState, BlockState oldState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.newState = newState;
    }

    @Override
    public double getAnimDuration() {
        return 10;
    }

    @Override
    public double getLifeSpan(){
        return newIsOpen? Double.MAX_VALUE : getAnimDuration();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.VAULT_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.VAULT_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    private float getDistance(double nowTick) {
        float max = 3f/16f;
        float min = -3f/16f;
        float progress = (float)Curves.ease(getProgress(nowTick), getCurve());
        return oldIsOpen ? min + (max - min) * progress :max - (max - min) * progress;
    }

    private float getRotation(double nowTick) {
        double progress = Math.clamp((nowTick - (this.startTick + 2)) / getAnimDuration(), 0.0, 1.0);
        float max = 0f;
        float min = 720f;
        return oldIsOpen ? 0 : max - (max - min) * (float)Curves.ease(progress, Curves.Classic.EASE_IN_OUT_CUBIC);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {

        Direction facing = defaultState.getValue(VaultBlock.FACING);
        float scale = 1;

        //VaultBlockEntity vaultBlockEntity = (VaultBlockEntity) Minecraft.getInstance().level.getBlockEntity(position);
        ItemStack keyItemStack = new ItemStack(Items.TRIAL_KEY);
        if(defaultState.getValue(VaultBlock.OMINOUS)) keyItemStack = new ItemStack(Items.OMINOUS_TRIAL_KEY);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position.above());

        poseStack.translate(0.5f, 0.5f, 0.5f);
        float angle = facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.scale(scale, scale, 1);
        poseStack.translate(0f, 0f, 0.6f + getDistance(nowTick));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f + getRotation(nowTick)));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(keyState, keyItemStack, ItemDisplayContext.ON_SHELF, Minecraft.getInstance().player.level(), null, position.hashCode());

        keyState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
