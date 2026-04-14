package fr.madu59.fwa.compat;

import java.util.HashMap;
import java.util.Map;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModCompat {

    public final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";
    public final static ResourceLocation WW_DISPLAY_LANTERNS = ResourceLocation.tryParse("wilderwild:display_lantern");
    private final static boolean IS_AMENDMENTS_LOADED = FabricLoader.getInstance().isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");

    private final static Map<ResourceLocation, ItemStack> VAULT_KEYS = new HashMap<>();

    public ModCompat(){
        registerVaultKeys();
    }
    
    public static Type typeOf(Block block){
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if(DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return Type.DOOR;
        if(WW_DISPLAY_LANTERNS.equals(blockId)) return Type.LANTERN;
        return Type.USELESS;
    }

    public static boolean isOpen(BlockState state, Block block){
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return state.getValue(BlockStateProperties.OPEN);
        return false;
    }

    // LOADED MODS CHECK

    public static boolean isAmendmentsLoaded(){
        return IS_AMENDMENTS_LOADED;
    }

    public static boolean isIrisLoaded(){
        return IS_IRIS_LOADED;
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
        private static ResourceLocation getDimensionAtlasTexture(Level level) {
            var dimension = level.dimension();
            if (dimension == Level.OVERWORLD) return ATLAS_TEXTURE_OVERWORLD;
            if (dimension == Level.NETHER)    return ATLAS_TEXTURE_NETHER;
            if (dimension == Level.END)       return ATLAS_TEXTURE_END;
            return ATLAS_TEXTURE_OTHER;
        }

    }

}
