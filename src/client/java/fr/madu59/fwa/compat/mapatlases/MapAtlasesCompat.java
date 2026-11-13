package fr.madu59.fwa.compat.mapatlases;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class MapAtlasesCompat{
    private static final ResourceLocation ATLAS_TEXTURE_OVERWORLD =
            ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas.png");
    private static final ResourceLocation ATLAS_TEXTURE_NETHER =
            ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_nether.png");
    private static final ResourceLocation ATLAS_TEXTURE_END =
            ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_end.png");
    private static final ResourceLocation ATLAS_TEXTURE_OTHER =
            ResourceLocation.tryParse("map_atlases:textures/entity/lectern_atlas_unknown.png");

    public static ResourceLocation resolveTexture(BlockPos pos){
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        boolean hasAtlas = false;

        CompoundTag nbt = be.getUpdateTag(level.registryAccess());

        if (nbt.contains("Book")) {
            CompoundTag bookTag = nbt.getCompound("Book").orElseThrow();
            String bookId = bookTag.getString("id").orElseThrow();

            if ("map_atlases:atlas".equals(bookId)) {
                hasAtlas = true;
            }
        }

        if (!hasAtlas) return null;

        return getDimensionAtlasTexture(level);
    }

    private static ResourceLocation getDimensionAtlasTexture(Level level){
        if (level.dimension() == Level.OVERWORLD) return ATLAS_TEXTURE_OVERWORLD;
        if (level.dimension() == Level.NETHER) return ATLAS_TEXTURE_NETHER;
        if (level.dimension() == Level.END) return ATLAS_TEXTURE_END;
        return ATLAS_TEXTURE_OTHER;
    }
}
