package fr.madu59.fwa.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class ModCompat {

    public final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";

    public final static ResourceLocation WW_DISPLAY_LANTERNS = ResourceLocation.tryParse("wilderwild:display_lantern");
    public final static ResourceLocation ENDREM_ANCIENT_PORTAL_FRAME = ResourceLocation.tryParse("endrem:ancient_portal_frame");

    private final static boolean IS_AMENDMENTS_LOADED = FabricLoader.getInstance().isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");
    private final static boolean IS_SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium") || FabricLoader.getInstance().isModLoaded("embeddium");
    private final static boolean IS_MAP_ATLASES_LOADED = FabricLoader.getInstance().isModLoaded("map_atlases");
    private final static boolean IS_END_REMASTERED_LOADED = FabricLoader.getInstance().isModLoaded("endrem");
    private final static boolean IS_SCHOLAR_LOADED = FabricLoader.getInstance().isModLoaded("scholar");

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
        if(ENDREM_ANCIENT_PORTAL_FRAME.equals(blockId)) return !EndRemasteredCompat.isEmpty(state);
        return false;
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

    // VAULT COMPATIBILITY

    public static ItemStack getVaultKeyItem(Block block){
        ItemStack itemStack = VAULT_KEYS.get(BuiltInRegistries.BLOCK.getKey(block));
        if (itemStack != null) return itemStack;
        else return new ItemStack(Items.AIR);
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

            if (!isMapAtlasesLoaded()) return DEFAULT_BOOK_TEXTURE;

            try {
                Level level = Minecraft.getInstance().level;
                if (level == null) return DEFAULT_BOOK_TEXTURE;

                BlockEntity be = level.getBlockEntity(pos);
                if (be == null) return DEFAULT_BOOK_TEXTURE;

                boolean hasAtlas = false;

                CompoundTag nbt = be.getUpdateTag();

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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public class EndRemasteredCompat{
        private static Class<?> enumClass;
        private static Class<?> ancientPortalFrameClass;
        private static Object emptyEnum;
        private static Object eyeProperty;

        static {
            if (isEndRemasteredLoaded()) {
                try{
                    enumClass = Class.forName("com.teamremastered.endrem.blocks.ERFrameProperties");
                    ancientPortalFrameClass = Class.forName("com.teamremastered.endrem.blocks.AncientPortalFrame");
                    emptyEnum = Enum.valueOf((Class<Enum>)enumClass, "EMPTY");
                    eyeProperty = ancientPortalFrameClass.getField("EYE").get(null);
                }catch(Exception e){

                }
            }
        }

        public static boolean isEndRemasteredPortal(BlockState state){
            if(state == null) return false;
            return ENDREM_ANCIENT_PORTAL_FRAME.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }

        public static boolean isEmpty(BlockState state){
            if(eyeProperty == null || emptyEnum == null) return false;
            return state.getValue((Property) eyeProperty) == emptyEnum;
        }

        public static BlockState setEmpty(BlockState state){
            if(eyeProperty == null || emptyEnum == null) return state;
            return state.setValue((Property) eyeProperty, (Comparable) emptyEnum);
        }
    }

    // SCHOLAR COMPATIBILITY

    public class ScholarCompat{
        public static final ResourceLocation BOOKS_TEXTURE = ResourceLocation.tryParse("scholar:block/chiseled_bookshelf_untinted_books");
        public static final Map<BlockPos, NonNullList<ItemStack>> STORAGE = new ConcurrentHashMap<>();
        private static Method getDefaultTintColorForSlotMethod;
        private static Field ITEM_COLORS_FIELD;
        private static final String TAG_COLOR = "color";
        private static final String TAG_DISPLAY = "display";

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
            if (stack.isEmpty()) return -1;
            else if (stack.getItem() instanceof WritableBookItem || stack.getItem() instanceof WrittenBookItem){
                CompoundTag compoundTag = stack.getTagElement(TAG_DISPLAY);
                if (compoundTag != null && compoundTag.contains(TAG_COLOR, Tag.TAG_ANY_NUMERIC)) {
                    return compoundTag.getInt(TAG_COLOR);
                }
                return 0xFF99452E;
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
            return STORAGE.getOrDefault(pos, NonNullList.withSize(6, ItemStack.EMPTY)).get(slot);
        }
    }
}
