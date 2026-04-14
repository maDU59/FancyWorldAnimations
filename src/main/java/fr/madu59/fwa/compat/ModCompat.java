package fr.madu59.fwa.compat;

import java.util.HashMap;
import java.util.Map;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModCompat {

    public final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";
    public final static Identifier WW_DISPLAY_LANTERNS = Identifier.tryParse("wilderwild:display_lantern");
    private final static boolean IS_AMENDMENTS_LOADED = FabricLoader.getInstance().isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");

    private final static Map<Identifier, ItemStack> VAULT_KEYS = new HashMap<>();

    public ModCompat(){
        registerVaultKeys();
    }
    
    public static Type typeOf(Block block){
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if(DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return Type.DOOR;
        if(WW_DISPLAY_LANTERNS.equals(blockId)) return Type.LANTERN;
        return Type.USELESS;
    }

    public static boolean isOpen(BlockState state, Block block){
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (DRAMATIC_DOORS_NAMESPACE.equals(blockId.getNamespace()) || blockId.toString().startsWith("everycomp:dd")) return state.getValue(BlockStateProperties.OPEN);
        return false;
    }

    public static boolean isAmendmentsLoaded(){
        return IS_AMENDMENTS_LOADED;
    }

    public static boolean isIrisLoaded(){
        return IS_IRIS_LOADED;
    }

    public static ItemStack getVaultKeyItem(Block block){
        ItemStack itemStack = VAULT_KEYS.get(BuiltInRegistries.BLOCK.getKey(block));
        if (itemStack != null) return itemStack;
        else return new ItemStack(Items.TRIAL_KEY);
    }

    private static void registerVaultKeys(){
        registerVaultKey("enderscape:end_city/vault", "enderscape:end_city_key");
    }

    public static void registerVaultKey(String vaultId, String itemId){
        registerVaultKey(Identifier.tryParse(vaultId), Identifier.tryParse(itemId));
    }

    public static void registerVaultKey(Identifier vaultId, Identifier itemId){
        VAULT_KEYS.put(vaultId, BuiltInRegistries.ITEM.get(itemId).map(ItemStack::new).orElse(ItemStack.EMPTY));
    }

    public static boolean isAnimatedModdedBlock(BlockState state){
        if(WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return true;
        return false;
    }

}
