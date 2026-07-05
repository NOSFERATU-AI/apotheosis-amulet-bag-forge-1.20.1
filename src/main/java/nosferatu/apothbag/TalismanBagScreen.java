package nosferatu.apothbag;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TalismanBagScreen extends AbstractContainerScreen<TalismanBagMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE =
            new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int BAG_ROWS = 1;

    public TalismanBagScreen(TalismanBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + BAG_ROWS * 18;
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
        int topHeight = BAG_ROWS * 18 + 17;

        graphics.blit(CONTAINER_TEXTURE, x, y, 0, 0, this.imageWidth, topHeight);
        graphics.blit(CONTAINER_TEXTURE, x, y + topHeight, 0, 126, this.imageWidth, 96);
    }
}
