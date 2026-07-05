package nosferatu.apothbag;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TalismanBagContainer extends SimpleContainer {
    private final ItemStack bag;
    private final boolean clientOnly;
    private boolean loading = false;
    private boolean refreshing = false;

    public TalismanBagContainer(ItemStack bag) {
        super(TalismanBagItem.MAX_SLOTS);
        this.bag = bag;
        this.clientOnly = bag.isEmpty();
        this.loading = true;
        if (!this.clientOnly) {
            TalismanBagItem.loadItems(bag, this);
            refreshVisualState();
        }
        this.loading = false;
    }

    public ItemStack getBagStack() {
        return this.bag;
    }

    public boolean isSlotUnlocked(int index) {
        if (this.clientOnly) return true;
        return index >= 0 && index < TalismanBagItem.getUnlockedSlots(this.bag);
    }

    public void setItemSilently(int index, ItemStack stack) {
        super.setItem(index, stack);
    }

    public void refreshVisualState() {
        if (this.clientOnly || this.refreshing) return;
        this.refreshing = true;
        try {
            int unlocked = TalismanBagItem.getUnlockedSlots(this.bag);
            for (int i = 0; i < TalismanBagItem.MAX_SLOTS; i++) {
                ItemStack current = super.getItem(i);
                if (i < unlocked) {
                    if (TalismanBagItem.isLockedVisual(current)) {
                        super.setItem(i, ItemStack.EMPTY);
                    }
                }
                else {
                    if (!TalismanBagItem.isLockedVisual(current)) {
                        super.setItem(i, TalismanBagItem.createLockedVisual());
                    }
                }
            }
        }
        finally {
            this.refreshing = false;
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index >= 0
                && index < TalismanBagItem.MAX_SLOTS
                && this.isSlotUnlocked(index)
                && TalismanBagItem.isPotionCharm(stack);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index < 0 || index >= TalismanBagItem.MAX_SLOTS) return;

        if (!this.loading && !this.clientOnly) {
            if (!this.isSlotUnlocked(index)) {
                if (!TalismanBagItem.isLockedVisual(super.getItem(index))) {
                    this.refreshing = true;
                    try {
                        super.setItem(index, TalismanBagItem.createLockedVisual());
                    }
                    finally {
                        this.refreshing = false;
                    }
                }
                return;
            }
            if (!stack.isEmpty() && !this.canPlaceItem(index, stack)) {
                return;
            }
        }

        if (TalismanBagItem.isPotionCharm(stack)) {
            TalismanBagItem.enableCharm(stack);
        }
        super.setItem(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!clientOnly && !loading && !refreshing) {
            TalismanBagItem.saveItems(this.bag, this);
        }
    }
}
