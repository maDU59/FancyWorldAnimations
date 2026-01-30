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
        list.addCategory("fwa.config.category.trapdoor");
        list.addButton(SettingsManager.TRAPDOOR_STATE);
        list.addButton(SettingsManager.TRAPDOOR_EASING);
        list.addCategory("fwa.config.category.fencegate");
        list.addButton(SettingsManager.FENCEGATE_STATE);
        list.addButton(SettingsManager.FENCEGATE_EASING);
        list.addCategory("fwa.config.category.lectern");
        list.addCategory("fwa.config.category.chiseled_bookshelf");
        list.addCategory("fwa.config.category.jukebox");
        list.addCategory("fwa.config.category.lever");
        list.addButton(SettingsManager.LEVER_STATE);
        list.addButton(SettingsManager.LEVER_EASING);
        list.addCategory("fwa.config.category.button");
        list.addCategory("fwa.config.category.end_portal_frame");
        list.addCategory("fwa.config.category.chest");
        

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
