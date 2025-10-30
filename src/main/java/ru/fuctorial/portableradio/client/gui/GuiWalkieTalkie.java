package ru.fuctorial.portableradio.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.client.audio.AudioTestManager;
import ru.fuctorial.portableradio.client.gui.widget.GuiAudioDeviceSelector;
import ru.fuctorial.portableradio.client.gui.widget.GuiSlider;
import ru.fuctorial.portableradio.common.inventory.ContainerWalkieTalkie;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketUpdateFrequency;
import ru.fuctorial.portableradio.common.network.PacketUpdateVolume;

@SideOnly(Side.CLIENT)
public class GuiWalkieTalkie extends GuiContainer {

    private static final ResourceLocation guiTexture = new ResourceLocation("portableradio", "textures/gui/gui_walkie_talkie.png");
    private final ContainerWalkieTalkie container;
    private boolean isRadioOn = false;
    private GuiSlider frequencySlider;
    private GuiSlider micVolumeSlider;
    private GuiSlider speakerVolumeSlider;
    private GuiAudioDeviceSelector microphoneSelector;
    private GuiAudioDeviceSelector speakerSelector;
    private GuiButton testMicButton;
    private GuiButton testSpeakerButton;

    private long micTestStartTime = 0;
    private long speakerTestStartTime = 0;

