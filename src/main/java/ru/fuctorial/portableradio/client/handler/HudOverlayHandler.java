package ru.fuctorial.portableradio.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

@SideOnly(Side.CLIENT)
public class HudOverlayHandler extends Gui {

    private enum RadioState { HIDDEN, OFF, ON }
    private final Minecraft mc;

    public HudOverlayHandler() {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        RadioState state = findRadioState(player);
        if (state == RadioState.HIDDEN) return;

        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int x = res.getScaledWidth() - 18;
        int y = res.getScaledHeight() - 18;
        int width = 16;
        int height = 16;

        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int borderColor = 0xFF000000;
        drawRect(x, y, x + width, y + height, borderColor);


        int color;
        if (state == RadioState.ON) {

            color = 0xFF00B300;
        } else {

            color = 0xFFB30000;
        }


        int inset = 2;
        drawRect(x + inset, y + inset, x + width - inset, y + height - inset, color);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();

    }

    private RadioState findRadioState(EntityPlayer player) {
        boolean hasAnyRadio = false;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                hasAnyRadio = true;
                if (((ItemWalkieTalkie) stack.getItem()).isRadioOn(stack)) {
                    return RadioState.ON;
                }
            }
        }
        return hasAnyRadio ? RadioState.OFF : RadioState.HIDDEN;
    }
}