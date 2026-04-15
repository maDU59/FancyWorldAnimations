package fr.madu59.fwa.compat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class ModCompat {

    public final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";

    public final static ResourceLocation WW_DISPLAY_LANTERNS = ResourceLocation.tryParse("wilderwild:display_lantern");
    public final static ResourceLocation ENDREM_ANCIENT_PORTAL_FRAME = ResourceLocation.tryParse("endrem:ancient_portal_frame");

    private final static boolean IS_AMENDMENTS_LOADED = FabricLoader.getInstance().isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");
    private final static boolean IS_MAP_ATLASES_LOADED = FabricLoader.getInstance().isModLoaded("map_atlases");
    private final static boolean IS_END_REMASTERED_LOADED = FabricLoader.getInstance().isModLoaded("endrem");

    private final static Map<ResourceLocation, ItemStack> VAULT_KEYS = new HashMap<>();

    public ModCompat(){
        registerVaultKeys();
    }
    
    public static Type typeOf(Block block){
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if(DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return Type.DOOR;
        if(WW_DISPLAY_LANTERNS.equals(blockId)) return Type.LANTERN;
        if(ENDREM_ANCIENT_PORTAL_FRAME.equals(blockId)) return Type.END_PORTAL_FRAME;
        return Type.USELESS;
    }

    public static boolean isOpen(BlockState state, Block block){
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return state.getValue(BlockStateProperties.OPEN);
        if(ENDREM_ANCIENT_PORTAL_FRAME.equals(blockId)) return state.getValue(BlockStateProperties.EYE);
        return false;
    }

    // LOADED MODS CHECK

    public static boolean isAmendmentsLoaded(){
        return IS_AMENDMENTS_LOADED;
    }

    public static boolean isIrisLoaded(){
        return IS_IRIS_LOADED;
    }

    public static boolean isEndRemasteredLoaded(){
        return IS_END_REMASTERED_LOADED;
    }

    // VAULT COMPATIBILITY

    public static ItemStack getVaultKeyItem(Block block){
        ItemStack itemStack = VAULT_KEYS.get(BuiltInRegistries.BLOCK.getKey(block));
        if (itemStack != null) return itemStack;
        else return new ItemStack(Items.TRIAL_KEY);
    }

    private static void registerVaultKeys(){
        registerVaultKey("enderscape:end_city/vault", "enderscape:end_city_key");
    }

    public static void registerVaultKey(String vaultId, String itemId){
        registerVaultKey(ResourceLocation.tryParse(vaultId), ResourceLocation.tryParse(itemId));
    }

    public static void registerVaultKey(ResourceLocation vaultId, ResourceLocation itemId){
        VAULT_KEYS.put(vaultId, new ItemStack(BuiltInRegistries.ITEM.get(itemId)));
    }

    // IDLING MODDED BLOCKS COMPATIBILITY

    public static boolean isAnimatedModdedBlock(BlockState state){
        if(WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return true;
        return false;
    }

    // MAP ATLASES COMPATIBILITY

    public class MapAtlasesCompat{

        // DEFAULT TEXTURE FOR BOOKS
        private static final ResourceLocation DEFAULT_BOOK_TEXTURE =
                ResourceLocation.tryParse("minecraft:textures/entity/enchanting_table_book.png");

        // ATLAS TEXTURE BASED ON DIMENSION, EXCLUSIVE TO THE ATLAS
        private static final ResourceLocation ATLAS_TEXTURE_OVERWORLD =
                ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas.png");
        private static final ResourceLocation ATLAS_TEXTURE_NETHER =
                ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_nether.png");
        private static final ResourceLocation ATLAS_TEXTURE_END =
                ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_end.png");
        private static final ResourceLocation ATLAS_TEXTURE_OTHER =
                ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_unknown.png");

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
        public static ResourceLocation resolveTexture(BlockPos pos) {

            if (!IS_MAP_ATLASES_LOADED) return DEFAULT_BOOK_TEXTURE;

            try {
                Level level = Minecraft.getInstance().level;
                if (level == null) return DEFAULT_BOOK_TEXTURE;

                BlockEntity be = level.getBlockEntity(pos);
                if (be == null) return DEFAULT_BOOK_TEXTURE;

                boolean hasAtlas = false;

                CompoundTag nbt = be.getUpdateTag(level.registryAccess());

                if (nbt.contains("Book")) {
                    CompoundTag bookTag = nbt.getCompound("Book");
                    String bookId = bookTag.getString("id");

                    if ("map_atlases:atlas".equals(bookId)) {
                        hasAtlas = true;
                    }
                }

                if (!hasAtlas) return DEFAULT_BOOK_TEXTURE;

                return getDimensionAtlasTexture(level);

            } catch (Exception e) {
                // Something changed in Map Atlases' API; degrade gracefully
                return DEFAULT_BOOK_TEXTURE;
            }
        }

        /**
         * Apply Atlas texture based on dimension
         */
        private static ResourceLocation getDimensionAtlasTexture(Level level) {
            var dimension = level.dimension();
            if (dimension == Level.OVERWORLD) return ATLAS_TEXTURE_OVERWORLD;
            if (dimension == Level.NETHER)    return ATLAS_TEXTURE_NETHER;
            if (dimension == Level.END)       return ATLAS_TEXTURE_END;
            return ATLAS_TEXTURE_OTHER;
        }

    }

    // END REMASTERED COMPATIBILITY

    public class EndRemasteredCompat{
        private static Method renderMethod;

        static {
            if (isEndRemasteredLoaded()) {
                try{
                    Class<?> ancientPortalRendererClass = Class.forName("com.teamremastered.endrem.client.AncientPortalRenderer");
                    Class<?> ancientPortalFrameEntityClass = Class.forName("com.teamremastered.endrem.block.AncientPortalFrameEntity");
                    renderMethod = ancientPortalRendererClass.getMethod("render", ancientPortalFrameEntityClass, float.class, PoseStack.class, MultiBufferSource.class, int.class, int.class, Vec3.class);
                }catch(Exception e){
                    renderMethod = null;
                }
            }
            else{
                renderMethod = null;
            }
        }

        public static boolean isEndRemasteredPortal(BlockState state){
            return ENDREM_ANCIENT_PORTAL_FRAME.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }

        public static void renderEndPortalFrameAnimation(AnimationRenderingContext context, PoseStack poseStack, BlockPos position, int light){

            Level level = Minecraft.getInstance().level;
            if (level == null || renderMethod == null) return;

            BlockEntity be = level.getBlockEntity(position);
            if (be == null) return;

            try{
                BlockEntityRenderDispatcher dispatcher = net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher();
                Object rendererInstance = dispatcher.getRenderer((BlockEntity) be);

                renderMethod.invoke(rendererInstance, be, (float)context.getNowTick(), poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY, new Vec3(0, 0, 0));
                RenderHelper.endBatch(context.getBufferSource());
            }catch(Exception e){
                return;
            }
        }
    }
}
