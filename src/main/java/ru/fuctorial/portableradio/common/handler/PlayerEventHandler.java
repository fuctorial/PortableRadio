package ru.fuctorial.portableradio.common.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import ru.fuctorial.portableradio.server.radio.RadioManager;


public class PlayerEventHandler {

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player != null) {
            RadioManager.INSTANCE.stopTransmitting(event.player);
        }
    }
}