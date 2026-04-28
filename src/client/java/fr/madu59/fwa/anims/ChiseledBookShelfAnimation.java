package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.compat.ModCompat.ScholarCompat;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ChiseledBookShelfAnimation extends Animation{

    int slot;
    boolean isAdding;
    private final Identifier atlasId = Identifier.tryParse("minecraft:blocks");
    private final Identifier textureId = Identifier.tryParse("minecraft:block/chiseled_bookshelf_occupied");
    
    public ChiseledBookShelfAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);

        this.slot = 0;
        for(BooleanProperty prop : ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES){
            if(defaultState.getValue(prop) != oldState.getValue(prop)){
                isAdding = defaultState.getValue(prop);
                break;
            }
            this.slot += 1;
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
    public boolean isEnabled(BlockState state){
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
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();

        List<TextureAtlasSprite> sprites;
        List<Integer> colors;
        AtlasManager atlasManager = Minecraft.getInstance().getAtlasManager();
        if(ModCompat.isScholarLoaded()){
            sprites = List.of(atlasManager.getAtlasOrThrow(atlasId).getSprite(textureId), atlasManager.getAtlasOrThrow(atlasId).getSprite(ScholarCompat.BOOKS_TEXTURE));
            List<BlockTintSource> tintSources = Minecraft.getInstance().getBlockColors().getTintSources(defaultState);
            colors = List.of(-1, tintSources.get(slot).colorInWorld(defaultState, Minecraft.getInstance().level, position));
            if(!isAdding){
                FancyWorldAnimationsClient.removeAnimationAt(position);
                return;
            }
        }
        else{
            sprites = List.of(atlasManager.getAtlasOrThrow(atlasId).getSprite(textureId));
            colors = List.of(-1);
        }

        Direction facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        PoseStack.Pose entry = poseStack.last();

        float w = 4f/16f;
        float h = (slot == 0 || slot == 4)? 5f/16f : 6f/16f;
        float d = 5f/15f;

        float y = slot > 2 ? 4/16f : 12f/16f;
        float x = 3f/16f + ((slot) % 3) * 5f/16f;
        float z = getDistance(context.getNowTick());

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

        float u1 = 1f/16f + ((slot) % 3) * 5f/16f;
        float u2 = u1 + w;
        float v1 = 1 - (y - 3f/16f + h);
        float v2 = v1 + h;

        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position.relative(facing));
        VertexConsumer buffer = RenderHelper.getBuffer();

        for(int i = 0; i < sprites.size(); i++){

            TextureAtlasSprite sprite = sprites.get(i);
            int color = colors.get(i);

            int r = 255;
            int g = 255;
            int b = 255;
            if(color != -1){
                r = color >> 16 & 255;
                g = color >> 8 & 255;
                b = color & 255;
            }

            writeQuad(entry, buffer, 
                0, 0, d,  0, h, d,  w, h, d,  w, 0, d, 
                u1, v1, u2, v2, light, 0, 0, -1, sprite, r, g, b);

            writeQuad(entry, buffer, 
                w, 0, 0,  w, h, 0,  0, h, 0,  0, 0, 0, 
                u1, v1, u2, v2, light, 0, 0, 1, sprite, r, g, b);

            writeQuad(entry, buffer, 
                0, h, d,  0, h, 0,  w, h, 0,  w, h, d, 
                u1, v1, u2, v2, light, 0, -1, 0, sprite, r, g, b);

            writeQuad(entry, buffer, 
                0, 0, 0,  0, 0, d,  w, 0, d,  w, 0, 0, 
                u1, v1, u2, v2, light, 0, 1, 0, sprite, r, g, b);

            writeQuad(entry, buffer, 
                w, 0, d,  w, h, d,  w, h, 0,  w, 0, 0, 
                u1, v1, u2, v2, light, -1, 0, 0, sprite, r, g, b);

            writeQuad(entry, buffer, 
                0, 0, 0,  0, h, 0,  0, h, d,  0, 0, d, 
                u1, v1, u2, v2, light, 1, 0, 0, sprite, r, g, b);
            
        }
    }

    private void writeQuad(PoseStack.Pose pose, VertexConsumer buffer, 
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float x3, float y3, float z3,
                        float x4, float y4, float z4,
                        float uMin, float vMin, float uMax, float vMax, 
                        int light, float nx, float ny, float nz, TextureAtlasSprite sprite
                        , int r, int g, int b) {

        uMin = sprite.getU(uMin);
        uMax = sprite.getU(uMax);
        vMin = sprite.getV(vMin);
        vMax = sprite.getV(vMax);

        vertex(pose, buffer, x1, y1, z1, uMin, vMax, light, nx, ny, nz, r, g, b);
        vertex(pose, buffer, x4, y4, z4, uMax, vMax, light, nx, ny, nz, r, g, b);
        vertex(pose, buffer, x3, y3, z3, uMax, vMin, light, nx, ny, nz, r, g, b);
        vertex(pose, buffer, x2, y2, z2, uMin, vMin, light, nx, ny, nz, r, g, b);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz, int r, int g, int b) {
        buffer.addVertex(pose.pose(), x, y, z)
            .setColor(r, g, b, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(nx, ny, nz);
    }
}
