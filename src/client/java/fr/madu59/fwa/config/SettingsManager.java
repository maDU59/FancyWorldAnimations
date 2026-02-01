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

    public static Option<Double> DOOR_SPEED = loadOptionWithDefaults(
        "door_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
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

    public static Option<Double> TRAPDOOR_SPEED = loadOptionWithDefaults(
        "trapdoor_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
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
        Curves.Door.SPRINGY
    );

    public static Option<Double> FENCEGATE_SPEED = loadOptionWithDefaults(
        "fencegate_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> LEVER_STATE = loadOptionWithDefaults(
        "lever_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> LEVER_EASING = loadOptionWithDefaults(
        "lever_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.LINEAR
    );

    public static Option<Double> LEVER_SPEED = loadOptionWithDefaults(
        "lever_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> LECTERN_STATE = loadOptionWithDefaults(
        "lectern_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> LECTERN_EASING = loadOptionWithDefaults(
        "lectern_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static Option<Double> LECTERN_SPEED = loadOptionWithDefaults(
        "lectern_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> LECTERN_INFINITE = loadOptionWithDefaults(
        "lectern_infinite",
        "fwa.config.option.infinite.name",
        "fwa.config.option.infinite.description",
        true
    );

    public static Option<Boolean> JUKEBOX_STATE = loadOptionWithDefaults(
        "jukebox_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> JUKEBOX_EASING = loadOptionWithDefaults(
        "jukebox_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static Option<Double> JUKEBOX_SPEED = loadOptionWithDefaults(
        "jukebox_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> JUKEBOX_INFINITE = loadOptionWithDefaults(
        "lectern_infinite",
        "fwa.config.option.infinite.name",
        "fwa.config.option.infinite.description",
        true
    );

    public static Option<Boolean> BELL_STATE = loadOptionWithDefaults(
        "bell_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    );

    public static Option<Double> BELL_SPEED = loadOptionWithDefaults(
        "bell_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> BELL_INFINITE = loadOptionWithDefaults(
        "bell_infinite",
        "fwa.config.option.infinite.name",
        "fwa.config.option.infinite.description",
        true
    );

    public static Option<Boolean> CHISELED_BOOKSHELF_STATE = loadOptionWithDefaults(
        "chiseled_bookshelf_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> CHISELED_BOOKSHELF_EASING = loadOptionWithDefaults(
        "chiseled_bookshelf_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.LINEAR
    );

    public static Option<Double> CHISELED_BOOKSHELF_SPEED = loadOptionWithDefaults(
        "chiseled_bookshelf_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> CAULDRON_STATE = loadOptionWithDefaults(
        "cauldron_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> CAULDRON_EASING = loadOptionWithDefaults(
        "cauldron_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.SPRINGY
    );

    public static Option<Double> CAULDRON_SPEED = loadOptionWithDefaults(
        "cauldron_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> BUTTON_STATE = loadOptionWithDefaults(
        "button_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> BUTTON_EASING = loadOptionWithDefaults(
        "button_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.LINEAR
    );

    public static Option<Double> BUTTON_SPEED = loadOptionWithDefaults(
        "button_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> REPEATER_STATE = loadOptionWithDefaults(
        "repeater_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> REPEATER_EASING = loadOptionWithDefaults(
        "repeater_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.SPRINGY
    );

    public static Option<Double> REPEATER_SPEED = loadOptionWithDefaults(
        "repeater_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Boolean> END_PORTAL_FRAME_STATE = loadOptionWithDefaults(
        "end_portal_frame_state",
        "fwa.config.option.state.name",
        "fwa.config.option.state.description",
        true
    ); 

    public static Option<Curves.Door> END_PORTAL_FRAME_EASING = loadOptionWithDefaults(
        "end_portal_frame_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.DEFAULT
    );

    public static Option<Double> END_PORTAL_FRAME_SPEED = loadOptionWithDefaults(
        "end_portal_frame_speed",
        "fwa.config.option.speed.name",
        "fwa.config.option.speed.description",
        1.0
    );

    public static Option<Curves.Door> CHEST_EASING = loadOptionWithDefaults(
        "chest_easing",
        "fwa.config.option.easing.name",
        "fwa.config.option.easing.description",
        Curves.Door.SPRINGY
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
            if (option.getValue() != option.getDefaultValue()) map.put(option.getId(), option.getValue().toString());
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