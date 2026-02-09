package fr.madu59.fwa.config.configscreen;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import fr.madu59.fwa.config.SettingsManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FancyWorldAnimationsConfigScreen extends Screen {
    
    private MyConfigListWidget list;
    private final Screen parent;

    protected FancyWorldAnimationsConfigScreen(Screen parent) {
        super(Component.literal("Fwa configuration screen"));
        this.parent = parent;
    }

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                literal("fwaConfig")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new FancyWorldAnimationsConfigScreen(null)));
                        return 1;
                    })
            );
        });
    }

    @Override
    protected void init() {
        super.init();
        // Create the scrolling list
        this.list = new MyConfigListWidget(this.minecraft, this.width, this.height - 80, 40, 26);

        // Example: Add categories + buttons
        list.addCategory("fwa.config.category.door");
        list.addButton(SettingsManager.DOOR_STATE);
        list.addButton(SettingsManager.DOOR_EASING);
        list.addSlider(SettingsManager.DOOR_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.trapdoor");
        list.addButton(SettingsManager.TRAPDOOR_STATE);
        list.addButton(SettingsManager.TRAPDOOR_EASING);
        list.addSlider(SettingsManager.TRAPDOOR_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.fencegate");
        list.addButton(SettingsManager.FENCEGATE_STATE);
        list.addButton(SettingsManager.FENCEGATE_EASING);
        list.addSlider(SettingsManager.FENCEGATE_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.lectern");
        list.addButton(SettingsManager.LECTERN_STATE);
        list.addButton(SettingsManager.LECTERN_EASING);
        list.addSlider(SettingsManager.LECTERN_SPEED, 0.5, 2.0, 0.1);
        list.addButton(SettingsManager.LECTERN_INFINITE);
        list.addCategory("fwa.config.category.chiseled_bookshelf");
        list.addButton(SettingsManager.CHISELED_BOOKSHELF_STATE);
        list.addButton(SettingsManager.CHISELED_BOOKSHELF_EASING);
        list.addSlider(SettingsManager.CHISELED_BOOKSHELF_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.jukebox");
        list.addButton(SettingsManager.JUKEBOX_STATE);
        list.addButton(SettingsManager.JUKEBOX_EASING);
        list.addSlider(SettingsManager.JUKEBOX_SPEED, 0.5, 2.0, 0.1);
        list.addButton(SettingsManager.JUKEBOX_INFINITE);
        list.addCategory("fwa.config.category.cauldron");
        list.addButton(SettingsManager.CAULDRON_STATE);
        list.addButton(SettingsManager.CAULDRON_EASING);
        list.addSlider(SettingsManager.CAULDRON_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.composter");
        list.addButton(SettingsManager.COMPOSTER_STATE);
        list.addButton(SettingsManager.COMPOSTER_EASING);
        list.addSlider(SettingsManager.COMPOSTER_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.lever");
        list.addButton(SettingsManager.LEVER_STATE);
        list.addButton(SettingsManager.LEVER_EASING);
        list.addSlider(SettingsManager.LEVER_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.button");
        list.addButton(SettingsManager.BUTTON_STATE);
        list.addButton(SettingsManager.BUTTON_EASING);
        list.addSlider(SettingsManager.BUTTON_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.repeater");
        list.addButton(SettingsManager.REPEATER_STATE);
        list.addButton(SettingsManager.REPEATER_EASING);
        list.addSlider(SettingsManager.REPEATER_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.end_portal_frame");
        list.addButton(SettingsManager.END_PORTAL_FRAME_STATE);
        list.addButton(SettingsManager.END_PORTAL_FRAME_EASING);
        list.addSlider(SettingsManager.END_PORTAL_FRAME_SPEED, 0.5, 2.0, 0.1);
        list.addButton(SettingsManager.END_PORTAL_FRAME_INFINITE);
        list.addCategory("fwa.config.category.bell");
        list.addButton(SettingsManager.BELL_STATE);
        list.addSlider(SettingsManager.BELL_SPEED, 0.5, 2.0, 0.1);
        list.addButton(SettingsManager.BELL_INFINITE);
        list.addCategory("fwa.config.category.campfire");
        list.addButton(SettingsManager.CAMPFIRE_STATE);
        list.addSlider(SettingsManager.CAMPFIRE_SPEED, 0.5, 2.0, 0.1);
        list.addCategory("fwa.config.category.chest");
        list.addButton(SettingsManager.CHEST_EASING);
        

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
