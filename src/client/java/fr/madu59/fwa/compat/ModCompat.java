package fr.madu59.fwa.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimations;
import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import fr.madu59.fwa.api.animations.AnimationAdditions;
import fr.madu59.fwa.platform.PlatformHelper;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ModCompat {

    public final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";

    public final static ResourceLocation ALEXSCAVES_GINGERBREAD_DOOR = ResourceLocation.tryParse("alexscaves:gingerbread_door");
    public final static ResourceLocation WW_DISPLAY_LANTERNS = ResourceLocation.tryParse("wilderwild:display_lantern");
    public final static ResourceLocation ENDREM_ANCIENT_PORTAL_FRAME = ResourceLocation.tryParse("endrem:ancient_portal_frame");

    private final static boolean IS_AMENDMENTS_LOADED = PlatformHelper.isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = PlatformHelper.isModLoaded("iris") || PlatformHelper.isModLoaded("oculus");
    private final static boolean IS_SODIUM_LOADED = PlatformHelper.isModLoaded("sodium") || PlatformHelper.isModLoaded("embeddium");
    private final static boolean IS_MAP_ATLASES_LOADED = PlatformHelper.isModLoaded("map_atlases");
    private final static boolean IS_END_REMASTERED_LOADED = PlatformHelper.isModLoaded("endrem");
    private final static boolean IS_SCHOLAR_LOADED = PlatformHelper.isModLoaded("scholar");
    private final static boolean IS_COPPERATIVE_LOADED = PlatformHelper.isModLoaded("copperative");
    private final static boolean IS_MORECULLING_LOADED = PlatformHelper.isModLoaded("moreculling");
    private final static boolean IS_FLASHBACK_LOADED = PlatformHelper.isModLoaded("flashback");

    private final static Map<ResourceLocation, ResourceLocation> VAULT_KEYS = new HashMap<>();

    public static void init(){
        registerVaultKeys();
        registerAnimations();
        disableIncompatibleOptions();
    }
    
    public static Type typeOf(Block block){
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if(DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return Type.DOOR;
        return Type.USELESS;
    }

    public static boolean isOpen(BlockState state, Type type){
        return false;
    }

    private static void registerAnimations(){
        AnimationAdditions.registerAnimationForBlock(ResourceLocation.tryParse("minecraft:air"), Type.USELESS);
        AnimationAdditions.registerAnimationForBlock(WW_DISPLAY_LANTERNS, Type.LANTERN);
        AnimationAdditions.registerAnimationForBlock(ENDREM_ANCIENT_PORTAL_FRAME, Type.END_PORTAL_FRAME);
        AnimationAdditions.registerAnimationForBlock(ALEXSCAVES_GINGERBREAD_DOOR, Type.DOOR);
    }

    // LOADED MODS CHECK

    public static boolean isAmendmentsLoaded(){
        return IS_AMENDMENTS_LOADED;
    }

    public static boolean isIrisLoaded(){
        return IS_IRIS_LOADED;
    }

    public static boolean isSodiumLoaded(){
        return IS_SODIUM_LOADED;
    }

    public static boolean isMapAtlasesLoaded(){
        return IS_MAP_ATLASES_LOADED;
    }

    public static boolean isEndRemasteredLoaded(){
        return IS_END_REMASTERED_LOADED;
    }

    public static boolean isScholarLoaded(){
        return IS_SCHOLAR_LOADED;
    }

    public static boolean isCopperativeLoaded(){
        return IS_COPPERATIVE_LOADED;
    }

    public static boolean isMoreCullingLoaded(){
        return IS_MORECULLING_LOADED;
    }

    public static boolean isFlashbackLoaded(){
        return IS_FLASHBACK_LOADED;
    }

    // DISABLE MOD OPTIONS THAT ARE INCOMPATIBLE WITH FWA (E.G. MORE CULLING'S BLOCKSTATE CULLING)

    private static void disableIncompatibleOptions(){
        if(isMoreCullingLoaded()){
            MoreCullingCompat.disableBlockStateCulling();
        }
    }

    // VAULT COMPATIBILITY

    public static ItemStack getVaultKeyItem(Block block){
        ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.getOptional(VAULT_KEYS.get(BuiltInRegistries.BLOCK.getKey(block))).orElse(Items.TRIAL_KEY));
        if (itemStack != null && !itemStack.isEmpty()) return itemStack;
        else return new ItemStack(Items.TRIAL_KEY);
    }

    private static void registerVaultKeys(){
        registerVaultKey("enderscape:end_city/vault", "enderscape:end_city_key");
    }

    public static void registerVaultKey(String vaultId, String itemId){
        registerVaultKey(ResourceLocation.tryParse(vaultId), ResourceLocation.tryParse(itemId));
    }

    public static void registerVaultKey(ResourceLocation vaultId, ResourceLocation itemId){
        VAULT_KEYS.put(vaultId, itemId);
    }

    // IDLING MODDED BLOCKS COMPATIBILITY

    public static boolean isAnimatedModdedBlock(BlockState state){
        if(WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return true;
        return false;
    }

    // MAP ATLASES COMPATIBILITY

    public static class MapAtlasesCompat{

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

            if (!isMapAtlasesLoaded()) return DEFAULT_BOOK_TEXTURE;

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

    public static class EndRemasteredCompat{
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

    // SCHOLAR COMPATIBILITY

    public static class ScholarCompat{
        public static final ResourceLocation BOOKS_TEXTURE = ResourceLocation.tryParse("scholar:block/chiseled_bookshelf_untinted_books");
        public static final Map<BlockPos, NonNullList<ItemStack>> STORAGE = new ConcurrentHashMap<>();
        private static Method getDefaultTintColorForSlotMethod;
        private static Field ITEM_COLORS_FIELD;

        static{
            if (isScholarLoaded()) {
                try{
                    Class<?> chiseledBookshelfColorsClass = Class.forName("io.github.mortuusars.scholar.client.chiseled_bookshelf.ChiseledBookshelfColors");
                    getDefaultTintColorForSlotMethod = chiseledBookshelfColorsClass.getMethod("getDefaultTintColorForSlot", BlockState.class, int.class);
                    ITEM_COLORS_FIELD = chiseledBookshelfColorsClass.getDeclaredField("ITEM_COLORS");
                }catch(Exception e){
                    getDefaultTintColorForSlotMethod = null;
                    ITEM_COLORS_FIELD = null;
                }
            }
            else{
                getDefaultTintColorForSlotMethod = null;
                ITEM_COLORS_FIELD = null;
            }
        }

        @SuppressWarnings("unchecked")
        public static int getColor(ItemStack stack, BlockState state, int slot){
            if(slot < 0 || slot > 5) 
                try { 
                    return (Integer) getDefaultTintColorForSlotMethod.invoke(null, state, slot);
                } catch (Exception e) {
                    return -1;
                }
            if (stack.isEmpty()) return -1;
            else if (stack.getItem() instanceof WritableBookItem || stack.getItem() instanceof WrittenBookItem){
                return DyedItemColor.getOrDefault(stack, 0xFF99452E);
            }
            else{
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                try {
                    return ((Map<ResourceLocation, Integer>)ITEM_COLORS_FIELD.get(null)).getOrDefault(itemId, (Integer) getDefaultTintColorForSlotMethod.invoke(null, state, slot));
                } catch (Exception e) {
                    return -1;
                }
            }
        }

        public static ItemStack getBookshelfItemStack(BlockPos pos, int slot){
            if(slot < 0 || slot > 5) return ItemStack.EMPTY;
            return STORAGE.getOrDefault(pos, NonNullList.withSize(6, ItemStack.EMPTY)).get(slot);
        }
    }

    // FLASHBACK COMPATIBILITY

    public static class FlashbackCompat{
        private static Method getVisualMillis;
        private static Method isExporting;

        static{
            if (isFlashbackLoaded()) {
                try{
                    Class<?> flashbackClass = Class.forName("com.moulberry.flashback.Flashback");
                    getVisualMillis = flashbackClass.getMethod("getVisualMillis");
                    isExporting = flashbackClass.getMethod("isExporting");
                }catch(Exception e){
                    isExporting = null;
                    getVisualMillis = null;
                }
            }
            else{
                isExporting = null;
                getVisualMillis = null;
            }
        }

        public static double getPartialTick(double defaultValue){
            if (isExporting == null || getVisualMillis == null) return defaultValue;
            try{
                if(Boolean.TRUE.equals(isExporting.invoke(null))){
                    Object millis = getVisualMillis.invoke(null);
                    if(millis instanceof Number number){
                        return number.doubleValue() / 50.0;
                    }
                    return defaultValue;
                }
                else{
                    return defaultValue;
                }
            }catch(Exception e){
                return defaultValue;
            }
        }
    }

    // MORE CULLING COMPATIBILITY

    public static class MoreCullingCompat{
        
        public static void disableBlockStateCulling(){
            try{
                Class<?> configAdditionsClass = Class.forName("ca.fxco.moreculling.api.config.ConfigAdditions");
                Method disableOptionMethod = configAdditionsClass.getMethod("disableOption", String.class, String.class, BooleanSupplier.class, Object.class);
                disableOptionMethod.invoke(null, "moreculling.config.option.blockStateCulling", "Incompatible with the following mod: FWA", (BooleanSupplier) () -> false, false);
                FancyWorldAnimations.LOGGER.info("Successfully disabled MoreCulling's blockStateCulling!");
            }catch(Exception e){
                FancyWorldAnimations.LOGGER.warn("Failed to disable MoreCulling's blockStateCulling, visual issues may appear!");
            }
        }
    }
}
