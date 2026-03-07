package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.blockentity.registry.BlockEntityTypes;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    public BlockEntityTypeMixin() {
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void fwa$isValid(BlockState state, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if ((Object)this == BlockEntityTypes.LANTERN) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }
}