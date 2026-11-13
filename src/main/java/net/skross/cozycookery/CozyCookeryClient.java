package net.skross.cozycookery;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.skross.cozycookery.menu.ModMenuTypes;
import net.skross.cozycookery.screen.ChoppingBoardScreen;
import net.skross.cozycookery.screen.CookingPotScreen;
import net.skross.cozycookery.screen.FryingPanScreen;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.block.entity.renderer.ChoppingBoardBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.CookingPotBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.FryingPanBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.MixingBowlBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.MortarBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.OvenBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.StoveBlockEntityRenderer;
import net.skross.cozycookery.block.entity.renderer.ToolRackBlockEntityRenderer;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.component.SoupContents;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CozyCookery.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CozyCookery.MOD_ID, value = Dist.CLIENT)
public class CozyCookeryClient {
    public CozyCookeryClient(ModContainer container) {
    }

    // Which screen opens for which menu
    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COOKING_POT_MENU.get(), CookingPotScreen::new);
        event.register(ModMenuTypes.FRYING_PAN_MENU.get(), FryingPanScreen::new);
        event.register(ModMenuTypes.CHOPPING_BOARD_MENU.get(), ChoppingBoardScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // "cozycookery:filled" = 1 when a mixing bowl item carries soup -> its item model shows the soup
        event.enqueueWork(() -> ItemProperties.register(ModBlocks.MIXING_BOWL.get().asItem(),
                ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "filled"),
                (stack, level, entity, seed) -> stack.has(ModDataComponents.SOUP_CONTENTS.get()) ? 1.0f : 0.0f));
    }

    // Tells Minecraft which renderer draws the item lying on the chopping board
    @SubscribeEvent
    static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CHOPPING_BOARD_BE.get(), ChoppingBoardBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STOVE_BE.get(), StoveBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COOKING_POT_BE.get(), CookingPotBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRYING_PAN_BE.get(), FryingPanBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MORTAR_BE.get(), MortarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_BOWL_BE.get(), MixingBowlBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OVEN_BE.get(), OvenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TOOL_RACK_BE.get(), ToolRackBlockEntityRenderer::new);
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

        // Soup surface in a placed mixing bowl
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl && bowl.hasSoup()) {
                return bowl.getSoupColor();
            }
            return 0xFFFFFF;
        }, ModBlocks.MIXING_BOWL.get());
    }

    // Soup surface on a mixing bowl ITEM (in your hand / inventory). Item colours need an alpha value (0xFF...)
    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            SoupContents contents = stack.get(ModDataComponents.SOUP_CONTENTS.get());
            return contents != null ? 0xFF000000 | contents.color() : 0xFFFFFFFF;
        }, ModBlocks.MIXING_BOWL.get().asItem());
    }
}

