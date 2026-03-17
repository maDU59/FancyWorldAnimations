package fr.madu59.fwa.mixin.client;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    @Unique
    private final BlockState fwa$AIR_STATE = Blocks.AIR.defaultBlockState();

    protected LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, LevelChunkSection @Nullable [] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void fwa$onReplaceWithPacketData(FriendlyByteBuf friendlyByteBuf, Map<Heightmap.Types, long[]> map, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfo ci) {
        if(Minecraft.getInstance().level == null) return;
        LevelChunkSection[] sections = this.getSections();
        BlockPos chunkPos = this.getPos().getWorldPosition();
        int chunkMinY = this.getMinY();
        
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            fwa$scanSectionFor(section, chunkPos, chunkMinY + i * 16);
        }
    }

    @Unique
    private void fwa$scanSectionFor(LevelChunkSection section, BlockPos pos, int minY){
        if (section.getStates().maybeHas(state -> state.is(Blocks.END_PORTAL_FRAME) || 
                                              state.is(Blocks.LECTERN) || 
                                              state.is(Blocks.JUKEBOX) || 
                                              state.is(Blocks.BELL)    ||
                                              state.getBlock() instanceof LanternBlock ||
                                              state.getBlock() instanceof ChainBlock)){
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        
                        if (state.is(Blocks.END_PORTAL_FRAME) || 
                                              state.is(Blocks.LECTERN) || 
                                              state.is(Blocks.JUKEBOX) || 
                                              state.is(Blocks.BELL)    ||
                                              state.getBlock() instanceof LanternBlock ||
                                              state.getBlock() instanceof ChainBlock) {
                            BlockPos worldPos = pos.offset(x, minY + y, z);
                            FancyWorldAnimationsClient.onBlockUpdate(worldPos, fwa$AIR_STATE, state);
                        }
                    }
                }
            }
        }
    }
}