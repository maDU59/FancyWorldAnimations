package fr.madu59.fwa.compat.scholar;

import io.github.mortuusars.scholar.book.BookColor;
import io.github.mortuusars.scholar.client.chiseled_bookshelf.ChiseledBookshelfColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScholarCompat{
    public static final ResourceLocation BOOKS_TEXTURE = ResourceLocation.tryParse("scholar:block/chiseled_bookshelf_untinted_books");
    public static final Map<BlockPos, NonNullList<ItemStack>> STORAGE = new ConcurrentHashMap<>();

    public static int getColor(ItemStack stack, BlockState state, int slot){
        if(slot < 0 || slot > 5)
            return ChiseledBookshelfColors.getDefaultTintColorForSlot(state, slot);
        if (stack.isEmpty()) return -1;

        if (stack.getItem() instanceof WrittenBookItem || stack.getItem() instanceof WritableBookItem) {
            return BookColor.of(stack);
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ChiseledBookshelfColors.ITEM_COLORS.getOrDefault(itemId, ChiseledBookshelfColors.getDefaultTintColorForSlot(state, slot));
    }

    public static ItemStack getBookshelfItemStack(BlockPos pos, int slot){
        if(slot < 0 || slot > 5) return ItemStack.EMPTY;
        return STORAGE.getOrDefault(pos, NonNullList.withSize(6, ItemStack.EMPTY)).get(slot);
    }

    //TODO: Correct the book color for the lectern book when the book is a scholar book
}
