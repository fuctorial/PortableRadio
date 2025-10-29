package ru.fuctorial.portableradio;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.fuctorial.portableradio.common.command.RadioDebugCommand;
import ru.fuctorial.portableradio.common.handler.ItemTossHandler;
import ru.fuctorial.portableradio.common.handler.PlayerEventHandler;
import ru.fuctorial.portableradio.common.item.ModItems;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.proxy.CommonProxy;
import ru.fuctorial.portableradio.client.ClientConfig;

@Mod(modid = PortableRadio.MODID, name = PortableRadio.NAME, version = PortableRadio.VERSION)
public class PortableRadio {

    public static final String MODID = "portableradio";
    public static final String NAME = "PortableRadio";
    public static final String VERSION = "0.1.0-alpha";

    public static final Logger logger = LogManager.getLogger(MODID);

    public static boolean debugMode = false;

    @Mod.Instance(MODID)
    public static PortableRadio instance;

    @SidedProxy(clientSide = "ru.fuctorial.portableradio.client.proxy.ClientProxy",
            serverSide = "ru.fuctorial.portableradio.common.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        debugMode = event.getSuggestedConfigurationFile().exists();

        ModItems.init();
        PacketHandler.init();
        proxy.registerPackets();
        proxy.preInit(event);

        logger.info("PortableRadio PreInit complete");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new RadioDebugCommand());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        FMLCommonHandler.instance().bus().register(new PlayerEventHandler());
        MinecraftForge.EVENT_BUS.register(new ItemTossHandler());

        logger.info("PortableRadio Init complete");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        logger.info("PortableRadio PostInit complete");
    }

    public static void debug(String message) {
        if (debugMode) {
            logger.info("[DEBUG] " + message);
        }
    }
}