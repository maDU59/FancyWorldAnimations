package fr.madu59.fwa;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import fr.madu59.fwa.anims.ButtonAnimation;
import fr.madu59.fwa.anims.CampfireAnimation;
import fr.madu59.fwa.anims.ChainAnimation;
import fr.madu59.fwa.anims.ChiseledBookShelfAnimation;
import fr.madu59.fwa.anims.ComposterAnimation;
import fr.madu59.fwa.anims.DoorAnimation;
import fr.madu59.fwa.anims.EndPortalFrameAnimation;
import fr.madu59.fwa.anims.FenceGateAnimation;
import fr.madu59.fwa.anims.JukeBoxAnimation;
import fr.madu59.fwa.anims.LanternAnimation;
import fr.madu59.fwa.anims.LayeredCauldronAnimation;
import fr.madu59.fwa.anims.LecternAnimation;
import fr.madu59.fwa.anims.LeverAnimation;
import fr.madu59.fwa.anims.RepeaterAnimation;
import fr.madu59.fwa.anims.TrapDoorAnimation;
import fr.madu59.fwa.anims.TripWireHookAnimation;
import fr.madu59.fwa.compat.Blacklist;
import fr.madu59.fwa.compat.BlacklistReloadListener;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.config.configscreen.FancyWorldAnimationsConfigScreen;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FancyWorldAnimationsClient implements ClientModInitializer {

	public static final Animations animations = new Animations();
	private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
	private static ResourceKey<Level> dimension;

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BlacklistReloadListener());
		FancyWorldAnimationsConfigScreen.registerCommand();
		ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, client) -> {
            animations.animations.clear();
        });
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			if(SettingsManager.MOD_TOGGLE.getValue()) {
				double tickDelta = getPartialTick();
				render(new AnimationRenderingContext(context.matrixStack(), context.camera().getPosition(), context.consumers(), context.frustum(), tickDelta, false));
			}
		});
	}

	public static void onBlockUpdate(BlockPos blockPos, BlockState oldState, BlockState newState){
		onBlockUpdate(blockPos, oldState, newState, true);
	}

	public static void onBlockUpdate(BlockPos blockPos, BlockState oldState, BlockState newState, boolean propagateChain)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null) return;

		if(SettingsManager.LANTERN_STATE.getValue() || SettingsManager.CHAIN_STATE.getValue()){
			if(propagateChain){
				propagateChain(level, blockPos, newState);
			}
		}

		Type type = typeOf(oldState, newState);

		if(type == Type.USELESS){
			animations.removeAt(blockPos);
			return;
		}

		if(!isSameType(type, newState)){
			animations.removeAt(blockPos);
			return;
		}

		if(Blacklist.isBlacklisted(newState)){
			return;
		}

		boolean oldIsOpen = isOpen(oldState);
		boolean newIsOpen = isOpen(newState);

		double startTick = getPartialTick();

		if (animations.containsAt(blockPos)) {
			Animation animation = animations.getAt(blockPos);
			if (animation.isUnique()) {
				startTick = getPartialTick() - animation.getAnimDuration() * (1 - animation.getProgress(getPartialTick()));
				animations.removeAt(blockPos);
			}
		}

		if(!shouldStartAnimation(oldIsOpen, newIsOpen, type, oldState, newState, blockPos)) return;

		Animation animation = createAnimation(blockPos, type, startTick, oldIsOpen, newIsOpen, oldState, newState);
		if (animation.isEnabled()) animations.add(blockPos, animation);
	}

	public static double getPartialTick() {
		return System.nanoTime() / 50_000_000.0;
	}

	public static void render(AnimationRenderingContext context)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null){
			animations.animations.clear();
			return;
		}
		if(dimension != null && dimension != level.dimension()){
			animations.animations.clear();
			dimension = level.dimension();
			return;
		}
		if(animations.isEmpty()) return;

		RenderHelper.prepareFrame(context);

		for (Animation animation : animations.animations.values()) {
			if(context.getFrustum() == null || context.getFrustum().isVisible(animation.getBoundingBox())){
				renderAnimation(animation, context);
				if (context.getBufferSource() instanceof MultiBufferSource.BufferSource source && SettingsManager.MAX_SHADER_COMPAT.getValue() && IRIS_LOADED){
					source.endBatch();
				}
			}
		}
		dimension = level.dimension();
		animations.clean(context.getNowTick());
	}

	private static boolean shouldStartAnimation(boolean oldIsOpen, boolean newIsOpen, Type type, BlockState oldState, BlockState newState, BlockPos pos)
	{
		if(type == Type.END_PORTAL_FRAME && SettingsManager.END_PORTAL_FRAME_INFINITE.getValue()) return true;
		if(type == Type.CHISELED_BOOKSHELF) return oldState.getBlock() == newState.getBlock();
		if(type == Type.JUKEBOX) return newState.getValue(BlockStateProperties.HAS_RECORD);
		if(type == Type.REPEATER) return oldState.getBlock() == newState.getBlock() && newState.getValue(BlockStateProperties.DELAY) != oldState.getValue(BlockStateProperties.DELAY);
		if(type == Type.LAYERED_CAULDRON) {
			if (oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof LayeredCauldronBlock && newState.getValue(BlockStateProperties.LEVEL_CAULDRON) != oldState.getValue(BlockStateProperties.LEVEL_CAULDRON)) return true;
			if (oldState.getBlock() != newState.getBlock() && (newState.getBlock() instanceof LayeredCauldronBlock && oldState.getBlock() instanceof CauldronBlock)|| (oldState.getBlock() instanceof LayeredCauldronBlock && newState.getBlock() instanceof CauldronBlock)) return true;
			if (oldState.getBlock() != newState.getBlock() && ((newState.getBlock() instanceof LavaCauldronBlock && oldState.getBlock() instanceof CauldronBlock) || (oldState.getBlock() instanceof LavaCauldronBlock && newState.getBlock() instanceof CauldronBlock))) return true;
			return false;
		}
		if(type == Type.LANTERN) return SwingingBlockHelper.isHangingLantern(newState);
		if(type == Type.CHAIN) {
			return SwingingBlockHelper.isVerticalChain(newState) && (!SettingsManager.CHAIN_GROUNDED.getValue() || !SwingingBlockHelper.isLastGrounded(pos));
		}
		if(type == Type.COMPOSTER) return oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof ComposterBlock && newState.getValue(BlockStateProperties.LEVEL_COMPOSTER) != oldState.getValue(BlockStateProperties.LEVEL_COMPOSTER);
		return oldIsOpen != newIsOpen;
	}

	private static void renderAnimation(Animation animation, AnimationRenderingContext context)
	{
		BlockPos pos = animation.getPos();
		PoseStack poseStack = context.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(pos.getX() - context.getCameraPos().x, pos.getY() - context.getCameraPos().y, pos.getZ() - context.getCameraPos().z);
		animation.render(context);
		poseStack.popPose();
	}

	private static boolean isOpen(BlockState state)
	{
		Block block = state.getBlock();
		if(block instanceof DoorBlock || "dramaticdoors".equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) return state.getValue(BlockStateProperties.OPEN);
		if(block instanceof TrapDoorBlock) return state.getValue(BlockStateProperties.OPEN);
		if(block instanceof FenceGateBlock) return state.getValue(BlockStateProperties.OPEN);
		if(block instanceof LeverBlock) return state.getValue(BlockStateProperties.POWERED);
		if(block instanceof LecternBlock) return state.getValue(BlockStateProperties.HAS_BOOK);
		if(block instanceof ButtonBlock) return state.getValue(BlockStateProperties.POWERED);
		if(block instanceof JukeboxBlock) return state.getValue(BlockStateProperties.HAS_RECORD);
		if(block instanceof EndPortalFrameBlock) return state.getValue(BlockStateProperties.EYE);
		if(block instanceof BellBlock) return true;
		if(block instanceof CampfireBlock) return state.getValue(CampfireBlock.LIT);
		if(block instanceof TripWireHookBlock) return state.getValue(BlockStateProperties.ATTACHED);
		return false;
	}

	private static Animation createAnimation(BlockPos pos, Type type, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState)
	{
		switch (type)
		{
			case DOOR: return new DoorAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case TRAPDOOR: return new TrapDoorAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case FENCE_GATE: return new FenceGateAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case LEVER: return new LeverAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case CHISELED_BOOKSHELF: return new ChiseledBookShelfAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case LECTERN: return new LecternAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case BUTTON: return new ButtonAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case JUKEBOX: return new JukeBoxAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case END_PORTAL_FRAME: return new EndPortalFrameAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case REPEATER: return new RepeaterAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case LAYERED_CAULDRON: return new LayeredCauldronAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case BELL: return new BellAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case CAMPFIRE: return new CampfireAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case COMPOSTER: return new ComposterAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case TRIPWIRE_HOOK: return new TripWireHookAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case LANTERN: return new LanternAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case CHAIN: return new ChainAnimation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
			default: return new Animation(pos, startTick, oldIsOpen, newIsOpen, oldState, newState);
		}
	}

	private static Type typeOf(BlockState state){
		Block block = state.getBlock();
		if(block instanceof DoorBlock || BuiltInRegistries.BLOCK.getKey(block).getNamespace() == "dramaticdoors") return Type.DOOR;
		if(block instanceof TrapDoorBlock) return Type.TRAPDOOR;
		if(block instanceof FenceGateBlock) return Type.FENCE_GATE;
		if(block instanceof LeverBlock) return Type.LEVER;
		if(block instanceof ChiseledBookShelfBlock) return Type.CHISELED_BOOKSHELF;
		if(block instanceof LecternBlock) return Type.LECTERN;
		if(block instanceof ButtonBlock) return Type.BUTTON;
		if(block instanceof JukeboxBlock) return Type.JUKEBOX;
		if(block instanceof EndPortalFrameBlock) return Type.END_PORTAL_FRAME;
		if(block instanceof RepeaterBlock) return Type.REPEATER;
		if(block instanceof LayeredCauldronBlock || block instanceof CauldronBlock || block instanceof LavaCauldronBlock) return Type.LAYERED_CAULDRON;
		if(block instanceof BellBlock) return Type.BELL;
		if(block instanceof CampfireBlock) return Type.CAMPFIRE;
		if(block instanceof ComposterBlock) return Type.COMPOSTER;
		//if(block instanceof TripWireHookBlock) return Type.TRIPWIRE_HOOK;
		if(block instanceof LanternBlock) return Type.LANTERN;
		if(block instanceof ChainBlock) return Type.CHAIN;
		return Type.USELESS;
	}

	private static Type typeOf(BlockState oldState, BlockState newState)
	{
		Type type = typeOf(oldState);
		if (type != Type.USELESS) return type;
		else return typeOf(newState);
	}

	private static boolean isSameType(Type type, BlockState state)
	{
		return type == typeOf(state);
	}

	public static boolean shouldCancelBlockEntityRendering(BlockPos pos)
	{
		Animation animation = animations.getAt(pos);
		if (animation != null) {
			return animation.hideOriginalBlockEntity() && !animation.isForRemoval() && SettingsManager.MOD_TOGGLE.getValue();
		}
		else{
			return false;
		}
	}

	public static boolean shouldCancelBlockRendering(BlockPos pos)
	{
		Animation animation = animations.getAt(pos);
		if (animation != null) {
			if(animation.isForRemoval()) animation.approveRemoval(getPartialTick());
			return animation.hideOriginalBlock()  && !animation.isForRemoval()  && SettingsManager.MOD_TOGGLE.getValue();
		}
		else{
			return false;
		}
	}

	public static void removeAnimationAt(BlockPos position){
		animations.removeAt(position);
	}

	public static void safeRemoveAnimationAt(BlockPos position){
		animations.removeSafeAt(position);
	}

	public static void propagateChain(ClientLevel level, BlockPos blockPos, BlockState newState){
		if(SettingsManager.CHAIN_STATE.getValue()){
			if(SettingsManager.CHAIN_GROUNDED.getValue() && (!newState.isAir() && (!SwingingBlockHelper.isSwingingBlock(newState) || SwingingBlockHelper.isLastGrounded(blockPos)))){
				BlockPos abovePos = blockPos.above();
				while(true) {
					Animation anim = animations.animations.get(abovePos);
					if (anim == null || !SwingingBlockHelper.isVerticalChain(anim)) break;
					anim.markForRemoval();
					abovePos = abovePos.above();
				}
			}
			if(newState.isAir() || (SwingingBlockHelper.isSwingingBlock(newState) && !SwingingBlockHelper.isLastGrounded(blockPos))){
				BlockPos abovePos = blockPos.above();
				while(SwingingBlockHelper.isVerticalChain(level.getBlockState(abovePos)) && !animations.animations.containsKey(abovePos)){
					BlockState aboveState = level.getBlockState(abovePos);
					onBlockUpdate(abovePos, aboveState, aboveState, false);
					abovePos = abovePos.above();
				}
			}
		}
		BlockPos abovePos = blockPos.above();
		if(SwingingBlockHelper.isActiveSwingingBlock(newState)){
			if(SwingingBlockHelper.isVerticalChain(level.getBlockState(abovePos))){
				Animation anim = animations.animations.get(abovePos);
				if (anim != null) anim.setLast(false);
			}
		}
		else{
			if(SwingingBlockHelper.isVerticalChain(level.getBlockState(abovePos))){
				Animation anim = animations.animations.get(abovePos);
				if (anim != null) anim.setLast(true);
			}
		}
		BlockPos belowPos = blockPos.below();
		if(SwingingBlockHelper.isSwingingBlock(level.getBlockState(belowPos))){
			BlockPos lastPos = SwingingBlockHelper.getLast(belowPos);
			Animation anim = animations.animations.get(lastPos);
			if (anim != null) anim.needUpdate();
		}
	}

	public static enum Type
	{
		DOOR,
		TRAPDOOR,
		FENCE_GATE,
		LEVER,
		CHISELED_BOOKSHELF,
		LECTERN,
		BUTTON,
		JUKEBOX,
		END_PORTAL_FRAME,
		REPEATER,
		LAYERED_CAULDRON,
		BELL,
		CAMPFIRE,
		COMPOSTER,
		TRIPWIRE_HOOK,
		LANTERN,
		CHAIN,
		USELESS
	}
}