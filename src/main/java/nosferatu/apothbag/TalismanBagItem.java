package nosferatu.apothbag;

import dev.shadowsoffire.apotheosis.potion.PotionCharmItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class TalismanBagItem extends Item {
    public static final int MAX_SLOTS = 5;
    private static final String TAG_UNLOCKED = "UnlockedSlots";
    private static final String TAG_ITEMS = "Items";

    public TalismanBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);

        if (!level.m_5776_() && player instanceof ServerPlayer serverPlayer) {
            if (player.m_6144_()) {
                unlockNextSlot(stack, serverPlayer);
            }
            else {
                openBag(serverPlayer, stack);
            }
        }

        return InteractionResultHolder.m_19092_(stack, level.m_5776_());
    }

    private static void openBag(ServerPlayer player, ItemStack bagStack) {
        Component title = Component.m_237115_("container.apoth_talisman_bag.talisman_bag");
        MenuProvider provider = new SimpleMenuProvider((int id, Inventory inventory, Player menuPlayer) -> {
            TalismanBagContainer container = new TalismanBagContainer(bagStack);
            return new TalismanBagMenu(id, inventory, container);
        }, title);
        NetworkHooks.openScreen(player, provider, buf -> { });
    }

    public static boolean unlockNextSlot(ItemStack stack, ServerPlayer player) {
        int unlocked = getUnlockedSlots(stack);
        if (unlocked >= MAX_SLOTS) {
            player.m_5661_(Component.m_237115_("message.apoth_talisman_bag.all_slots_unlocked"), true);
            return false;
        }

        int cost = (unlocked + 1) * 100;
        if (!player.m_7500_() && player.f_36078_ < cost) {
            player.m_5661_(Component.m_237110_("message.apoth_talisman_bag.not_enough_levels", cost), true);
            return false;
        }

        if (!player.m_7500_()) {
            player.m_6749_(-cost);
        }

        setUnlockedSlots(stack, unlocked + 1);
        player.m_5661_(Component.m_237110_("message.apoth_talisman_bag.slot_unlocked", unlocked + 1, cost), true);
        return true;
    }

    public static int getUnlockedSlots(ItemStack stack) {
        CompoundTag tag = stack.m_41784_();
        int slots = tag.m_128451_(TAG_UNLOCKED);
        if (slots < 0) return 0;
        return Math.min(slots, MAX_SLOTS);
    }

    public static void setUnlockedSlots(ItemStack stack, int slots) {
        stack.m_41784_().m_128405_(TAG_UNLOCKED, Math.max(0, Math.min(slots, MAX_SLOTS)));
    }

    public static boolean hasStoredItemsFast(ItemStack stack) {
        if (stack.m_41619_()) return false;
        CompoundTag tag = stack.m_41784_();
        int unlocked = tag.m_128451_(TAG_UNLOCKED);
        if (unlocked <= 0) return false;
        return tag.m_128437_(TAG_ITEMS, 10).size() > 0;
    }

    public static boolean isPotionCharm(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41720_() instanceof PotionCharmItem && PotionCharmItem.hasEffect(stack);
    }

    public static ItemStack createLockedVisual() {
        return new ItemStack((ItemLike) ApothTalismanBag.LOCKED_SLOT.get());
    }

    public static boolean isLockedVisual(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41720_() == ApothTalismanBag.LOCKED_SLOT.get();
    }

    public static void loadItems(ItemStack bag, TalismanBagContainer container) {
        CompoundTag tag = bag.m_41784_();
        ListTag list = tag.m_128437_(TAG_ITEMS, 10);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.m_128728_(i);
            int slot = itemTag.m_128445_("Slot") & 255;
            if (slot >= 0 && slot < MAX_SLOTS) {
                container.setItemSilently(slot, ItemStack.m_41712_(itemTag));
            }
        }
    }

    public static void saveItems(ItemStack bag, TalismanBagContainer container) {
        ListTag list = new ListTag();
        int unlocked = getUnlockedSlots(bag);
        for (int slot = 0; slot < Math.min(unlocked, MAX_SLOTS); slot++) {
            ItemStack stack = container.m_8020_(slot);
            if (isPotionCharm(stack)) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.m_128344_("Slot", (byte) slot);
                stack.m_41739_(itemTag);
                list.m_7614_(list.size(), itemTag);
            }
        }
        bag.m_41784_().m_128365_(TAG_ITEMS, list);
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int unlocked = getUnlockedSlots(stack);
        tooltip.add(Component.m_237110_("tooltip.apoth_talisman_bag.unlocked", unlocked, MAX_SLOTS));
        if (unlocked < MAX_SLOTS) {
            int cost = (unlocked + 1) * 100;
            tooltip.add(Component.m_237110_("tooltip.apoth_talisman_bag.next_cost", cost));
        }
        tooltip.add(Component.m_237115_("tooltip.apoth_talisman_bag.open"));
        tooltip.add(Component.m_237115_("tooltip.apoth_talisman_bag.unlock"));
    }
}
