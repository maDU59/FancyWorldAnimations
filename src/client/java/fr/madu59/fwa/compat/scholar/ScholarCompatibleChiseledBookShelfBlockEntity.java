package fr.madu59.fwa.compat.scholar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public interface ScholarCompatibleChiseledBookShelfBlockEntity {
    NonNullList<ItemStack> fwa$getOldItems();
}
