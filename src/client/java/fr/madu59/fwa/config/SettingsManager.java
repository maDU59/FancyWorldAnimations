package fr.madu59.fwa.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.madu59.fwa.FancyWorldAnimations;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

import net.fabricmc.loader.api.FabricLoader;

public class SettingsManager {

    public static List<Option<?>> ALL_OPTIONS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fwa.json");
    private static Map<String, String> loadedSettings = loadSettings();

    public static Option<Boolean> MOD_TOGGLE = loadOptionWithDefaults(
        "mod_toggle",
        "fwa.config.option.mod_toggle.name",
        "fwa.config.option.toggle_name.description",
        true
    );

    public static Option<Boolean> DOOR_STATE = loadOptionWithDefaults(
        "door_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    );

    public static Option<Curves.Door> DOOR_EASING = loadOptionWithDefaults(
        "door_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static Option<Boolean> TRAPDOOR_STATE = loadOptionWithDefaults(
        "trapdoor_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> TRAPDOOR_EASING = loadOptionWithDefaults(
        "trapdoor_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static Option<Boolean> FENCEGATE_STATE = loadOptionWithDefaults(
        "fencegate_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> FENCEGATE_EASING = loadOptionWithDefaults(
        "fencegate_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static void saveSettings(List<Option<?>> options) {
        Map<String, String> map = toMap(options);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(map, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> toMap(List<Option<?>> options) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Option<?> option : options) {
            map.put(option.getId(), option.value.toString());
        }
        return map;
    }

    private static Map<String, String> loadSettings() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> map = GSON.fromJson(reader, type);
            return map;
        } catch (Exception e) {
            FancyWorldAnimations.LOGGER.info("[FWA] Config file not found or invalid, using default");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOptionValue(String key, T defaultValue) {
        if (loadedSettings == null || !loadedSettings.containsKey(key)) return null;
        else if (defaultValue instanceof Enum<?> e){
            return (T) Enum.valueOf(e.getDeclaringClass(), loadedSettings.get(key));
        }
        else if (defaultValue instanceof Float){
            return (T) Float.valueOf(loadedSettings.get(key));
        }
        else return null;
    }

    private static <T> Option<T> loadOptionWithDefaults(String id, String name, String description, T defaultValue) {
        T optionValue= getOptionValue(id, defaultValue);
        if (optionValue == null) optionValue = defaultValue;
        Option<T> option = new Option<T>(
                id,
                name,
                description,
                optionValue,
                defaultValue
        );
        return option;
    }
}