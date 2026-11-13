package fr.madu59.fwa.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.madu59.fwa.rendering.AnimationBlockData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.world.level.block.state.BlockState;

public final class IrisCompat {
    public static void beginBlock(VertexConsumer consumer, AnimationBlockData blockData) {
        if ((consumer instanceof BlockSensitiveBufferBuilder blockSensitiveConsumer)) {
            Object2IntMap<BlockState> blockStateIdsMap = WorldRenderingSettings.INSTANCE.getBlockStateIds();
            if (blockStateIdsMap == null) return;
            blockSensitiveConsumer.beginBlock((short) blockStateIdsMap.getOrDefault(blockData.getBlockState(), -1), (short) 0, blockData.getPosX(), blockData.getPosY(), blockData.getPosZ());
        }
    }

    public static void endBlock(VertexConsumer consumer) {
        if (consumer instanceof BlockSensitiveBufferBuilder blockSensitiveConsumer) {
            blockSensitiveConsumer.endBlock();
        }
    }
}
