package fr.madu59.fwa.compat.mapatlases;

import pepjebs.mapatlases.utils.AtlasLectern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class MapAtlasesCompat{
    private static final Identifier DEFAULT_BOOK_TEXTURE =
            Identifier.tryParse("minecraft:textures/entity/enchantment/enchanting_table_book.png");
    private static final Identifier ATLAS_TEXTURE_OVERWORLD =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas.png");
    private static final Identifier ATLAS_TEXTURE_NETHER =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_nether.png");
    private static final Identifier ATLAS_TEXTURE_END =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_end.png");
    private static final Identifier ATLAS_TEXTURE_OTHER =
            Identifier.tryParse("map_atlases:textures/entity/lectern_atlas_unknown.png");

    public static Identifier resolveTexture(BlockPos pos){
        Level level = Minecraft.getInstance().level;
        if (level == null) return DEFAULT_BOOK_TEXTURE;

        if (!(level.getBlockEntity(pos) instanceof AtlasLectern lectern) || !lectern.mapatlases$hasAtlas()) {
            return DEFAULT_BOOK_TEXTURE;
        }

        return getDimensionAtlasTexture(level);
    }

    private static Identifier getDimensionAtlasTexture(Level level){
        if (level.dimension() == Level.OVERWORLD) return ATLAS_TEXTURE_OVERWORLD;
        if (level.dimension() == Level.NETHER) return ATLAS_TEXTURE_NETHER;
        if (level.dimension() == Level.END) return ATLAS_TEXTURE_END;
        return ATLAS_TEXTURE_OTHER;
    }
}
