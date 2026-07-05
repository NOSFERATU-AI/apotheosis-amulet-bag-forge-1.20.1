package nosferatu.apothbag;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TalismanBagMenu extends AbstractContainerMenu {
    private static final int BAG_SLOTS = TalismanBagItem.MAX_SLOTS;
    private static final int PLAYER_INV_START = BAG_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final TalismanBagContainer container;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TalismanBagMenu(int id, Inventory playerInventory, TalismanBagContainer container) {
        super((MenuType) MenuType.f_39972_, id);
        this.container = container;

        for (int i = 0; i < BAG_SLOTS; i++) {
            this.m_38897_(new TalismanBagSlot(container, i, 44 + i * 18, 20));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.m_38897_(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.m_38897_(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean m_6875_(Player player) {
        return this.container.m_6542_(player);
    }

    @Override
    public ItemStack m_7648_(Player player, int index) {
        ItemStack result = ItemStack.f_41583_;
        if (index < 0 || index >= this.f_38839_.size()) return result;

        Slot slot = this.f_38839_.get(index);
        if (slot != null && slot.m_6657_()) {
            ItemStack stackInSlot = slot.m_7993_();
            result = stackInSlot.m_41777_();

            if (index < BAG_SLOTS) {
                if (!this.container.isSlotUnlocked(index)) {
                    return ItemStack.f_41583_;
                }
                if (!this.m_38903_(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.f_41583_;
                }
            }
            else {
                if (!TalismanBagItem.isPotionCharm(stackInSlot)) {
                    return ItemStack.f_41583_;
                }
                if (!this.m_38903_(stackInSlot, 0, BAG_SLOTS, false)) {
                    return ItemStack.f_41583_;
                }
            }

            if (stackInSlot.m_41619_()) {
                slot.m_5852_(ItemStack.f_41583_);
            }
            else {
                slot.m_6654_();
            }
        }
        return result;
    }

    @Override
    public void m_6877_(Player player) {
        super.m_6877_(player);
        this.container.m_6596_();
    }
}
