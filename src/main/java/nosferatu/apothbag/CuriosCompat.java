package nosferatu.apothbag;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class CuriosCompat {
    private static boolean initialized = false;
    private static boolean available = false;
    private static Method getCuriosInventoryMethod;

    private CuriosCompat() {
    }

    public static ItemStack findFirstActiveBag(Player player) {
        if (!init()) {
            return ItemStack.EMPTY;
        }

        try {
            Object lazyOptional = getCuriosInventoryMethod.invoke(null, player);
            if (lazyOptional == null) {
                return ItemStack.EMPTY;
            }

            Object handler = unwrapLazyOptional(lazyOptional);
            if (handler == null) {
                return ItemStack.EMPTY;
            }

            Object curios = handler.getClass().getMethod("getCurios").invoke(handler);
            if (!(curios instanceof Map<?, ?> curiosMap)) {
                return ItemStack.EMPTY;
            }

            for (Object stacksHandler : curiosMap.values()) {
                if (stacksHandler == null) continue;

                Object stacks = stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
                if (stacks == null) continue;

                int slots = ((Number) stacks.getClass().getMethod("getSlots").invoke(stacks)).intValue();
                Method getStackInSlot = stacks.getClass().getMethod("getStackInSlot", int.class);

                for (int i = 0; i < slots; i++) {
                    Object value = getStackInSlot.invoke(stacks, i);
                    if (value instanceof ItemStack stack
                            && stack.getItem() instanceof TalismanBagItem
                            && TalismanBagItem.hasStoredItemsFast(stack)) {
                        return stack;
                    }
                }
            }
        }
        catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    private static boolean init() {
        if (initialized) {
            return available;
        }

        initialized = true;
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            getCuriosInventoryMethod = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
            available = true;
        }
        catch (Throwable ignored) {
            available = false;
        }
        return available;
    }

    private static Object unwrapLazyOptional(Object lazyOptional) throws Exception {
        Object optional = lazyOptional.getClass().getMethod("resolve").invoke(lazyOptional);
        if (optional instanceof Optional<?> resolved) {
            return resolved.orElse(null);
        }
        return null;
    }
}
