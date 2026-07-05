package nosferatu.apothbag;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

import java.lang.reflect.Method;
import java.util.List;

public class TalismanBagItem extends Item {
    public static final int MAX_SLOTS = 5;
    private static final String TAG_UNLOCKED = "UnlockedSlots";
    private static final String TAG_ITEMS = "Items";

    private static Class<?> potionCharmItemClass;
    private static Method potionCharmHasEffectMethod;
    private static boolean apotheosisReflectionTried = false;

    public TalismanBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                unlockNextSlot(stack, serverPlayer);
            }
            else {
                openBag(serverPlayer, stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void openBag(ServerPlayer player, ItemStack bagStack) {
        Component title = Component.translatable("container.apoth_talisman_bag.talisman_bag");
        MenuProvider provider = new SimpleMenuProvider((int id, Inventory inventory, Player menuPlayer) -> {
            TalismanBagContainer container = new TalismanBagContainer(bagStack);
            return new TalismanBagMenu(id, inventory, container);
        }, title);
        NetworkHooks.openScreen(player, provider, buf -> { });
    }

    public static boolean unlockNextSlot(ItemStack stack, ServerPlayer player) {
        int unlocked = getUnlockedSlots(stack);
        if (unlocked >= MAX_SLOTS) {
            player.displayClientMessage(Component.translatable("message.apoth_talisman_bag.all_slots_unlocked"), true);
            return false;
        }

        int cost = (unlocked + 1) * 100;
        if (!player.getAbilities().instabuild && player.experienceLevel < cost) {
            player.displayClientMessage(Component.translatable("message.apoth_talisman_bag.not_enough_levels", cost), true);
            return false;
        }

        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-cost);
        }

        setUnlockedSlots(stack, unlocked + 1);
        player.displayClientMessage(Component.translatable("message.apoth_talisman_bag.slot_unlocked", unlocked + 1, cost), true);
        return true;
    }

    public static int getUnlockedSlots(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int slots = tag.getInt(TAG_UNLOCKED);
        if (slots < 0) return 0;
        return Math.min(slots, MAX_SLOTS);
    }

    public static void setUnlockedSlots(ItemStack stack, int slots) {
        stack.getOrCreateTag().putInt(TAG_UNLOCKED, Math.max(0, Math.min(slots, MAX_SLOTS)));
    }

    public static boolean hasStoredItemsFast(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getOrCreateTag();
        int unlocked = tag.getInt(TAG_UNLOCKED);
        if (unlocked <= 0) return false;
        if (!tag.contains(TAG_ITEMS, Tag.TAG_LIST)) return false;
        return tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND).size() > 0;
    }

    private static void initApotheosisReflection() {
        if (apotheosisReflectionTried) return;
        apotheosisReflectionTried = true;

        String[] classNames = {
                "dev.shadowsoffire.apotheosis.potion.PotionCharmItem",
                "dev.shadowsoffire.apotheosis.item.PotionCharmItem"
        };

        for (String className : classNames) {
            try {
                potionCharmItemClass = Class.forName(className);
                potionCharmHasEffectMethod = potionCharmItemClass.getMethod("hasEffect", ItemStack.class);
                return;
            }
            catch (Throwable ignored) {
                potionCharmItemClass = null;
                potionCharmHasEffectMethod = null;
            }
        }
    }

    public static boolean isPotionCharm(ItemStack stack) {
        if (stack.isEmpty()) return false;
        initApotheosisReflection();
        if (potionCharmItemClass == null || potionCharmHasEffectMethod == null) return false;
        if (!potionCharmItemClass.isInstance(stack.getItem())) return false;
        try {
            Object result = potionCharmHasEffectMethod.invoke(null, stack);
            return Boolean.TRUE.equals(result);
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static void enableCharm(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putBoolean("charm_enabled", true);
        }
    }

    public static ItemStack createLockedVisual() {
        return new ItemStack((ItemLike) ApothTalismanBag.LOCKED_SLOT.get());
    }

    public static boolean isLockedVisual(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ApothTalismanBag.LOCKED_SLOT.get();
    }

    public static void loadItems(ItemStack bag, TalismanBagContainer container) {
        if (bag.isEmpty()) return;
        CompoundTag tag = bag.getOrCreateTag();
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < MAX_SLOTS) {
                container.setItemSilently(slot, ItemStack.of(itemTag));
            }
        }
    }

    public static void saveItems(ItemStack bag, TalismanBagContainer container) {
        if (bag.isEmpty()) return;
        ListTag list = new ListTag();
        int unlocked = getUnlockedSlots(bag);
        for (int slot = 0; slot < Math.min(unlocked, MAX_SLOTS); slot++) {
            ItemStack stack = container.getItem(slot);
            if (isPotionCharm(stack)) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) slot);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }
        bag.getOrCreateTag().put(TAG_ITEMS, list);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int unlocked = getUnlockedSlots(stack);
        tooltip.add(Component.translatable("tooltip.apoth_talisman_bag.unlocked", unlocked, MAX_SLOTS));
        if (unlocked < MAX_SLOTS) {
            int cost = (unlocked + 1) * 100;
            tooltip.add(Component.translatable("tooltip.apoth_talisman_bag.next_cost", cost));
        }
        tooltip.add(Component.translatable("tooltip.apoth_talisman_bag.open"));
        tooltip.add(Component.translatable("tooltip.apoth_talisman_bag.unlock"));
    }
}
