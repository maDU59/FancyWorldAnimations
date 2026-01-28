package fr.madu59.fwa;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.ChiseledBookShelfAnimation;
import fr.madu59.fwa.anims.DoorAnimation;
import fr.madu59.fwa.anims.FenceGateAnimation;
import fr.madu59.fwa.anims.LecternAnimation;
import fr.madu59.fwa.anims.LeverAnimation;
import fr.madu59.fwa.anims.TrapDoorAnimation;
import fr.madu59.fwa.utils.Curves;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FancyWorldAnimationsClient implements ClientModInitializer {

	public static final Minecraft client = Minecraft.getInstance();
	private static final Animations animations = new Animations();

	@Override
	public void onInitializeClient() {
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
		if(!shouldStartAnimation(oldIsOpen, newIsOpen, type, oldState, newState)) return;

		double startTick = (double)client.level.getGameTime();
		synchronized (animations){

			if (animations.containsAt(blockPos)) {
				Animation animation = animations.getAt(blockPos);
				if (animation.isUnique()) {
					startTick = (double)client.level.getGameTime() - animation.getAnimDuration() * (1 - Curves.unease(animation.getProgress(getPartialTick()), animation.getCurve()));
					animations.removeAt(blockPos);
				}
			}

			animations.add(createAnimation(blockPos, type, getDefaultState(newState, type), startTick, oldIsOpen, newIsOpen, oldState, newState));
		}
	}

	private static double getPartialTick() {
		return client.level.getGameTime() + (double) Math.clamp(client.getDeltaTracker().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f);
	}

	private static void render(WorldRenderContext context, double nowTick)
	{
		if(animations.isEmpty() || client.level == null) return;

		Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
		PoseStack poseStack = context.matrices();
		MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();

		for (Animation animation : animations) {
			renderAnimation(animation, nowTick, cameraPos, poseStack, bufferSource);
		}

		bufferSource.endBatch();
		animations.clean(nowTick);
	}

	private static boolean shouldStartAnimation(boolean oldIsOpen, boolean newIsOpen, Type type, BlockState oldState, BlockState newState)
	{
		if(type == Type.CHISELED_BOOKSHELF){
			return oldState.getBlock() == newState.getBlock();
		}
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
		return false;
	}

	private static BlockState getDefaultState(BlockState state, Type type)
	{
		switch (type)
		{
			case DOOR: return state.setValue(DoorBlock.OPEN, false);
			case TRAPDOOR: return state.setValue(TrapDoorBlock.OPEN, false);
			case FENCE_GATE: return state.setValue(FenceGateBlock.OPEN, false);
			case LEVER: return state.setValue(LeverBlock.POWERED, false);
			case LECTERN: return state.setValue(LecternBlock.HAS_BOOK, false);
			default: return state;
		}
	}

	private static Animation createAnimation(BlockPos pos, Type type, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newState, BlockState oldState)
	{
		if(type == Type.DOOR) return new DoorAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		if(type == Type.TRAPDOOR) return new TrapDoorAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		if(type == Type.FENCE_GATE) return new FenceGateAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		if(type == Type.LEVER) return new LeverAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		if(type == Type.CHISELED_BOOKSHELF) return new ChiseledBookShelfAnimation(pos, newState, startTick, oldIsOpen, newIsOpen, oldState);
		if(type == Type.LECTERN) return new LecternAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
		return new Animation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
	}

	private static Type typeOf(BlockState oldState, BlockState newState)
	{
		Block block = oldState.getBlock();
		if(block instanceof DoorBlock) return Type.DOOR;
		if(block instanceof TrapDoorBlock) return Type.TRAPDOOR;
		if(block instanceof FenceGateBlock) return Type.FENCE_GATE;
		if(block instanceof LeverBlock) return Type.LEVER;
		if(block instanceof ChiseledBookShelfBlock) return Type.CHISELED_BOOKSHELF;
		if(block instanceof LecternBlock) return Type.LECTERN;

		block = newState.getBlock();
		if(block instanceof DoorBlock) return Type.DOOR;
		if(block instanceof TrapDoorBlock) return Type.TRAPDOOR;
		if(block instanceof FenceGateBlock) return Type.FENCE_GATE;
		if(block instanceof LeverBlock) return Type.LEVER;
		if(block instanceof ChiseledBookShelfBlock) return Type.CHISELED_BOOKSHELF;
		if(block instanceof LecternBlock) return Type.LECTERN;

		return Type.USELESS;
	}

	private static boolean isSameType(Type type, BlockState state)
	{
		Block block = state.getBlock();
		if(type == Type.DOOR && block instanceof DoorBlock) return true;
		if(type == Type.TRAPDOOR && block instanceof TrapDoorBlock) return true;
		if(type == Type.FENCE_GATE && block instanceof FenceGateBlock) return true;
		if(type == Type.LEVER && block instanceof LeverBlock) return true;
		if(type == Type.CHISELED_BOOKSHELF && block instanceof ChiseledBookShelfBlock) return true;
		if(type == Type.LECTERN && block instanceof LecternBlock) return true;
		return false;
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
		USELESS
	}
}