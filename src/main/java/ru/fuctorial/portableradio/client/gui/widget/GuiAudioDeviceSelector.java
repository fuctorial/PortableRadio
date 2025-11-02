package ru.fuctorial.portableradio.client.gui.widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import ru.fuctorial.portableradio.client.audio.AudioDeviceManager;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiAudioDeviceSelector extends GuiButton {

    private final List<String> displayDevices;
    private int selectedIndex;
    private final String label;

    public GuiAudioDeviceSelector(int id, int x, int y, int width, int height,
                                  String labelKey, boolean isInputDevice, int currentIndex) {
        super(id, x, y, width, height, "");
        this.label = I18n.format(labelKey);
        this.displayDevices = isInputDevice ? AudioDeviceManager.INSTANCE.getDisplayMicNames() : AudioDeviceManager.INSTANCE.getDisplaySpeakerNames();
        this.selectedIndex = (currentIndex >= 0 && currentIndex < displayDevices.size()) ? currentIndex : 0;
        updateDisplayString();
    }

    public int cycleDevice() {
        if (displayDevices.isEmpty()) {
            return 0;
        }
        selectedIndex = (selectedIndex + 1) % displayDevices.size();
        updateDisplayString();
        return selectedIndex;
    }

    private void updateDisplayString() {
        if (displayDevices.isEmpty() || selectedIndex >= displayDevices.size()) {
            this.displayString = this.label + ": " + I18n.format("gui.portableradio.no_devices");
        } else {
            String deviceName = displayDevices.get(selectedIndex);
            this.displayString = this.label + ": " + deviceName;
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean isHovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int hoverState = this.getHoverState(isHovered);

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);

            int color = 14737632;
            if (!this.enabled) {
                color = 10526880;
            } else if (isHovered) {
                color = 16777120;
            }

            String textToDisplay = this.displayString;
            int strWidth = mc.fontRenderer.getStringWidth(textToDisplay);
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

            if (strWidth > width - 8) {
                while (strWidth > width - 8 - ellipsisWidth && textToDisplay.length() > 0) {
                    textToDisplay = textToDisplay.substring(0, textToDisplay.length() - 1);
                    strWidth = mc.fontRenderer.getStringWidth(textToDisplay);
                }
                textToDisplay += "...";
            }

            this.drawCenteredString(mc.fontRenderer, textToDisplay, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);
        }
    }
}
