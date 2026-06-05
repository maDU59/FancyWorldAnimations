package fr.madu59.fwa.api.animations;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public class AnimationDisabler {

    private static final List<String> disabledMods = new ArrayList<>();
    private static final List<ResourceLocation> disabledBlocks = new ArrayList<>();

    /*
     * Disables animations for a specific mod.
     * @param modId The ID of the mod for which to disable animations.
     * @since 1.2.25
     */
    public static void disableForMod(String modId){
        disabledMods.add(modId);
    }

    /*
     * Disables animations for a specific block.
     * @param blockId The ID of the block for which to disable animations.
     * @since 1.2.25
     */
    public static void disableForBlock(ResourceLocation blockId){
        disabledBlocks.add(blockId);
    }

    /*
     * Returns a list of all disabled mods.
     * @return A list of disabled mod IDs.
     * @since 1.2.25
     */
    @ApiStatus.Internal
    public static List<String> getDisabledMods(){
        return new ArrayList<>(disabledMods);
    }

    /*
     * Returns a list of all disabled blocks.
     * @return A list of disabled block IDs.
     * @since 1.2.25
     */
    @ApiStatus.Internal
    public static List<ResourceLocation> getDisabledBlocks(){
        return new ArrayList<>(disabledBlocks);
    }

}
