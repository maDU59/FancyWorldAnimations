package fr.madu59.fwa.mixin.client;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    @Unique
    private static ClientLevel fwa$level;

    @Unique
    private static final BlockState fwa$AIR_STATE = Blocks.AIR.defaultBlockState();

    @Unique
    private static final Predicate<BlockState> IS_ANIMATED_BLOCK = state -> {
        FancyWorldAnimationsClient.Type type = FancyWorldAnimationsClient.typeOf(state);
        return type == FancyWorldAnimationsClient.Type.END_PORTAL_FRAME
            || type == FancyWorldAnimationsClient.Type.LECTERN
            || type == FancyWorldAnimationsClient.Type.JUKEBOX
            || type == FancyWorldAnimationsClient.Type.BELL
            || type == FancyWorldAnimationsClient.Type.LANTERN
            || type == FancyWorldAnimationsClient.Type.CHAIN;
    };

    protected LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void fwa$onReplaceWithPacketData(FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfo ci) {
        if(Minecraft.getInstance().level == null) return;

        if (Minecraft.getInstance().level != fwa$level) {
            FancyWorldAnimationsClient.animations.animations.clear();
            fwa$level = Minecraft.getInstance().level;
        }

        LevelChunkSection[] sections = this.getSections();
        BlockPos chunkPos = this.getPos().getWorldPosition();
        int chunkMinY = this.getMinBuildHeight();
        
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;
            fwa$scanSectionFor(section, chunkPos, chunkMinY + i * 16);
        }
    }

    @Unique
    private void fwa$scanSectionFor(LevelChunkSection section, BlockPos pos, int minY){
        if (section.getStates().maybeHas(IS_ANIMATED_BLOCK) || FancyWorldAnimationsClient.animations.hasSection(pos)){
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        BlockPos worldPos = pos.offset(x, minY + y, z);

                        if (IS_ANIMATED_BLOCK.test(state)) {
                            FancyWorldAnimationsClient.onBlockUpdate(worldPos, fwa$AIR_STATE, state);
                        }
                        else if (FancyWorldAnimationsClient.animations.containsAt(worldPos)) {
                            FancyWorldAnimationsClient.animations.removeSafeAt(pos);
                        }
                    }
                }
            }
        }
    }
}