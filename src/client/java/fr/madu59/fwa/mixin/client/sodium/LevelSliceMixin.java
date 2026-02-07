package fr.madu59.fwa.mixin.client.sodium;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSlice.class, remap = false)
public class LevelSliceMixin {

    @Inject(method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), cancellable = true)
    private void fwa$hideAnimatedBlocks(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
        BlockPos pos = new BlockPos(x, y, z);
        if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}