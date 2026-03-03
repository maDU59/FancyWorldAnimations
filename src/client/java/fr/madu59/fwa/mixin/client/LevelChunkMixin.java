package fr.madu59.fwa.mixin.client;

import java.util.function.Consumer;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    @Unique
    private final BlockState fwa$airState = Blocks.AIR.defaultBlockState();

    protected LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void fwa$onReplaceWithPacketData(FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfo ci) {
        if(Minecraft.getInstance().level == null) return;
        LevelChunkSection[] sections = this.getSections();
        
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            fwa$scanSectionFor(section, (LevelChunk) (Object) this, i);
        }
    }

    @Unique
    private void fwa$scanSectionFor(LevelChunkSection section, LevelChunk chunk, int sectionIndex){
        if (section.getStates().maybeHas(state -> state.is(Blocks.END_PORTAL_FRAME) || 
                                              state.is(Blocks.LECTERN) || 
                                              state.is(Blocks.JUKEBOX) || 
                                              state.is(Blocks.BELL))){
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        
                        if (state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.LECTERN) || state.is(Blocks.JUKEBOX) || state.is(Blocks.BELL)) {
                            BlockPos worldPos = chunk.getPos().getWorldPosition().offset(x, chunk.getMinBuildHeight() + y + (sectionIndex * 16), z);
                            fwa$init(state.getBlock(), state, worldPos);
                        }
                    }
                }
            }
        }
    }

	@Unique
	private void fwa$init(Block block, BlockState state, BlockPos blockPos) {
        if (block instanceof LecternBlock){
            FancyWorldAnimationsClient.onBlockUpdate(blockPos, fwa$airState, state);
        }
        if (block instanceof EndPortalFrameBlock){
            FancyWorldAnimationsClient.onBlockUpdate(blockPos, fwa$airState, state);
        }
        if (block instanceof JukeboxBlock){
            FancyWorldAnimationsClient.onBlockUpdate(blockPos, fwa$airState, state);
        }
        if (block instanceof BellBlock){
            FancyWorldAnimationsClient.onBlockUpdate(blockPos, fwa$airState, state);
        }
	}
}