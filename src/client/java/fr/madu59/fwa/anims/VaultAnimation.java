package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class VaultAnimation extends Animation{

    private final ItemStackRenderState keyState = new ItemStackRenderState();
    private final Direction facing;
    private final ItemStack keyItemStack;
    
    public VaultAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        ItemStack defaultKeyStack = new ItemStack(Items.TRIAL_KEY);
        ItemStack itemStack = defaultKeyStack;
        if(Minecraft.getInstance().level.getBlockEntity(position) instanceof VaultBlockEntity vaultBlockEntity){
            itemStack = vaultBlockEntity.getConfig().keyItem();
        }
        if (ItemStack.isSameItem(itemStack, defaultKeyStack) || itemStack.isEmpty()) {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                ServerLevel serverLevel = server.getLevel(Minecraft.getInstance().level.dimension());
        
                if (serverLevel != null) {
                    if (serverLevel.getChunkAt(position).getBlockEntity(position) instanceof VaultBlockEntity vaultBlockEntity) {
                        itemStack = vaultBlockEntity.getConfig().keyItem();
                    }
                }
            }
        }
        if (ItemStack.isSameItem(itemStack, defaultKeyStack) || itemStack.isEmpty()) {
            itemStack = defaultState.getValue(BlockStateProperties.OMINOUS)? new ItemStack(Items.OMINOUS_TRIAL_KEY) : new ItemStack(Items.TRIAL_KEY);
        }

        keyItemStack = itemStack;
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
    public boolean isEnabled(BlockState state){
        return SettingsManager.VAULT_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    @Override
    public AABB getBoundingBox(){
        return new AABB(position.getCenter().add(-1.5, -1.5, -1.5), position.getCenter().add(1.5, 1.5, 1.5));
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.VAULT_STATE, VaultState.UNLOCKING);
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

        float scale = 1;

        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position.above());

        poseStack.translate(0.5f, 0.5f, 0.5f);
        float angle = facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.scale(scale, scale, 1);
        poseStack.translate(0f, 0f, 0.6f + getDistance(context.getNowTick()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f + getRotation(context.getNowTick())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(keyState, keyItemStack, ItemDisplayContext.ON_SHELF, Minecraft.getInstance().player.level(), null, position.hashCode());

        keyState.submit(poseStack, context.getSubmitNodeCollector(), light, OverlayTexture.NO_OVERLAY, 0);
    }
}
