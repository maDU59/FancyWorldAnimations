package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LayeredCauldronBlock.class)
public interface GetContentHeightInvoker {
    @Invoker("getContentHeight")
    double fwa$getContentHeight(BlockState blockState);
}
