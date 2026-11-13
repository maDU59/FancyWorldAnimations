package fr.madu59.fwa.compat.moreculling;

import java.util.List;

import ca.fxco.moreculling.MoreCulling;
import ca.fxco.moreculling.api.block.MoreBlockCulling;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MoreCullingCompat{
    public static void disableBlockStateCullingForBlock(BlockState state){
        Block block = state.getBlock();

        if (block instanceof MoreBlockCulling moreBlockCulling && moreBlockCulling.canCull()) {
            moreBlockCulling.setCanCull(false);
        }
    }
}
