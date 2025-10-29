package ru.fuctorial.portableradio.common.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.common.handler.GuiHandler;
import ru.fuctorial.portableradio.common.network.*;

public class CommonProxy {
    public void registerPackets() {
        PacketHandler.INSTANCE.registerMessage(PacketGuiInteraction.Handler.class, PacketGuiInteraction.class, 0, Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketUpdateFrequency.Handler.class, PacketUpdateFrequency.class, 1, Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketPtt.Handler.class, PacketPtt.class, 2, Side.SERVER);

        PacketHandler.INSTANCE.registerMessage(PacketVoiceDataServerHandler.class, PacketVoiceData.class, 3, Side.SERVER);

        //PacketHandler.INSTANCE.registerMessage(PacketUpdateAudioDevice.Handler.class, PacketUpdateAudioDevice.class, 4, Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketUpdateVolume.Handler.class, PacketUpdateVolume.class, 5, Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketTogglePower.Handler.class, PacketTogglePower.class, 6, Side.SERVER);
    }

    public void preInit(FMLPreInitializationEvent event) { }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(PortableRadio.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) { }
}