package fr.madu59.fwa.mixin.scholar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.compat.ModCompat.ScholarCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

@Mixin(ChiseledBookShelfBlockEntity.class)
public abstract class ChiseledBookShelfBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Inject(method = "setItem", at = @At("HEAD"))
    private void fwa$setItem(final int slot, final ItemStack stack, CallbackInfo ci) {
        if(stack.is(ItemTags.BOOKSHELF_BOOKS)){
            saveItem(slot);
        }
    }

    @Inject(method = "removeItem", at = @At("HEAD"))
    private void fwa$removeItem(final int slot, final int count, CallbackInfoReturnable<ItemStack> ci) {
        saveItem(slot);
    }

    private void saveItem(int slot){
        ChiseledBookShelfBlockEntity shelf = (ChiseledBookShelfBlockEntity) (Object) this;
        BlockPos blockPos = shelf.getBlockPos().immutable();
        NonNullList<ItemStack> oldItems = ScholarCompat.STORAGE.getOrDefault(blockPos, NonNullList.withSize(6, ItemStack.EMPTY));
        oldItems.set(slot, items.get(slot).copy());
        ScholarCompat.STORAGE.put(blockPos, oldItems);
    }
}

