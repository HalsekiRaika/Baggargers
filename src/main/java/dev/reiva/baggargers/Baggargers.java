package dev.reiva.baggargers;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Baggargers implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Baggargers");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
		Registries.registry();
	}
}
