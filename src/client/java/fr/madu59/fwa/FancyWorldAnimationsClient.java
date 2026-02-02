package fr.madu59.fwa;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.BellAnimation;
import fr.madu59.fwa.anims.ButtonAnimation;
import fr.madu59.fwa.anims.CampfireAnimation;
import fr.madu59.fwa.anims.ChiseledBookShelfAnimation;
import fr.madu59.fwa.anims.ComposterAnimation;
import fr.madu59.fwa.anims.DoorAnimation;
import fr.madu59.fwa.anims.EndPortalFrameAnimation;
import fr.madu59.fwa.anims.FenceGateAnimation;
import fr.madu59.fwa.anims.JukeBoxAnimation;
import fr.madu59.fwa.anims.LayeredCauldronAnimation;
import fr.madu59.fwa.anims.LecternAnimation;
import fr.madu59.fwa.anims.LeverAnimation;
import fr.madu59.fwa.anims.RepeaterAnimation;
import fr.madu59.fwa.anims.TrapDoorAnimation;
import fr.madu59.fwa.anims.TripWireHookAnimation;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.config.configscreen.FancyWorldAnimationsConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FancyWorldAnimationsClient implements ClientModInitializer {

	public static final Minecraft client = Minecraft.getInstance();
	private static final Animations animations = new Animations();

	@Override
	public void onInitializeClient() {
		FancyWorldAnimationsConfigScreen.registerCommand();
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			double tickDelta = getPartialTick();
            render(context, tickDelta);
		});
	}

	public static void onBlockUpdate(BlockPos blockPos, BlockState oldState, BlockState newState)
	{
		if(client.level == null) return;
		Type type = typeOf(oldState, newState);

		if(type == Type.USELESS){
			synchronized (animations){
				animations.removeAt(blockPos);
			}
			return;
		}

		if(!isSameType(type, newState)){
			synchronized (animations){
				animations.removeAt(blockPos);
			}
			return;
		}

		boolean oldIsOpen = isOpen(oldState);
		boolean newIsOpen = isOpen(newState);

		double startTick = (double)client.level.getGameTime();
		synchronized (animations){

			if (animations.containsAt(blockPos)) {
				Animation animation = animations.getAt(blockPos);
				if (animation.isUnique()) {
					startTick = (double)client.level.getGameTime() - animation.getAnimDuration() * (1 - animation.getProgress(getPartialTick()));
					animations.removeAt(blockPos);
				}
			}

			if(!shouldStartAnimation(oldIsOpen, newIsOpen, type, oldState, newState)) return;

			Animation animation = createAnimation(blockPos, type, getDefaultState(newState, type), startTick, oldIsOpen, newIsOpen, oldState, newState);
			if (animation.isEnabled()) animations.add(blockPos, animation);
		}
	}

	private static double getPartialTick() {
		return client.level.getGameTime() + (double) Math.clamp(client.getDeltaTracker().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f);
	}

	private static void render(WorldRenderContext context, double nowTick)
	{
		if(animations.isEmpty() || client.level == null) return;

		Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
		PoseStack poseStack = context.matrixStack();
		MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();

		for (Animation animation : animations.animations.values()) {
			renderAnimation(animation, nowTick, cameraPos, poseStack, bufferSource);
		}

		bufferSource.endBatch();
		animations.clean(nowTick);
	}

	private static boolean shouldStartAnimation(boolean oldIsOpen, boolean newIsOpen, Type type, BlockState oldState, BlockState newState)
	{
		if(type == Type.END_PORTAL_FRAME && SettingsManager.END_PORTAL_FRAME_INFINITE.getValue()) return true;
		if(type == Type.CHISELED_BOOKSHELF) return oldState.getBlock() == newState.getBlock();
		if(type == Type.JUKEBOX) return newState.getValue(JukeboxBlock.HAS_RECORD);
		if(type == Type.REPEATER) return oldState.getBlock() == newState.getBlock() && newState.getValue(RepeaterBlock.DELAY) != oldState.getValue(RepeaterBlock.DELAY);
		if(type == Type.LAYERED_CAULDRON) {
			if (oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof LayeredCauldronBlock && newState.getValue(LayeredCauldronBlock.LEVEL) != oldState.getValue(LayeredCauldronBlock.LEVEL)) return true;
			if (oldState.getBlock() != newState.getBlock() && (newState.getBlock() instanceof LayeredCauldronBlock && oldState.getBlock() instanceof CauldronBlock)|| (oldState.getBlock() instanceof LayeredCauldronBlock && newState.getBlock() instanceof CauldronBlock)) return true;
			if (oldState.getBlock() != newState.getBlock() && ((newState.getBlock() instanceof LavaCauldronBlock && oldState.getBlock() instanceof CauldronBlock) || (oldState.getBlock() instanceof LavaCauldronBlock && newState.getBlock() instanceof CauldronBlock))) return true;
			return false;
		}
		if(type == Type.COMPOSTER) return oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof ComposterBlock && newState.getValue(ComposterBlock.LEVEL) != oldState.getValue(ComposterBlock.LEVEL);
		return oldIsOpen != newIsOpen;
	}

	private static void renderAnimation(Animation animation, double nowTick, Vec3 cameraPos, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource)
	{
		BlockPos pos = animation.getPos();

		poseStack.pushPose();
		poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
		animation.render(poseStack, bufferSource, nowTick);
		poseStack.popPose();
	}

	private static boolean isOpen(BlockState state)
	{
		Block block = state.getBlock();
		if(block instanceof DoorBlock) return state.getValue(DoorBlock.OPEN);
		if(block instanceof TrapDoorBlock) return state.getValue(TrapDoorBlock.OPEN);
		if(block instanceof FenceGateBlock) return state.getValue(FenceGateBlock.OPEN);
		if(block instanceof LeverBlock) return state.getValue(LeverBlock.POWERED);
		if(block instanceof LecternBlock) return state.getValue(LecternBlock.HAS_BOOK);
		if(block instanceof ButtonBlock) return state.getValue(ButtonBlock.POWERED);
		if(block instanceof JukeboxBlock) return state.getValue(JukeboxBlock.HAS_RECORD);
		if(block instanceof EndPortalFrameBlock) return state.getValue(EndPortalFrameBlock.HAS_EYE);
		if(block instanceof BellBlock) return true;
		if(block instanceof CampfireBlock) return state.getValue(CampfireBlock.LIT);
		if(block instanceof TripWireHookBlock) return state.getValue(TripWireHookBlock.ATTACHED);
		return false;
	}

	private static BlockState getDefaultState(BlockState state, Type type)
	{
		return switch (type)
		{
			case DOOR -> state.setValue(DoorBlock.OPEN, false);
			case TRAPDOOR -> state.setValue(TrapDoorBlock.OPEN, false);
			case FENCE_GATE -> state.setValue(FenceGateBlock.OPEN, false);
			case LEVER -> state.setValue(LeverBlock.POWERED, false);
			case LECTERN -> state.setValue(LecternBlock.HAS_BOOK, false);
			case BUTTON -> state.setValue(ButtonBlock.POWERED, false);
			case JUKEBOX -> state.setValue(JukeboxBlock.HAS_RECORD, false);
			case END_PORTAL_FRAME -> state.setValue(EndPortalFrameBlock.HAS_EYE, false);
			case REPEATER -> state.setValue(RepeaterBlock.DELAY, 1);
			case CAMPFIRE -> state.setValue(CampfireBlock.LIT, false);
			case TRIPWIRE_HOOK -> state.setValue(TripWireHookBlock.ATTACHED, false);
			default -> state;
		};
	}

	private static Animation createAnimation(BlockPos pos, Type type, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState)
	{
		switch (type)
		{
			case DOOR: return new DoorAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case TRAPDOOR: return new TrapDoorAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case FENCE_GATE: return new FenceGateAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case LEVER: return new LeverAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case CHISELED_BOOKSHELF: return new ChiseledBookShelfAnimation(pos, newState, startTick, oldIsOpen, newIsOpen, oldState);
			case LECTERN: return new LecternAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case BUTTON: return new ButtonAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case JUKEBOX: return new JukeBoxAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState);
			case END_PORTAL_FRAME: return new EndPortalFrameAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case REPEATER: return new RepeaterAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			case LAYERED_CAULDRON: return new LayeredCauldronAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			case BELL: return new BellAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			case CAMPFIRE: return new CampfireAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, oldState, newState);
			case COMPOSTER: return new ComposterAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			case TRIPWIRE_HOOK: return new TripWireHookAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
			default: return new Animation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		}
	}

	private static Type typeOf(BlockState state){
		Block block = state.getBlock();
		if(block instanceof DoorBlock) return Type.DOOR;
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
		synchronized (animations){
			if (animations.containsAt(pos)) {
				Animation animation = animations.getAt(pos);
				return animation.hideOriginalBlockEntity();
			}
			else{
				return false;
			}
		}
	}

	public static boolean shouldCancelBlockRendering(BlockPos pos)
	{
		synchronized (animations){
			if (animations.containsAt(pos)) {
				Animation animation = animations.getAt(pos);
				return animation.hideOriginalBlock();
			}
			else{
				return false;
			}
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
		USELESS
	}
}