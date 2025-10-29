package ru.fuctorial.portableradio.client.gui.widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiSlider extends GuiButton {

    private float sliderValue = 1.0F;
    public boolean dragging = false;
    private final float minValue;
    private final float maxValue;
    private final String label;

    public GuiSlider(int id, int x, int y, int width, int height, String label, float minValue, float maxValue, float currentValue) {
        super(id, x, y, width, height, "");
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.label = label;

        setValue(currentValue);
    }


    public int getValue() {
        return (int) (minValue + (maxValue - minValue) * sliderValue);
    }


    public void setValue(float value) {
        this.sliderValue = (value - minValue) / (maxValue - minValue);
        updateDisplayString();
    }

    private void updateDisplayString() {
        this.displayString = label + ": " + getValue();
    }


    @Override
    public int getHoverState(boolean mouseOver) {
        return 0;
    }


    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            if (this.dragging) {
                this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                if (this.sliderValue < 0.0F) this.sliderValue = 0.0F;
                if (this.sliderValue > 1.0F) this.sliderValue = 1.0F;
                updateDisplayString();
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 8, 20);

            this.drawCenteredString(mc.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 14737632);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            if (this.sliderValue < 0.0F) this.sliderValue = 0.0F;
            if (this.sliderValue > 1.0F) this.sliderValue = 1.0F;
            updateDisplayString();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }
}