package fr.madu59.fwa.config.configscreen;

import static net.minecraft.commands.Commands.literal;

import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = "fwa", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FancyWorldAnimationsConfigScreen extends Screen {
    
    private MyConfigListWidget list;
    private final Screen parent;

    public FancyWorldAnimationsConfigScreen(Screen parent) {
        super(Component.literal("Fwa configuration screen"));
        this.parent = parent;
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            literal("fwaConfig")
                .executes(context -> {
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new FancyWorldAnimationsConfigScreen(null)));
                        return 1;
                })
        );
    }

    @Override
    protected void init() {
        super.init();
        this.list = new MyConfigListWidget(this.minecraft, this.width, this.height - 80, 40, 26);

        list.category("fwa.config.category.general").build();
        list.button(SettingsManager.MOD_TOGGLE).build();
        list.slider(SettingsManager.ANIMATION_RENDER_DISTANCE).range(32, 1024).step(16).build();
        list.slider(SettingsManager.INFINITE_ANIMATION_RENDER_DISTANCE).range(32, 1024).step(16).build();
        list.slider(SettingsManager.SHADOW_ANIMATION_RENDER_DISTANCE).range(0.4f, 1.0f).step(0.1f).isEnabled(() -> ModCompat.isIrisLoaded()).build();
        
        list.category("fwa.config.category.door").build();
        list.button(SettingsManager.DOOR_STATE).build();
        list.button(SettingsManager.DOOR_EASING).isEnabled(() -> SettingsManager.DOOR_STATE.getValue()).build();
        list.slider(SettingsManager.DOOR_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.DOOR_STATE.getValue()).build();
        
        list.category("fwa.config.category.trapdoor").build();
        list.button(SettingsManager.TRAPDOOR_STATE).build();
        list.button(SettingsManager.TRAPDOOR_EASING).isEnabled(() -> SettingsManager.TRAPDOOR_STATE.getValue()).build();
        list.slider(SettingsManager.TRAPDOOR_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.TRAPDOOR_STATE.getValue()).build();
        
        list.category("fwa.config.category.fencegate").build();
        list.button(SettingsManager.FENCEGATE_STATE).build();
        list.button(SettingsManager.FENCEGATE_EASING).isEnabled(() -> SettingsManager.FENCEGATE_STATE.getValue()).build();
        list.slider(SettingsManager.FENCEGATE_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.FENCEGATE_STATE.getValue()).build();
        
        list.category("fwa.config.category.lectern").build();
        list.button(SettingsManager.LECTERN_STATE).build();
        list.button(SettingsManager.LECTERN_EASING).isEnabled(() -> SettingsManager.LECTERN_STATE.getValue()).build();
        list.slider(SettingsManager.LECTERN_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.LECTERN_STATE.getValue()).build();
        list.button(SettingsManager.LECTERN_INFINITE).isEnabled(() -> SettingsManager.LECTERN_STATE.getValue()).build();
        
        list.category("fwa.config.category.chiseled_bookshelf").build();
        list.button(SettingsManager.CHISELED_BOOKSHELF_STATE).build();
        list.button(SettingsManager.CHISELED_BOOKSHELF_EASING).isEnabled(() -> SettingsManager.CHISELED_BOOKSHELF_STATE.getValue()).build();
        list.slider(SettingsManager.CHISELED_BOOKSHELF_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.CHISELED_BOOKSHELF_STATE.getValue()).build();
        
        list.category("fwa.config.category.jukebox").build();
        list.button(SettingsManager.JUKEBOX_STATE).build();
        list.button(SettingsManager.JUKEBOX_EASING).isEnabled(() -> SettingsManager.JUKEBOX_STATE.getValue()).build();
        list.slider(SettingsManager.JUKEBOX_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.JUKEBOX_STATE.getValue()).build();
        list.button(SettingsManager.JUKEBOX_INFINITE).isEnabled(() -> SettingsManager.JUKEBOX_STATE.getValue()).build();
        
        list.category("fwa.config.category.cauldron").build();
        list.button(SettingsManager.CAULDRON_STATE).build();
        list.button(SettingsManager.CAULDRON_EASING).isEnabled(() -> SettingsManager.CAULDRON_STATE.getValue()).build();
        list.slider(SettingsManager.CAULDRON_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.CAULDRON_STATE.getValue()).build();
        
        list.category("fwa.config.category.composter").build();
        list.button(SettingsManager.COMPOSTER_STATE).build();
        list.button(SettingsManager.COMPOSTER_EASING).isEnabled(() -> SettingsManager.COMPOSTER_STATE.getValue()).build();
        list.slider(SettingsManager.COMPOSTER_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.COMPOSTER_STATE.getValue()).build();
        
        list.category("fwa.config.category.lever").build();
        list.button(SettingsManager.LEVER_STATE).build();
        list.button(SettingsManager.LEVER_EASING).isEnabled(() -> SettingsManager.LEVER_STATE.getValue()).build();
        list.slider(SettingsManager.LEVER_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.LEVER_STATE.getValue()).build();
        list.button(SettingsManager.LEVER_SPLIT).isEnabled(() -> SettingsManager.LEVER_STATE.getValue()).build();
        
        list.category("fwa.config.category.button").build();
        list.button(SettingsManager.BUTTON_STATE).build();
        list.button(SettingsManager.BUTTON_EASING).isEnabled(() -> SettingsManager.BUTTON_STATE.getValue()).build();
        list.slider(SettingsManager.BUTTON_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.BUTTON_STATE.getValue()).build();
        
        list.category("fwa.config.category.redstone").build();
        list.button(SettingsManager.REDSTONE_STATE).build();
        list.button(SettingsManager.REDSTONE_EASING).isEnabled(() -> SettingsManager.REDSTONE_STATE.getValue()).build();
        list.slider(SettingsManager.REDSTONE_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.REDSTONE_STATE.getValue()).build();
        
        list.category("fwa.config.category.repeater").build();
        list.button(SettingsManager.REPEATER_STATE).build();
        list.button(SettingsManager.REPEATER_EASING).isEnabled(() -> SettingsManager.REPEATER_STATE.getValue()).build();
        list.slider(SettingsManager.REPEATER_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.REPEATER_STATE.getValue()).build();
        
        list.category("fwa.config.category.end_portal_frame").build(); // Added missing .build() fix
        list.button(SettingsManager.END_PORTAL_FRAME_STATE).build();
        list.button(SettingsManager.END_PORTAL_FRAME_EASING).isEnabled(() -> SettingsManager.END_PORTAL_FRAME_STATE.getValue()).build();
        list.slider(SettingsManager.END_PORTAL_FRAME_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.END_PORTAL_FRAME_STATE.getValue()).build();
        list.button(SettingsManager.END_PORTAL_FRAME_INFINITE).isEnabled(() -> SettingsManager.END_PORTAL_FRAME_STATE.getValue()).build();
        
        list.category("fwa.config.category.bell").build();
        list.button(SettingsManager.BELL_STATE).build();
        list.slider(SettingsManager.BELL_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.BELL_STATE.getValue()).build();
        list.button(SettingsManager.BELL_INFINITE).isEnabled(() -> SettingsManager.BELL_STATE.getValue()).build();
        
        list.category("fwa.config.category.campfire").build();
        list.button(SettingsManager.CAMPFIRE_STATE).build();
        list.slider(SettingsManager.CAMPFIRE_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.CAMPFIRE_STATE.getValue()).build();
        
        list.category("fwa.config.category.vault").build();
        list.button(SettingsManager.VAULT_STATE).build();
        list.button(SettingsManager.VAULT_EASING).isEnabled(() -> SettingsManager.VAULT_STATE.getValue()).build();
        
        list.category("fwa.config.category.chest").build();
        list.button(SettingsManager.CHEST_EASING).build(); 
        
        list.category("fwa.config.category.shulkerbox").build();
        list.button(SettingsManager.SHULKERBOX_STATE).build();
        list.slider(SettingsManager.SHULKERBOX_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.SHULKERBOX_STATE.getValue()).build();
        
        list.category("fwa.config.category.lantern").build();
        list.button(SettingsManager.LANTERN_STATE).build();
        
        list.category("fwa.config.category.chain").build();
        list.button(SettingsManager.CHAIN_STATE).build();
        list.button(SettingsManager.CHAIN_GROUNDED).isEnabled(() -> SettingsManager.CHAIN_STATE.getValue()).build();
        list.button(SettingsManager.LANTERN_OVERRIDE).isEnabled(() -> SettingsManager.CHAIN_STATE.getValue() || SettingsManager.LANTERN_STATE.getValue()).build();
        list.button(SettingsManager.CHAIN_SWING_LIMIT).isEnabled(() -> SettingsManager.CHAIN_STATE.getValue() || SettingsManager.LANTERN_STATE.getValue()).build();
        
        list.category("fwa.config.category.dripleaf").build();
        list.button(SettingsManager.DRIPLEAF_STATE).build();
        list.button(SettingsManager.DRIPLEAF_EASING).isEnabled(() -> SettingsManager.DRIPLEAF_STATE.getValue()).build();
        list.slider(SettingsManager.DRIPLEAF_SPEED).range(0.5, 2.0).step(0.1).isEnabled(() -> SettingsManager.DRIPLEAF_STATE.getValue()).build();

        Button doneButton = Button.builder(Component.translatable("fwa.config.done"), b -> {
            this.minecraft.setScreen(this.parent);
            SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
        }).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build();

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        SettingsManager.saveSettings(SettingsManager.ALL_OPTIONS);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.list.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}