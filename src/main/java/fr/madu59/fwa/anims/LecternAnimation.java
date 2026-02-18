package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LecternAnimation extends Animation{
    
    private final BookModel bookModel;
    private final float hash;

    public LecternAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
        this.hash = position.hashCode();
    }

    @Override
    public double getAnimDuration() {
        return 5.0 / SettingsManager.LECTERN_SPEED.getValue();
    }

    @Override
    public double getLifeSpan(){
        return !(hasInfiniteAnimation()  && newIsOpen)? getAnimDuration() : Double.MAX_VALUE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.LECTERN_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.LECTERN_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.LECTERN_INFINITE.getValue();
    }

    private double getAngle(double progress) {
        double startAngle = oldIsOpen ? 1.2 : 0.0;
        double endAngle = newIsOpen ? 1.2 : 0.0;
        return startAngle + (endAngle - startAngle) * progress;
    }

    private float getPageAngle(float defaultVal, double nowTick){
        float time = (float)(nowTick - this.startTick);
        if (time <= getAnimDuration()) return defaultVal;
        else {
            float uniqueOffset = (float)((hash + defaultVal * 67) % 100);
            time += uniqueOffset;
            float slowWave = (float) Math.sin(time * 0.1f) * 0.05f;

            float fastWave = (float) Math.sin(time * 0.25f) * 0.02f;

            return defaultVal + (slowWave + fastWave) / 2f;
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector submitNodeCollector, double nowTick) {

        Direction facing = defaultState.getValue(LecternBlock.FACING);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityCutoutNoCull(Identifier.tryParse("minecraft:textures/entity/enchanting_table_book.png")));
        
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        BookModel.State bookState = new BookModel.State(0f, getPageAngle(0.1f, nowTick), getPageAngle(0.9f, nowTick), (float)getAngle(Curves.ease(getProgress(nowTick), getCurve())));

        bookModel.setupAnim(bookState);
        poseStack.translate(0.5F, 1.0625F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.getClockWise().toYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0F, -0.125F, 0.0F);

        bookModel.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
    }
}
