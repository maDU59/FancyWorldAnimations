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
import fr.madu59.fwa.anims.VaultAnimation;
import fr.madu59.fwa.compat.Blacklist;
import fr.madu59.fwa.compat.BlacklistReloadListener;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.config.configscreen.FancyWorldAnimationsConfigScreen;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = FancyWorldAnimations.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FancyWorldAnimations.MOD_ID, value = Dist.CLIENT)
public class FancyWorldAnimationsClient{

	private static final Animations animations = new Animations();

	public FancyWorldAnimationsClient(ModContainer container, IEventBus bus){
        NeoForge.EVENT_BUS.register(FancyWorldAnimationsConfigScreen.class);
        container.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> {
            return new FancyWorldAnimationsConfigScreen(parent);
        });
    }

	@SubscribeEvent
    public static void onRegisterClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.tryParse("fwa:blacklist-loader"), new BlacklistReloadListener());
    }

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event){
		animations.animations.clear();
	}

	@SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterOpaqueBlocks event) {
		if(SettingsManager.MOD_TOGGLE.getValue()) {
			double tickDelta = getPartialTick();
			render(new AnimationRenderingContext(event.getPoseStack(), Minecraft.getInstance().gameRenderer.getMainCamera().position(), Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(), tickDelta));
		}
    }

	public static void onBlockUpdate(BlockPos blockPos, BlockState oldState, BlockState newState)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null) return;
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

		double startTick = (double)level.getGameTime();

		if (animations.containsAt(blockPos)) {
			Animation animation = animations.getAt(blockPos);
			if (animation.isUnique()) {
				startTick = (double)level.getGameTime() - animation.getAnimDuration() * (1 - animation.getProgress(getPartialTick()));
				animations.removeAt(blockPos);
			}
		}

		if(!shouldStartAnimation(oldIsOpen, newIsOpen, type, oldState, newState)) return;

		Animation animation = createAnimation(blockPos, type, getDefaultState(newState, type), startTick, oldIsOpen, newIsOpen, oldState, newState);
		if (animation.isEnabled()) animations.add(blockPos, animation);
	}

	public static double getPartialTick() {
		return (double) Minecraft.getInstance().level.getGameTime() + (double) Math.clamp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f);
	}

	public static void render(AnimationRenderingContext context)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null){
			animations.animations.clear();
			return;
		}
		if(animations.isEmpty()) return;

		for (Animation animation : animations.animations.values()) {
			renderAnimation(animation, context);
		}
		animations.clean(context.getNowTick());
	}

	private static boolean shouldStartAnimation(boolean oldIsOpen, boolean newIsOpen, Type type, BlockState oldState, BlockState newState)
	{
		if(type == Type.END_PORTAL_FRAME && SettingsManager.END_PORTAL_FRAME_INFINITE.getValue()) return true;
		if(type == Type.CHISELED_BOOKSHELF) return oldState.getBlock() == newState.getBlock();
		if(type == Type.JUKEBOX) return newState.getValue(BlockStateProperties.HAS_RECORD);
		if(type == Type.REPEATER) return oldState.getBlock() == newState.getBlock() && newState.getValue(BlockStateProperties.DELAY) != oldState.getValue(BlockStateProperties.DELAY);
		if(type == Type.LAYERED_CAULDRON) {
			if (oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof LayeredCauldronBlock && newState.getValue(BlockStateProperties.LEVEL) != oldState.getValue(BlockStateProperties.LEVEL)) return true;
			if (oldState.getBlock() != newState.getBlock() && (newState.getBlock() instanceof LayeredCauldronBlock && oldState.getBlock() instanceof CauldronBlock)|| (oldState.getBlock() instanceof LayeredCauldronBlock && newState.getBlock() instanceof CauldronBlock)) return true;
			if (oldState.getBlock() != newState.getBlock() && ((newState.getBlock() instanceof LavaCauldronBlock && oldState.getBlock() instanceof CauldronBlock) || (oldState.getBlock() instanceof LavaCauldronBlock && newState.getBlock() instanceof CauldronBlock))) return true;
			return false;
		}
		if(type == Type.LANTERN) return newState.getValue(BlockStateProperties.HANGING);
		if(type == Type.CHAIN) return newState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y;
		if(type == Type.COMPOSTER) return oldState.getBlock() == newState.getBlock() && newState.getBlock() instanceof ComposterBlock && newState.getValue(BlockStateProperties.LEVEL) != oldState.getValue(BlockStateProperties.LEVEL);
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
		if(block instanceof DoorBlock || BuiltInRegistries.BLOCK.getKey(block).getNamespace() == "dramaticdoors") return state.getValue(BlockStateProperties.OPEN);
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
		if(block instanceof VaultBlock) return state.getValue(BlockStateProperties.VAULT_STATE) == VaultState.UNLOCKING;
		return false;
	}

	private static BlockState getDefaultState(BlockState state, Type type)
	{
		return switch (type)
		{
			case DOOR -> state.setValue(BlockStateProperties.OPEN, false);
			case TRAPDOOR -> state.setValue(BlockStateProperties.OPEN, false);
			case FENCE_GATE -> state.setValue(BlockStateProperties.OPEN, false);
			case LEVER -> state.setValue(BlockStateProperties.POWERED, false);
			case LECTERN -> state.setValue(BlockStateProperties.HAS_BOOK, false);
			case BUTTON -> state.setValue(BlockStateProperties.POWERED, false);
			case JUKEBOX -> state.setValue(BlockStateProperties.HAS_RECORD, false);
			case END_PORTAL_FRAME -> state.setValue(BlockStateProperties.EYE, false);
			case REPEATER -> state.setValue(BlockStateProperties.DELAY, 1);
			case CAMPFIRE -> state.setValue(BlockStateProperties.LIT, false);
			case TRIPWIRE_HOOK -> state.setValue(BlockStateProperties.ATTACHED, false);
			case VAULT -> state.setValue(BlockStateProperties.VAULT_STATE, VaultState.UNLOCKING);
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
			case VAULT: return new VaultAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			case LANTERN: return new LanternAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			case CHAIN: return new ChainAnimation(pos, defaultState, startTick, oldIsOpen, newIsOpen, newState, oldState);
			default: return new Animation(pos, defaultState, startTick, oldIsOpen, newIsOpen);
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
		if(block instanceof VaultBlock) return Type.VAULT;
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
		if (animations.containsAt(pos)) {
			Animation animation = animations.getAt(pos);
			return animation.hideOriginalBlockEntity() && !animation.isForRemoval() && SettingsManager.MOD_TOGGLE.getValue();
		}
		else{
			return false;
		}
	}

	public static boolean shouldCancelBlockRendering(BlockPos pos)
	{
		if (animations.containsAt(pos)) {
			Animation animation = animations.getAt(pos);
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
		VAULT,
		LANTERN,
		CHAIN,
		USELESS
	}
}