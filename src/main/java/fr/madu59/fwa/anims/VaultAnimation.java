package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
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
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

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
        poseStack.translate(0f, 0f, 0.6f + getDistance(context.getNowTick()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f + getRotation(context.getNowTick())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(keyItemStack, Minecraft.getInstance().level, null, position.hashCode());

        itemRenderer.render(keyItemStack, ItemDisplayContext.FIXED, false, poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY, model);
    }
}
