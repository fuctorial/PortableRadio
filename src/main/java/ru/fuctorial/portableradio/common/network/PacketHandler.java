package ru.fuctorial.portableradio.common.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import ru.fuctorial.portableradio.PortableRadio;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PortableRadio.MODID);
    public static void init() {
    }
}