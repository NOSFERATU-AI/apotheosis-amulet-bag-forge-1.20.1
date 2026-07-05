package nosferatu.apothbag;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class TalismanBagContainer extends SimpleContainer {
    private final ItemStack bag;
    private boolean loading = false;
    private boolean refreshing = false;

    public TalismanBagContainer(ItemStack bag) {
        super(TalismanBagItem.MAX_SLOTS);
        this.bag = bag;
        this.loading = true;
        TalismanBagItem.loadItems(bag, this);
        refreshVisualState();
        this.loading = false;
    }

    public ItemStack getBagStack() {
        return this.bag;
    }

    public boolean isSlotUnlocked(int index) {
        return index >= 0 && index < TalismanBagItem.getUnlockedSlots(this.bag);
    }

    public void setItemSilently(int index, ItemStack stack) {
        super.m_6836_(index, stack);
    }

    public void refreshVisualState() {
        if (this.refreshing) return;
        this.refreshing = true;
        try {
            int unlocked = TalismanBagItem.getUnlockedSlots(this.bag);
            for (int i = 0; i < TalismanBagItem.MAX_SLOTS; i++) {
                ItemStack current = super.m_8020_(i);
                if (i < unlocked) {
                    if (TalismanBagItem.isLockedVisual(current)) {
                        super.m_6836_(i, ItemStack.f_41583_);
                    }
                }
                else {
                    if (!TalismanBagItem.isLockedVisual(current)) {
                        super.m_6836_(i, TalismanBagItem.createLockedVisual());
                    }
                }
            }
        }
        finally {
            this.refreshing = false;
        }
    }

    @Override
    public boolean m_7013_(int index, ItemStack stack) {
        return index >= 0
                && index < TalismanBagItem.MAX_SLOTS
                && this.isSlotUnlocked(index)
                && TalismanBagItem.isPotionCharm(stack);
    }

    @Override
    public void m_6836_(int index, ItemStack stack) {
        if (index < 0 || index >= TalismanBagItem.MAX_SLOTS) return;

        if (!this.loading) {
            if (!this.isSlotUnlocked(index)) {
                if (!TalismanBagItem.isLockedVisual(super.m_8020_(index))) {
                    this.refreshing = true;
                    try {
                        super.m_6836_(index, TalismanBagItem.createLockedVisual());
                    }
                    finally {
                        this.refreshing = false;
                    }
                }
                return;
            }
            if (!stack.m_41619_() && !this.m_7013_(index, stack)) {
                return;
            }
        }

        if (TalismanBagItem.isPotionCharm(stack)) {
            stack.m_41784_().m_128379_("charm_enabled", true);
        }
        super.m_6836_(index, stack);
    }

    @Override
    public void m_6596_() {
        super.m_6596_();
        if (!loading && !refreshing) {
            TalismanBagItem.saveItems(this.bag, this);
        }
    }
}
