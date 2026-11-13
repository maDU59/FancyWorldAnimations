package fr.madu59.fwa.compat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import fr.madu59.fwa.api.animations.AnimationAdditions;
import fr.madu59.fwa.compat.entityculling.EntityCullingCompat;
import fr.madu59.fwa.compat.iris.IrisCompat;
import fr.madu59.fwa.compat.moreculling.MoreCullingCompat;
import fr.madu59.fwa.platform.PlatformHelper;
import fr.madu59.fwa.rendering.AnimationBlockData;
import fr.madu59.fwa.rendering.AnimationRenderer;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ModCompat {
    public static final String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";
    public static final ResourceLocation ALEXSCAVES_GINGERBREAD_DOOR = ResourceLocation.tryParse("alexscaves:gingerbread_door");
    public static final ResourceLocation WW_DISPLAY_LANTERNS = ResourceLocation.tryParse("wilderwild:display_lantern");
    public static final ResourceLocation ENDREM_ANCIENT_PORTAL_FRAME = ResourceLocation.tryParse("endrem:ancient_portal_frame");

    private static final boolean IS_AMENDMENTS_LOADED = PlatformHelper.isModLoaded("amendments");
    private static final boolean IS_IRIS_LOADED = PlatformHelper.isModLoaded("iris") || PlatformHelper.isModLoaded("oculus");
    private static final boolean IS_SODIUM_LOADED = PlatformHelper.isModLoaded("sodium") || PlatformHelper.isModLoaded("embeddium");
    private static final boolean IS_MAP_ATLASES_LOADED = PlatformHelper.isModLoaded("map_atlases");
    private static final boolean IS_END_REMASTERED_LOADED = PlatformHelper.isModLoaded("endrem");
    private static final boolean IS_SCHOLAR_LOADED = PlatformHelper.isModLoaded("scholar");
    private static final boolean IS_COPPERATIVE_LOADED = PlatformHelper.isModLoaded("copperative");
    private static final boolean IS_MORECULLING_LOADED = PlatformHelper.isModLoaded("moreculling");
    private static final boolean IS_FLASHBACK_LOADED = PlatformHelper.isModLoaded("flashback");
    private static boolean IS_FA_OBJECTS_LOADED = false;
    private static final Map<ResourceLocation, ResourceLocation> VAULT_KEYS = new HashMap<>();

    private ModCompat() {
    }

    public static void init(){
        registerVaultKeys();
        registerAnimations();
        disableIncompatibleOptions();
        if(PlatformHelper.isModLoaded("entityculling")) EntityCullingCompat.init();
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
        AnimationAdditions.registerAnimationForBlock(ResourceLocation.tryBuild("hardcore_torches", "lit_lantern"), Type.LANTERN);
        AnimationAdditions.registerAnimationForBlock(ResourceLocation.tryBuild("hardcore_torches", "unlit_lantern"), Type.LANTERN);
        AnimationAdditions.registerAnimationForBlock(ResourceLocation.tryBuild("hardcore_torches", "lit_soul_lantern"), Type.LANTERN);
        AnimationAdditions.registerAnimationForBlock(ResourceLocation.tryBuild("hardcore_torches", "unlit_soul_lantern"), Type.LANTERN);
    }

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

    public static boolean isFAObjectsLoaded(){
        return IS_FA_OBJECTS_LOADED;
    }

    public static void reload(){
        Minecraft client = Minecraft.getInstance();
        if(client == null) return;
        ResourceManager currentManager = client.getResourceManager();
        if (currentManager == null) return;

        IS_FA_OBJECTS_LOADED = currentManager.listPacks().anyMatch(pack -> pack.packId()
            .toLowerCase(Locale.ROOT)
            .contains("fa+objects")
        );
    }

    public static void beginBlock(VertexConsumer consumer, AnimationBlockData data){
        if(isIrisLoaded()) IrisCompat.beginBlock(consumer, data);
    }

    public static void endBlock(VertexConsumer consumer){
        if(isIrisLoaded()) IrisCompat.endBlock(consumer);
    }

    public static void disableCulling(BlockState state){
        if(isMoreCullingLoaded()){
            MoreCullingCompat.disableBlockStateCullingForBlock(state);
        }
    }

    private static void disableIncompatibleOptions(){

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
}
