package nosferatu.apothbag;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ApothTalismanBag.MODID)
public class ApothTalismanBag {
    public static final String MODID = "apoth_talisman_bag";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<Item> TALISMAN_BAG = ITEMS.register("talisman_bag",
            () -> new TalismanBagItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LOCKED_SLOT = ITEMS.register("locked_slot",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<MenuType<TalismanBagMenu>> TALISMAN_BAG_MENU = MENUS.register("talisman_bag",
            () -> IForgeMenuType.create((windowId, inventory, data) ->
                    new TalismanBagMenu(windowId, inventory, new TalismanBagContainer(ItemStack.EMPTY))));

    public ApothTalismanBag() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        MENUS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(BagEvents::onPlayerTick);
    }
}
