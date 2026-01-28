package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.Curves.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LecternAnimation extends Animation{
    
    BookModel bookModel;
    double animationDuration = 5.0;

    public LecternAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public double getAnimDuration() {
        return animationDuration;
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    @Override
    public double getProgress(double nowTick) {
        return Math.clamp((nowTick - this.startTick) / animationDuration, 0.0, 1.0);
    }

    private double getAngle(double progress) {
        double startAngle = oldIsOpen ? 1.2 : 0.0;
        double endAngle = newIsOpen ? 1.2 : 0.0;
        return startAngle + (endAngle - startAngle) * progress;
    }

    @Override
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(LecternBlock.FACING);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityCutoutNoCull(Identifier.tryParse("minecraft:textures/entity/enchanting_table_book.png")));
        
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        BookModel.State bookState = new BookModel.State(0f, 0.1f, 0.9f, (float)getAngle(Curves.ease(getProgress(nowTick), Type.LINEAR)));

        bookModel.setupAnim(bookState);
        poseStack.translate(0.5F, 1.0625F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.getClockWise().toYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0F, -0.125F, 0.0F);

        bookModel.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
    }
}
