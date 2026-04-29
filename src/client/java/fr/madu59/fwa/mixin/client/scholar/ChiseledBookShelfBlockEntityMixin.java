package fr.madu59.fwa.mixin.client.scholar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.fwa.compat.scholar.ScholarCompatibleChiseledBookShelfBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

@Mixin(ChiseledBookShelfBlockEntity.class)
public abstract class ChiseledBookShelfBlockEntityMixin implements ScholarCompatibleChiseledBookShelfBlockEntity {

    private final NonNullList<ItemStack> oldItems = NonNullList.withSize(6, ItemStack.EMPTY);

    @Shadow 
    public abstract boolean acceptsItemType(ItemStack stack);

    @Shadow
    private NonNullList<ItemStack> items;

    @Override
    public NonNullList<ItemStack> fwa$getOldItems() {
        return this.oldItems;
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    private void fwa$setItem(int slot, ItemStack stack, CallbackInfo ci) {
        if(acceptsItemType(stack) || stack.isEmpty()){
            oldItems.set(slot, items.get(slot).copy());
        }
    }
}

