package fr.madu59.fwa.config;

import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public class Option<T> {
    private String id;
    private String name;
    private String description;
    private T value;
    private T defaultValue;
    private boolean reload;
    private BooleanSupplier enabledSupplier;
    private T disabledValue;

    public Option(String id, String name, String description, T value, T defaultValue, boolean reload) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
        this.defaultValue = defaultValue;
        this.reload = reload;
        SettingsManager.ALL_OPTIONS.add(this);
    }

    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        T value = this.value;
        if(enabledSupplier != null && !enabledSupplier.getAsBoolean()){
            if(disabledValue != null) return disabledValue;
            if(value instanceof Boolean){
                value = (T) Boolean.FALSE;
            }
        }
        return value;
    }

    public T getTrueValue() {
        return this.value;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return I18n.get(this.name);
    }

    public String getDescription() {
        return I18n.get(this.description);
    }

    public void setToNextValue() {
        if(reload) Minecraft.getInstance().levelRenderer.allChanged();
        this.value = cycle(this.value);
    }

    public void setValue(T value){
        this.value = value;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public Option<T> isEnabled(BooleanSupplier enabledSupplier){
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    public boolean isEnabled(){
        if(this.enabledSupplier == null) return true;
        else return enabledSupplier.getAsBoolean();
    }

    public Option<T> disabledValue(T disabledValue){
        this.disabledValue = disabledValue;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T cycle(T value) {
        if (value instanceof Enum<?> enumValue) {
            Enum<?>[] constants = enumValue.getDeclaringClass().getEnumConstants();
            int nextOrdinal = (enumValue.ordinal() + 1) % constants.length;
            return (T) constants[nextOrdinal];
        }
        if(value instanceof Boolean boolValue) {
            return (T) Boolean.valueOf(!boolValue);
        }
        else return null;
    }
}
