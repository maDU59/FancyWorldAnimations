package fr.madu59.fwa.api.animations;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

public class BlackListOverrides {

    private static final List<String> overridenMods = new ArrayList<>();
    private static final List<Identifier> overridenBlocks = new ArrayList<>();

    /*
     * Overrides the baked in blacklist in case of a compatibility layer being made
     * @param modId The ID of the mod for which to disable animations.
     * @since 1.2.31
     */
    public static void disableForMod(String modId){
        overridenMods.add(modId);
    }

    /*
     * Overrides the baked in blacklist in case of a compatibility layer being made
     * @param modId The ID of the block for which to disable animations.
     * @since 1.2.31
     */
    public static void disableForBlock(Identifier blockId){
        overridenBlocks.add(blockId);
    }

    /*
     * Overrides the baked in blacklist in case of a compatibility layer being made. This does not override the user blacklist.
     * @param modId The ID of the block for which to disable animations.
     * @since 1.2.31
     */
    @ApiStatus.Internal
    public static boolean containsMod(String modId){
        return overridenMods.contains(modId);
    }

    /*
     * Overrides the baked in blacklist in case of a compatibility layer being made. This does not override the user blacklist.
     * @param modId The ID of the block for which to disable animations.
     * @since 1.2.31
     */
    @ApiStatus.Internal
    public static boolean containsBlock(Identifier blockId){
        return overridenBlocks.contains(blockId);
    }
}
