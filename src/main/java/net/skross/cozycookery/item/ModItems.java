package net.skross.cozycookery.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.item.custom.KnifeItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CozyCookery.MOD_ID);

    // Knife stats: 1 attack damage (+ the tier's bonus) and a fast attack speed of -2.0
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new KnifeItem(ModToolTiers.FLINT, knifeProperties(ModToolTiers.FLINT)));
    public static final DeferredItem<Item> IRON_KNIFE = ITEMS.register("iron_knife",
            () -> new KnifeItem(Tiers.IRON, knifeProperties(Tiers.IRON)));
    public static final DeferredItem<Item> GOLDEN_KNIFE = ITEMS.register("golden_knife",
            () -> new KnifeItem(Tiers.GOLD, knifeProperties(Tiers.GOLD)));
    public static final DeferredItem<Item> DIAMOND_KNIFE = ITEMS.register("diamond_knife",
            () -> new KnifeItem(Tiers.DIAMOND, knifeProperties(Tiers.DIAMOND)));
    public static final DeferredItem<Item> NETHERITE_KNIFE = ITEMS.register("netherite_knife",
            () -> new KnifeItem(Tiers.NETHERITE, knifeProperties(Tiers.NETHERITE).fireResistant()));

    private static Item.Properties knifeProperties(Tier tier) {
        return new Item.Properties().attributes(SwordItem.createAttributes(tier, 1, -2.0f));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
