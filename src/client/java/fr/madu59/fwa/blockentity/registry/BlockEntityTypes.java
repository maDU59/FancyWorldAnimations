package fr.madu59.fwa.blockentity.registry;


import java.util.ArrayList;

import fr.madu59.fwa.blockentity.LanternBlockEntity;
import fr.madu59.fwa.rendering.LanternRenderer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopperBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypes {
    public static BlockEntityType<LanternBlockEntity> LANTERN;

    private static Block[] lanternBlocks() {
        ArrayList<Block> blocks = new ArrayList<Block>();
        blocks.add(Blocks.LANTERN);
        blocks.add(Blocks.SOUL_LANTERN);
        WeatheringCopperBlocks copperLantern = Blocks.COPPER_LANTERN;
        copperLantern.forEach(blocks::add);
        return blocks.toArray(new Block[0]);
   }

    public static void register() {
        LANTERN = (BlockEntityType<LanternBlockEntity>) Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.tryBuild("fwa", "lantern"), FabricBlockEntityTypeBuilder.create(LanternBlockEntity::new, lanternBlocks()).build());
        BlockEntityRenderers.register(LANTERN, LanternRenderer::new);
    }
}
