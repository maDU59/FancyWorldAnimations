package fr.madu59.fwa.api.animations;

import org.jetbrains.annotations.ApiStatus;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;

public class AnimationAdditions {

    private static final Map<Identifier, Type> additionalAnimations = new HashMap<>();

    /*
     * Registers an animation for a specific block.
     * @param blockId The ID of the block for which to register the animation.
     * @param animationType The type of the animation to register.
     * @since 1.2.26
     */
    public static void registerAnimationForBlock(Identifier blockId, Type animationType){
        additionalAnimations.put(blockId, animationType);
    }

    /*
     * Returns a map of all registered animations.
     * @return A map where the keys are block IDs and the values are animation types.
     * @since 1.2.26
     */
    @ApiStatus.Internal
    public static Map<Identifier, Type> getAdditionalBlockAnimations(){
        return new HashMap<>(additionalAnimations);
    }

    /*
     * Checks if an animation is registered for a specific block.
     * @param blockId The ID of the block to check.
     * @return True if an animation is registered for the block, false otherwise.
     * @since 1.2.26
     */
    @ApiStatus.Internal
    public static boolean hasAnimation(Identifier blockId){
        return additionalAnimations.containsKey(blockId);
    }

    /*
     * Returns the animation type registered for a specific block.
     * @param blockId The ID of the block to check.
     * @return The animation type registered for the block, or null if no animation is registered.
     * @since 1.2.26
     */
    @ApiStatus.Internal
    public static Type getAnimationType(Identifier blockId){
        return additionalAnimations.get(blockId);
    }
}
