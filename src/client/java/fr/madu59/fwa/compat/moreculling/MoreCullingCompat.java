package fr.madu59.fwa.compat.moreculling;

import java.util.List;

import ca.fxco.moreculling.MoreCulling;
import ca.fxco.moreculling.api.block.MoreBlockCulling;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MoreCullingCompat{
    public static void disableBlockStateCullingForBlock(BlockState state){
        Block block = state.getBlock();
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();

        if (MoreCulling.CONFIG != null) {
            List<String> dontCull = MoreCulling.CONFIG.dontCull;
            if (dontCull != null && !dontCull.contains(blockId)) dontCull.add(blockId);
        }

        if (block instanceof MoreBlockCulling moreBlockCulling && moreBlockCulling.moreculling$canCull()) {
            moreBlockCulling.moreculling$setCanCull(false);
        }
    }
}
