package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity; 
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class LecternAnimation extends Animation {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    // DEFAULT TEXTURE FOR BOOKS
    private static final Identifier DEFAULT_BOOK_TEXTURE =
            Identifier.tryParse("minecraft:textures/entity/enchantment/enchanting_table_book.png");

    // ATLAS TEXTURE BASED ON DIMENSION, EXCLUSIVE TO THE ATLAS
    private static final Identifier ATLAS_TEXTURE_OVERWORLD =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas.png");
    private static final Identifier ATLAS_TEXTURE_NETHER =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_nether.png");
    private static final Identifier ATLAS_TEXTURE_END =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_end.png");
    private static final Identifier ATLAS_TEXTURE_OTHER =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_unknown.png");

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final BookModel bookModel;
    private final float hash;

    /**
     * Resolved once at construction time and never changed again.
     * Holds the texture that should be rendered for the book on this lectern.
     * Defaults to the vanilla enchanting-table book texture; switches to the
     * appropriate atlas texture when Map Atlases is present and the block
     * entity reports that it contains an atlas.
     */
    private final Identifier textureId;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public LecternAnimation(BlockPos position, double startTick,
                            boolean oldIsOpen, boolean newIsOpen,
                            BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        this.bookModel = new BookModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
        this.hash = position.hashCode();
        this.textureId = resolveTexture(position);
    }

    // -----------------------------------------------------------------------
    // Texture resolution (soft dependency on Map Atlases)
    // -----------------------------------------------------------------------

    /**
     * Checks whether the lectern at {pos} holds a Map Atlas item.
     * The check is done purely through the {AtlasLectern} interface that
     * Map Atlases injects via mixin, so this code path is only active when that
     * mod is loaded. No hard compile-time dependency is introduced; the cast is
     * guarded by a try/catch so that a missing class simply falls through to the
     * default texture.
     */
    private static Identifier resolveTexture(BlockPos pos) {
        try {
            Level level = Minecraft.getInstance().level;
            if (level == null) return DEFAULT_BOOK_TEXTURE;

            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return DEFAULT_BOOK_TEXTURE;

            // pepjebs.mapatlases.utils.AtlasLectern is injected onto
            // LecternBlockEntity by Map Atlases at runtime via mixin.
            // We load the interface reflectively so this class compiles and
            // runs fine even when Map Atlases is absent.
            Class<?> atlasLecternClass =
                    Class.forName("pepjebs.mapatlases.utils.AtlasLectern");

            if (!atlasLecternClass.isInstance(be)) return DEFAULT_BOOK_TEXTURE;

            // hasAtlas() — defined in the AtlasLectern interface
            boolean hasAtlas = (boolean)
                    atlasLecternClass.getMethod("mapatlases$hasAtlas").invoke(be);

            if (!hasAtlas) return DEFAULT_BOOK_TEXTURE;

            return getDimensionAtlasTexture(level);

        } catch (ClassNotFoundException ignored) {
            // Map Atlases is not installed — perfectly normal
            return DEFAULT_BOOK_TEXTURE;
        } catch (Exception e) {
            // Something changed in Map Atlases' API; degrade gracefully
            return DEFAULT_BOOK_TEXTURE;
        }
    }

    /**
     * Apply Atlas texture based on dimension
     */
    private static Identifier getDimensionAtlasTexture(Level level) {
        var dimension = level.dimension();
        if (dimension == Level.OVERWORLD) return ATLAS_TEXTURE_OVERWORLD;
        if (dimension == Level.NETHER)    return ATLAS_TEXTURE_NETHER;
        if (dimension == Level.END)       return ATLAS_TEXTURE_END;
        return ATLAS_TEXTURE_OTHER;
    }

    // -----------------------------------------------------------------------
    // Animation contract
    // -----------------------------------------------------------------------

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
    public boolean isEnabled(BlockState state){
        return SettingsManager.LECTERN_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    @Override
    public BlockState getDefaultState(BlockState state){
        return state.setValue(BlockStateProperties.HAS_BOOK, false);
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
    public void render(AnimationRenderingContext context) {
        PoseStack poseStack = context.getPoseStack();
        Double nowTick = context.getNowTick();

        Direction facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        VertexConsumer buffer = RenderHelper.getBuffer(RenderTypes.entityCutout(textureId));
        
        int light = LevelRenderer.getLightCoords((BlockAndLightGetter) Minecraft.getInstance().level, position);
        BookModel.State bookState = new BookModel.State((float)getAngle(Curves.ease(getProgress(nowTick), getCurve())), getPageAngle(0.1f, nowTick), getPageAngle(0.9f, nowTick));

        bookModel.setupAnim(bookState);
        poseStack.translate(0.5F, 1.0625F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.getClockWise().toYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0F, -0.125F, 0.0F);

        bookModel.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
    }
}
