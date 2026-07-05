package nosferatu.apothbag;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TalismanBagScreen extends AbstractContainerScreen<TalismanBagMenu> {
    public TalismanBagScreen(TalismanBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 3, y + 3, x + this.imageWidth - 3, y + this.imageHeight - 3, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 6, x + this.imageWidth - 6, y + this.imageHeight - 6, 0xFFC6C6C6);

        for (int i = 0; i < TalismanBagItem.MAX_SLOTS; i++) {
            drawSlot(graphics, x + 43 + i * 18, y + 19);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, x + 7 + col * 18, y + 50 + row * 18);
            }
        }

        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, x + 7 + col * 18, y + 108);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF555555);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFE0E0E0);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF8B8B8B);
    }
}
