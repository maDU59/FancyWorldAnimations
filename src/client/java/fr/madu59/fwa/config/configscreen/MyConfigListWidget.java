package fr.madu59.fwa.config.configscreen;

import java.util.List;
import fr.madu59.fwa.config.Option;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class MyConfigListWidget extends ContainerObjectSelectionList<MyConfigListWidget.Entry> {

    public MyConfigListWidget(Minecraft client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
    }

    @Override
	protected int scrollBarX() {
		return this.getX() + this.getWidth() - 6;
	}

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public void addCategory(String name) {
        this.addEntry(new CategoryEntry(name));
    }

    public void addButton(String name, Button.OnPress onPress) {
        this.addEntry(new ButtonEntry(Button.builder(Component.literal(name), onPress).bounds(0, 0, 100, 20).build(), null, ""));
    }

    public void addButton(Option<?> option) {
        this.addButton(option, "");
    }

    public void addButton(Option<?> option, String indent) {
        this.addEntry(new ButtonEntry(Button.builder(Component.translatable("fwa.config.value." + option.getValue().toString().toLowerCase()), btn -> {option.setToNextValue();}).bounds(0, 0, 100, 20).build(), option, indent));
    }

    public <N extends Number> void addSlider(Option<N> option, N min, N max, N step) {
        addSlider(option, min, max, step, "");
    }

    public <N extends Number> void addSlider(Option<N> option, N min, N max, N step, String indent) {

        double dMin = min.doubleValue();
        double dMax = max.doubleValue();
        double dCurrent = option.getValue().doubleValue();

        double initialPosition = (dMax <= dMin) ? 0 : (dCurrent - dMin) / (dMax - dMin);
        
        initialPosition = Math.max(0.0, Math.min(1.0, initialPosition));

        this.addEntry(new SliderEntry(new AbstractSliderButton(0, 0, 100, 20, 
        Component.literal(option.getValue().toString()), initialPosition){

            @Override
            protected void updateMessage() {

                String stepStr = step.toString();
                int decimalPlaces = 0;
                if (stepStr.contains(".")) {
                    decimalPlaces = stepStr.length() - stepStr.indexOf('.') - 1;
                }

                String format = "%." + decimalPlaces + "f";
                String formattedValue = String.format(java.util.Locale.ROOT, format, option.getValue());

                this.setMessage(Component.literal(formattedValue));
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void applyValue() {
                if (option.getValue() instanceof Integer) {

                    int imax = max.intValue();
                    int imin = min.intValue();
                    int istep = step.intValue();
                    int newValue = imin + Math.round((imax - imin) * (float)this.value / istep) * istep;
                    option.setValue((N)(Object) Math.round(newValue));

                } else if (option.getValue() instanceof Double) {
                    
                    double dmax = max.doubleValue();
                    double dmin = min.doubleValue();
                    double dstep = step.doubleValue();
                    double newValue = dmin + (double)Math.round((dmax - dmin) * this.value / dstep) * dstep;
                    option.setValue((N)(Object) newValue);

                } else if (option.getValue() instanceof Float) {

                    float fmax = max.floatValue();
                    float fmin = min.floatValue();
                    float fstep = step.floatValue();
                    float newValue = fmin + (float)Math.round((fmax - fmin) * (float)this.value / fstep) * fstep;
                    option.setValue((N)(Object) newValue);
                }
            }
        }, option, indent));
    }

    // Base entry
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<MyConfigListWidget.Entry> {}

    // Category header
    public static class CategoryEntry extends MyConfigListWidget.Entry {
        private final String name;

        public CategoryEntry(String name) {
            this.name = name;
        }

        @Override
       public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Font textRenderer = Minecraft.getInstance().font;
            int textX = x + entryWidth / 2;
            int textY = y + (entryHeight - textRenderer.lineHeight) / 2;
            context.drawCenteredString(textRenderer, Component.translatable(this.name).withStyle(ChatFormatting.UNDERLINE), textX, textY, 0xFFFFFFFF);
        }  

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }
    }

    // Button entry
    public static class ButtonEntry extends MyConfigListWidget.Entry{
        private final Button button;
        private final String name;
        private final String indent;
        private final Option<?> option;

        public ButtonEntry(Button button, Option<?> option, String indent) {
            this.button = button;
            this.name = option.getName();
            this.indent = indent;
            this.option = option;
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.button.setY(y + (entryHeight - this.button.getHeight()) / 2);
            this.button.setX(entryWidth - this.button.getWidth() - 10);
            this.button.render(context, mouseX, mouseY, tickDelta);

            if(this.name == null) return;

            Font textRenderer = Minecraft.getInstance().font;
            context.drawString(textRenderer, Component.literal(indent + this.name), 10, y + (entryHeight - textRenderer.lineHeight) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.button.mouseClicked(mouseX, mouseY, button)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if(this.option != null){
                    this.button.setMessage(Component.translatable("fwa.config.value." + this.option.getValue().toString().toLowerCase()));
                }
                return true;
            }
            return false;
        }
    }

    // Slider entry
    public static class SliderEntry extends MyConfigListWidget.Entry{
        private final AbstractSliderButton slider;
        private final String name;
        private final String indent;

        public SliderEntry(AbstractSliderButton slider, Option<?> option, String indent) {
            this.slider = slider;
            this.name = option.getName();
            this.indent = indent;
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.slider.setY(y + (entryHeight - this.slider.getHeight()) / 2);
            this.slider.setX(entryWidth - this.slider.getWidth() - 10);
            this.slider.render(context, mouseX, mouseY, tickDelta);

            if(this.name == null) return;

            Font textRenderer = Minecraft.getInstance().font;
            context.drawString(textRenderer, Component.literal(indent + this.name), 10, y + (entryHeight - textRenderer.lineHeight) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.slider);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.slider);
        }
    }
}