    public GuiWalkieTalkie(ContainerWalkieTalkie container) {
        super(container);
        this.container = container;
        this.xSize = 176;
        this.ySize = 200;
        this.container.setGui(this);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        final int componentWidth = 130;
        final int componentX = guiLeft + (this.xSize - componentWidth) / 2;
        int yOffset = guiTop + 40;


        GuiButton customTestButtonMic = new GuiButton(4, componentX + componentWidth - 20, guiTop + 40 + 25, 20, 20, "") {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                if (this.visible) {
                    mc.getTextureManager().bindTexture(buttonTextures);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                    int hoverState = this.getHoverState(this.field_146123_n);
                    this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
                    this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);


                    if (AudioTestManager.INSTANCE.isMicrophoneTesting()) {
                        long elapsedTime = Minecraft.getSystemTime() - micTestStartTime;
                        int timeLeft = 5 - (int)(elapsedTime / 1000);
                        if (timeLeft < 0) timeLeft = 0;
                        String timerText = String.valueOf(timeLeft);
                        GuiWalkieTalkie.this.drawCenteredString(mc.fontRenderer, timerText, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0x00FF00);
                    } else {
                        GuiWalkieTalkie.this.drawCenteredString(mc.fontRenderer, "♪", this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xFFFFFF);
                    }
                }
            }
        };
        this.testMicButton = customTestButtonMic;

        GuiButton customTestButtonSpeaker = new GuiButton(5, componentX + componentWidth - 20, guiTop + 40 + 25 * 3, 20, 20, "") {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                if (this.visible) {
                    mc.getTextureManager().bindTexture(buttonTextures);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                    int hoverState = this.getHoverState(this.field_146123_n);
                    this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
                    this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);

                    if (AudioTestManager.INSTANCE.isSpeakerTesting()) {
                        long elapsedTime = Minecraft.getSystemTime() - speakerTestStartTime;
                        int timeLeft = 5 - (int)(elapsedTime / 1000);
                        if (timeLeft < 0) timeLeft = 0;
                        String timerText = String.valueOf(timeLeft);
                        GuiWalkieTalkie.this.drawCenteredString(mc.fontRenderer, timerText, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0x00FF00);
                    } else {
                        GuiWalkieTalkie.this.drawCenteredString(mc.fontRenderer, "♪", this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xFFFFFF);
                    }
                }
            }
        };
        this.testSpeakerButton = customTestButtonSpeaker;


        this.frequencySlider = new GuiSlider(1, componentX, yOffset, componentWidth, 20, I18n.format("gui.portableradio.frequency"), 100.0F, 500.0F, container.getFrequency());
        this.buttonList.add(this.frequencySlider);
        yOffset += 25;
        this.microphoneSelector = new GuiAudioDeviceSelector(2, componentX, yOffset, componentWidth - 25, 20, "gui.portableradio.microphone", true, ClientConfig.getSelectedMicIndex()); // ИСПРАВЛЕНИЕ: Используем ключ I18n для GuiAudioDeviceSelector
        this.buttonList.add(this.microphoneSelector);
        this.buttonList.add(this.testMicButton);
        yOffset += 25;
        this.micVolumeSlider = new GuiSlider(6, componentX, yOffset, componentWidth, 20, I18n.format("gui.portableradio.mic_volume"), 0.0F, 200.0F, container.getMicrophoneVolume());
        this.buttonList.add(this.micVolumeSlider);
        yOffset += 25;
        this.speakerSelector = new GuiAudioDeviceSelector(3, componentX, yOffset, componentWidth - 25, 20, "gui.portableradio.speaker", false, ClientConfig.getSelectedSpeakerIndex()); // ИСПРАВЛЕНИЕ: Используем ключ I18n для GuiAudioDeviceSelector
        this.buttonList.add(this.speakerSelector);
        this.buttonList.add(this.testSpeakerButton);
        yOffset += 25;
        this.speakerVolumeSlider = new GuiSlider(7, componentX, yOffset, componentWidth, 20, I18n.format("gui.portableradio.speaker_volume"), 0.0F, 200.0F, container.getSpeakerVolume());
        this.buttonList.add(this.speakerVolumeSlider);
    }



    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            ClientConfig.setSelectedMicIndex(this.microphoneSelector.cycleDevice());
        } else if (button.id == 3) {
            ClientConfig.setSelectedSpeakerIndex(this.speakerSelector.cycleDevice());
        } else if (button.id == 4) {
            if (AudioTestManager.INSTANCE.isMicrophoneTesting()) {
                AudioTestManager.INSTANCE.stopAllTests();
            } else {
                micTestStartTime = Minecraft.getSystemTime();
                AudioTestManager.INSTANCE.testMicrophone(ClientConfig.getSelectedMicIndex(), micVolumeSlider.getValue() / 100.0f);
            }
        } else if (button.id == 5) {
            if (AudioTestManager.INSTANCE.isSpeakerTesting()) {
                AudioTestManager.INSTANCE.stopAllTests();
            } else {
                speakerTestStartTime = Minecraft.getSystemTime();
                AudioTestManager.INSTANCE.testSpeaker(ClientConfig.getSelectedSpeakerIndex(), speakerVolumeSlider.getValue() / 100.0f);
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        if (state == 0) {
            if (this.frequencySlider != null && this.frequencySlider.dragging) {
                this.frequencySlider.mouseReleased(mouseX, mouseY);
                sendFrequencyUpdate();
            }
            if (this.micVolumeSlider != null && this.micVolumeSlider.dragging) {
                this.micVolumeSlider.mouseReleased(mouseX, mouseY);
            }
            if (this.speakerVolumeSlider != null && this.speakerVolumeSlider.dragging) {
                this.speakerVolumeSlider.mouseReleased(mouseX, mouseY);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        sendFrequencyUpdate();
        sendVolumeUpdates();
        AudioTestManager.INSTANCE.stopAllTests();
    }

    private void sendFrequencyUpdate() {
        if (this.frequencySlider != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketUpdateFrequency(this.frequencySlider.getValue()));
        }
    }

    private void sendVolumeUpdates() {
        if (this.micVolumeSlider != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketUpdateVolume(this.micVolumeSlider.getValue(), true));
        }
        if (this.speakerVolumeSlider != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketUpdateVolume(this.speakerVolumeSlider.getValue(), false));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        final int WIDGET_TEXT_COLOR = 14737632;
        String title = I18n.format("item.walkieTalkie.name");
        this.fontRendererObj.drawString(title, (this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2), 8, WIDGET_TEXT_COLOR);

        String testStatusKey = AudioTestManager.INSTANCE.getTestStatus();
        if (!testStatusKey.isEmpty()) {
            String testStatusDisplay = I18n.format(testStatusKey);
            int statusX = (this.xSize / 2 - this.fontRendererObj.getStringWidth(testStatusDisplay) / 2);
            int statusY = this.ySize - 30;
            this.fontRendererObj.drawString(testStatusDisplay, statusX, statusY, WIDGET_TEXT_COLOR);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(guiLeft, guiTop + this.ySize, 0, 0, 1);
        tessellator.addVertexWithUV(guiLeft + this.xSize, guiTop + this.ySize, 0, 1, 1);
        tessellator.addVertexWithUV(guiLeft + this.xSize, guiTop, 0, 1, 0);
        tessellator.addVertexWithUV(guiLeft, guiTop, 0, 0, 0);
        tessellator.draw();
        if (AudioTestManager.INSTANCE.isMicrophoneTesting() && testMicButton != null) {
            float level = AudioTestManager.INSTANCE.getCurrentAudioLevel();
            int buttonHeight = testMicButton.height;
            int fillHeight = (int) (buttonHeight * level);
            if (fillHeight > 0) {
                int color = (level < 0.3f) ? 0xAA00FF00 : (level < 0.7f) ? 0xAAFFFF00 : 0xAAFF0000;
                drawRect(testMicButton.xPosition, testMicButton.yPosition + buttonHeight - fillHeight, testMicButton.xPosition + testMicButton.width, testMicButton.yPosition + buttonHeight, color);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void updateFrequency(int frequency) { if (this.frequencySlider != null) this.frequencySlider.setValue(frequency); }
    public void updateStatus(boolean isRadioOn) { this.isRadioOn = isRadioOn; }
    public void updateMicrophoneVolume(int volume) { if (this.micVolumeSlider != null) this.micVolumeSlider.setValue(volume); }
    public void updateSpeakerVolume(int volume) { if (this.speakerVolumeSlider != null) this.speakerVolumeSlider.setValue(volume); }
}