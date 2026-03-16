package fr.madu59.fwa;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import fr.madu59.fwa.config.configscreen.FancyWorldAnimationsConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FancyWorldAnimations.MOD_ID)
public class FancyWorldAnimations {
	public static final String MOD_ID = "fwa";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogUtils.getLogger();

	public FancyWorldAnimations(FMLJavaModLoadingContext context) {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		IEventBus modEventBus = context.getModEventBus();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // This manually calls your client constructor!
            new FancyWorldAnimationsClient(modEventBus);
        });

		LOGGER.info("[FWA] Fancy World Animations initialized!");
	}
}