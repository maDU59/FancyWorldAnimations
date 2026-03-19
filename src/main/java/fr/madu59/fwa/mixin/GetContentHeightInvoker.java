package fr.madu59.fwa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = LayeredCauldronBlock.class, remap = true)
public abstract interface GetContentHeightInvoker {
    @Invoker("getContentHeight")
    double fwa$getContentHeight(BlockState blockState);
}
