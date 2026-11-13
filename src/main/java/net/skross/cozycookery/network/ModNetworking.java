package net.skross.cozycookery.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.skross.cozycookery.CozyCookery;

// Registers our network messages (mod event bus)
@EventBusSubscriber(modid = CozyCookery.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(StirPayload.TYPE, StirPayload.STREAM_CODEC, StirPayload::handle);
        registrar.playToServer(FlipPayload.TYPE, FlipPayload.STREAM_CODEC, FlipPayload::handle);
        registrar.playToServer(ChopPayload.TYPE, ChopPayload.STREAM_CODEC, ChopPayload::handle);
    }
}
