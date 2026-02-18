package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ChiseledBookShelfAnimation extends Animation{

    int pos;
    boolean isAdding;
    
    public ChiseledBookShelfAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldBlockState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);

        this.pos = 0;
        for(BooleanProperty prop : ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES){
            this.pos += 1;
            if(defaultState.getValue(prop) != oldBlockState.getValue(prop)){
                isAdding = defaultState.getValue(prop);
                break;
            }
        }
    }

    @Override
    public double getAnimDuration() {
        return 5 / SettingsManager.CHISELED_BOOKSHELF_SPEED.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) SettingsManager.CHISELED_BOOKSHELF_EASING.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.CHISELED_BOOKSHELF_STATE.getValue();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    private float getDistance(double nowTick) {
        float max = 3f/16f;
        float min = 1f/16f;
        float progress = (float)Curves.ease(getProgress(nowTick), getCurve());
        return isAdding ? min + (max - min) * progress :max - (max - min) * progress;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double nowTick) {

        Direction facing = defaultState.getValue(HorizontalDirectionalBlock.FACING);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(ResourceLocation.tryParse("minecraft:textures/atlas/blocks.png")).apply(ResourceLocation.tryParse("minecraft:block/chiseled_bookshelf_occupied"));

        PoseStack.Pose entry = poseStack.last();

        float w = 4f/16f;
        float h = (pos == 1 || pos == 5)? 5f/16f : 6f/16f;
        float d = 5f/15f;

        float y = pos > 3 ? 4/16f : 12f/16f;
        float x = 3f/16f + ((pos - 1) % 3) * 5f/16f;
        float z = getDistance(nowTick);

        if(facing == Direction.EAST){
            x = 1-x;
            z = 1-z;
        }
        if(facing == Direction.SOUTH || facing == Direction.NORTH){
            float temp = z;
            z = x;
            x = temp;
            if(facing == Direction.NORTH){
                z = 1-z;
            }
            if(facing == Direction.SOUTH){
                x = 1-x;
            }
        }

        poseStack.translate(z, y, x);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-w / 2f, -(6f/16f) / 2f, -d / 2f);

        float u1 = 1f/16f + ((pos - 1) % 3) * 5f/16f;
        float u2 = u1 + w;
        float v1 = 1 - (y - 3f/16f + h);
        float v2 = v1 + h;

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position.relative(facing));
        VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(defaultState));

        writeQuad(entry, buffer, 
            0, 0, d,  0, h, d,  w, h, d,  w, 0, d, 
            u1, v1, u2, v2, light, 0, 0, -1, sprite);

        writeQuad(entry, buffer, 
            w, 0, 0,  w, h, 0,  0, h, 0,  0, 0, 0, 
            u1, v1, u2, v2, light, 0, 0, 1, sprite);

        writeQuad(entry, buffer, 
            0, h, d,  0, h, 0,  w, h, 0,  w, h, d, 
            u1, v1, u2, v2, light, 0, -1, 0, sprite);

        writeQuad(entry, buffer, 
            0, 0, 0,  0, 0, d,  w, 0, d,  w, 0, 0, 
            u1, v1, u2, v2, light, 0, 1, 0, sprite);

        writeQuad(entry, buffer, 
            w, 0, d,  w, h, d,  w, h, 0,  w, 0, 0, 
            u1, v1, u2, v2, light, -1, 0, 0, sprite);

        writeQuad(entry, buffer, 
            0, 0, 0,  0, h, 0,  0, h, d,  0, 0, d, 
            u1, v1, u2, v2, light, 1, 0, 0, sprite);
        
        poseStack.translate(0.0f, 0.0f, 0.0f);
}

    private void writeQuad(PoseStack.Pose pose, VertexConsumer buffer, 
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float x3, float y3, float z3,
                        float x4, float y4, float z4,
                        float uMin, float vMin, float uMax, float vMax, 
                        int light, float nx, float ny, float nz, TextureAtlasSprite sprite) {

        uMin = sprite.getU(uMin);
        uMax = sprite.getU(uMax);
        vMin = sprite.getV(vMin);
        vMax = sprite.getV(vMax);

        vertex(pose, buffer, x1, y1, z1, uMin, vMax, light, nx, ny, nz);
        vertex(pose, buffer, x4, y4, z4, uMax, vMax, light, nx, ny, nz);
        vertex(pose, buffer, x3, y3, z3, uMax, vMin, light, nx, ny, nz);
        vertex(pose, buffer, x2, y2, z2, uMin, vMin, light, nx, ny, nz);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose.pose(), x, y, z)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(nx, ny, nz);
    }
}
