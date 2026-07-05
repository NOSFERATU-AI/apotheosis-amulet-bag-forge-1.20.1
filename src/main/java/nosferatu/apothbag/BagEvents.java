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
        if (player.m_9236_().m_5776_() || !(player instanceof ServerPlayer)) return;

        Inventory inventory = player.m_150109_();
        for (ItemStack stack : inventory.items) {
            if (stack.m_41720_() instanceof TalismanBagItem && TalismanBagItem.hasStoredItemsFast(stack)) {
                tickBag(player, stack);
                return;
            }
        }

        ItemStack offhand = player.m_21206_();
        if (offhand.m_41720_() instanceof TalismanBagItem && TalismanBagItem.hasStoredItemsFast(offhand)) {
            tickBag(player, offhand);
        }
    }

    private static void tickBag(Player player, ItemStack bag) {
        TalismanBagContainer container = new TalismanBagContainer(bag);
        int unlocked = TalismanBagItem.getUnlockedSlots(bag);
        boolean dirty = false;

        for (int i = 0; i < Math.min(unlocked, TalismanBagItem.MAX_SLOTS); i++) {
            ItemStack charm = container.m_8020_(i);
            if (TalismanBagItem.isPotionCharm(charm)) {
                charm.m_41784_().m_128379_("charm_enabled", true);
                charm.m_41720_().m_6883_(charm, player.m_9236_(), player, -1, false);
                dirty = true;
            }
        }

        if (dirty) {
            container.m_6596_();
        }
    }
}
