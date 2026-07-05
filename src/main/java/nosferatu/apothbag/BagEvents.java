package nosferatu.apothbag;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

public class BagEvents {
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide || !(player instanceof ServerPlayer)) return;

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (stack.getItem() instanceof TalismanBagItem && TalismanBagItem.hasStoredItemsFast(stack)) {
                tickBag(player, stack);
                return;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof TalismanBagItem && TalismanBagItem.hasStoredItemsFast(offhand)) {
            tickBag(player, offhand);
        }
    }

    private static void tickBag(Player player, ItemStack bag) {
        TalismanBagContainer container = new TalismanBagContainer(bag);
        int unlocked = TalismanBagItem.getUnlockedSlots(bag);
        boolean dirty = false;

        for (int i = 0; i < Math.min(unlocked, TalismanBagItem.MAX_SLOTS); i++) {
            ItemStack charm = container.getItem(i);
            if (TalismanBagItem.isPotionCharm(charm)) {
                TalismanBagItem.enableCharm(charm);
                charm.getItem().inventoryTick(charm, player.level(), player, i, false);
                dirty = true;
            }
        }

        if (dirty) {
            container.setChanged();
        }
    }
}
