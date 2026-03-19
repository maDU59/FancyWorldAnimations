package fr.madu59.fwa.compat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;

public class Blacklist {
    private static final Set<ResourceLocation> BLOCKS_BLACKLIST = new HashSet<>();
    private static final Set<String> MODS_BLACKLIST = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("fwa-blacklist.json");

    public static void load() {
        BLOCKS_BLACKLIST.clear();
        MODS_BLACKLIST.clear();
        loadFromResource();
        loadFromConfig();
    }

    private static void loadFromResource() {
        ResourceLocation resourcePath = ResourceLocation.fromNamespaceAndPath("fwa", "blacklist.json");
        Minecraft.getInstance().getResourceManager().getResource(resourcePath).ifPresent(resource -> {
            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                BlacklistData data = GSON.fromJson(reader, BlacklistData.class);
                applyData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadFromConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String content = Files.readString(CONFIG_PATH);
                BlacklistData data = GSON.fromJson(content, BlacklistData.class);
                applyData(data);
            }
        } catch (Exception e) {
            System.err.println("[FWA] Failed to load blacklist: " + e.getMessage());
        }
    }

    private static void applyData(BlacklistData data){
        for(String mod : data.mods){
            MODS_BLACKLIST.add(mod);
        }
        for(String block : data.blocks){
            ResourceLocation id = ResourceLocation.tryParse(block);
            if(id!=null) BLOCKS_BLACKLIST.add(id);
        }
    }

    public static boolean isBlacklisted(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String mod = id.getNamespace();
        return BLOCKS_BLACKLIST.contains(id) || MODS_BLACKLIST.contains(mod);
    }
}

