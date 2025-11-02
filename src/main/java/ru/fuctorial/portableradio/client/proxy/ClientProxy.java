package ru.fuctorial.portableradio.client.proxy;

import club.minnced.opus.util.OpusLibrary;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.client.handler.HudOverlayHandler;
import ru.fuctorial.portableradio.client.handler.KeyInputHandler;
import ru.fuctorial.portableradio.client.handler.KeybindHandler;
import ru.fuctorial.portableradio.common.item.ModItems;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketVoiceData;
import ru.fuctorial.portableradio.common.network.PacketVoiceDataClientHandler;
import ru.fuctorial.portableradio.common.proxy.CommonProxy;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerPackets() {
        super.registerPackets();
        PacketHandler.INSTANCE.registerMessage(PacketVoiceDataClientHandler.class, PacketVoiceData.class, 3, Side.CLIENT);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        KeybindHandler.init();
        ModItems.registerRenders();

        ClientConfig.init(event.getSuggestedConfigurationFile());
        try {
            OpusLibrary.loadFromJar();
            PortableRadio.logger.info("Successfully loaded Opus native library.");
        } catch (Throwable e) {
            PortableRadio.logger.fatal("FATAL: FAILED to load Opus native library! Voice chat will NOT work.", e);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        KeyInputHandler keyHandler = new KeyInputHandler();
        FMLCommonHandler.instance().bus().register(keyHandler);

        MinecraftForge.EVENT_BUS.register(new HudOverlayHandler());
    }
}