package ru.fuctorial.portableradio.common.handler;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.fuctorial.portableradio.client.gui.GuiWalkieTalkie;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;

public class GuiHandler implements IGuiHandler {

    public static final int WALKIE_TALKIE_GUI_ID = 0;
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == WALKIE_TALKIE_GUI_ID) {
            return new ContainerWalkieTalkie(player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == WALKIE_TALKIE_GUI_ID) {
            return new GuiWalkieTalkie(new ContainerWalkieTalkie(player));
        }
        return null;
    }
}