package net.skross.cozycookery;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.block.entity.renderer.ChoppingBoardBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.CookingPotBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.FryingPanBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.MortarBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.StoveBlockEntityRenderer;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CozyCookery.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CozyCookery.MOD_ID, value = Dist.CLIENT)
public class CozyCookeryClient {
    public CozyCookeryClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CozyCookery.LOGGER.info("HELLO FROM CLIENT SETUP");
        CozyCookery.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    // Tells Minecraft which renderer draws the item lying on the chopping board
    @SubscribeEvent
    static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CHOPPING_BOARD_BE.get(), ChoppingBoardBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STOVE_BE.get(), StoveBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COOKING_POT_BE.get(), CookingPotBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRYING_PAN_BE.get(), FryingPanBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MORTAR_BE.get(), MortarBlockEntityRenderer::new);
    }

    // Colours the water/soup surface in the cooking pot ("tintindex": 0 in the pot models)
    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
                return pot.getLiquidColor();
            }
            return CookingPotBlockEntity.WATER_COLOR;
        }, ModBlocks.COOKING_POT.get());
    }
}
