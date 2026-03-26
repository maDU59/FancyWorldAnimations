package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JukeBoxAnimation extends Animation{

    private final ItemStack discItemStack;
    private final BakedModel model;
    private final ItemRenderer  itemRenderer;
    
    public JukeBoxAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        ClientLevel level = Minecraft.getInstance().level;
        ItemStack itemStack = new ItemStack(Items.MUSIC_DISC_13);
        if(level.getBlockEntity(position) instanceof JukeboxBlockEntity jukeboxBlockEntity){
            itemStack = jukeboxBlockEntity.getTheItem();
        }
        if (itemStack.isEmpty()) {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                ServerLevel serverLevel = server.getLevel(level.dimension());
        
                if (serverLevel != null) {
                    if (serverLevel.getChunkAt(position).getBlockEntity(position) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
                        itemStack = jukeboxBlockEntity.getTheItem();
                    }
                }
            }
        }
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(Items.MUSIC_DISC_13);
        }
        discItemStack = itemStack;
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        model = itemRenderer.getModel(discItemStack, level, null, position.hashCode());
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
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        float scale = 0.67f;

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position.above());

        float dy = getDeltaY(context.getNowTick());
        dy = newIsOpen? 1f - dy : dy;

        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.scale(scale, scale, 1);
        poseStack.translate(-23f / 32f, 19f/16f + dy, 8f/16f);

        itemRenderer.render(discItemStack, ItemDisplayContext.FIXED, false, poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY, model);
    }
}
