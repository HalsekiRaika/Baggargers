package dev.reiva.baggargers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final class BackPackClient extends HandledScreen<BackPack.BackPackScreenHandler> {
	static final Identifier GUI_TEX = new Identifier("baggargers", "textures/gui/container/backpack_gui.png");

	BackPackClient(BackPack.BackPackScreenHandler handler, PlayerInventory playerInventory, Text title) {
		super(handler, playerInventory, LiteralText.EMPTY);
		this.backgroundWidth = 256;
		this.backgroundHeight = 256;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, GUI_TEX);
		int edgeSpacingX = (this.width - this.backgroundWidth) / 2;
		int edgeSpacingY = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrices, edgeSpacingX, edgeSpacingY, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) { /* No-op ;D */}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
}
