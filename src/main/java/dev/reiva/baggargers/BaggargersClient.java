package dev.reiva.baggargers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@Environment(EnvType.CLIENT)
public final class BaggargersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		HandledScreens.register(Registries.BACK_PACK_SCREEN, BackPackClient::new);
	}
}
