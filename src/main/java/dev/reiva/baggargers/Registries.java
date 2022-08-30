package dev.reiva.baggargers;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class Registries {
	private static final String MOD_ID = "baggargers";

	public static final Item BACK_PACK_ITEM = new BackPack();
	public static final ScreenHandlerType<BackPack.BackPackScreenHandler> BACK_PACK_SCREEN
		= new ExtendedScreenHandlerType<>(BackPack.BackPackScreenHandler::new);

	public static void registry() {
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "backpack"), BACK_PACK_ITEM);
		Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "backpack_gui"), BACK_PACK_SCREEN);
	}
}
