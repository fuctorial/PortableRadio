package ru.fuctorial.portableradio.client.handler;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeybindHandler {

    public static KeyBinding pttKey;

    public static void init() {

        pttKey = new KeyBinding("key.portableradio.p_t_t", Keyboard.KEY_V, "key.categories.portableradio");

        ClientRegistry.registerKeyBinding(pttKey);
    }
}