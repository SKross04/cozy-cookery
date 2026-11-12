package net.skross.cozycookery;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.item.ModCreativeModeTabs;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CozyCookery.MOD_ID)
public class CozyCookery {
    public static final String MOD_ID = "cozycookery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CozyCookery(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModDataComponents.register(modEventBus);
    }
}
