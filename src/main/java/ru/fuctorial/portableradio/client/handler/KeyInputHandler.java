package ru.fuctorial.portableradio.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import ru.fuctorial.portableradio.PortableRadio;
import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.client.audio.AudioCaptureThread;
import ru.fuctorial.portableradio.client.audio.AudioDeviceManager;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;
import ru.fuctorial.portableradio.common.network.PacketHandler;
import ru.fuctorial.portableradio.common.network.PacketPtt;

@SideOnly(Side.CLIENT)
public class KeyInputHandler {
    private boolean wasKeyDown = false;
    private static AudioCaptureThread captureThread;

    private static final long MIN_TRANSMISSION_DURATION_MS = 1000;
    private static final long COOLDOWN_MS = 1000;

    private long transmissionStartTime = 0;
    private long lastActionTime = 0;
    private boolean isWaitingForMinDuration = false;
    private boolean cooldownMessageShown = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        AudioCaptureThread.processClientThreadTasks();

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.currentScreen != null) {
            if (wasKeyDown) {
                handleForceStop();
            }
            return;
        }

        if (isWaitingForMinDuration && !wasKeyDown) {
            long elapsed = System.currentTimeMillis() - transmissionStartTime;
            if (elapsed >= MIN_TRANSMISSION_DURATION_MS) {
                stopTransmission();
                isWaitingForMinDuration = false;
            }
        }

        boolean isKeyDown = KeybindHandler.pttKey.getIsKeyPressed();

        if (isKeyDown && !wasKeyDown) {
            long now = System.currentTimeMillis();

            
            if (now - lastActionTime < COOLDOWN_MS) {
                
                if (!cooldownMessageShown) {
                    mc.thePlayer.addChatMessage(new ChatComponentTranslation("chat.portableradio.cooldown"));
                    cooldownMessageShown = true;
                }
                return;
            }

            cooldownMessageShown = false;
            lastActionTime = now;
            transmissionStartTime = now;
            startTransmission(mc.thePlayer);
            wasKeyDown = true;

        } else if (!isKeyDown && wasKeyDown) {
            long elapsed = System.currentTimeMillis() - transmissionStartTime;

            if (elapsed >= MIN_TRANSMISSION_DURATION_MS) {
                lastActionTime = System.currentTimeMillis();
                stopTransmission();
                wasKeyDown = false;
            } else {
                isWaitingForMinDuration = true;
                wasKeyDown = false;
            }
        }
    }

    private void startTransmission(EntityPlayer player) {
        ItemStack radioStack = getActiveRadioStack(player);
        if (radioStack == null) {
            ItemStack firstRadio = findFirstRadioStack(player);
            if (firstRadio != null) {
                player.addChatMessage(new ChatComponentTranslation("chat.portableradio.turn_on_first"));
            }
            return;
        }

        playSound(true);
        ItemWalkieTalkie radioItem = (ItemWalkieTalkie) radioStack.getItem();

        int micIndex = ClientConfig.getSelectedMicIndex();
        String selectedMicName = AudioDeviceManager.INSTANCE.getRealMicName(micIndex);
        float micVolume = radioItem.getMicrophoneVolume(radioStack);

        if (captureThread == null || !captureThread.isAlive()) {
            captureThread = new AudioCaptureThread(selectedMicName, micVolume);
            captureThread.start();
            PacketHandler.INSTANCE.sendToServer(new PacketPtt(true));
        }
    }

    private void stopTransmission() {
        if (captureThread != null) {
            playSound(false);
            captureThread.stopCapture();
            captureThread = null;
            PacketHandler.INSTANCE.sendToServer(new PacketPtt(false));
        }
    }

    private void handleForceStop() {
        stopTransmission();
        wasKeyDown = false;
        isWaitingForMinDuration = false;
    }

    private ItemStack getActiveRadioStack(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                if (((ItemWalkieTalkie) stack.getItem()).isRadioOn(stack)) {
                    return stack;
                }
            }
        }
        return null;
    }

    private void playSound(boolean isActivation) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && isActivation) {
            String soundName = "portableradio:radio_on";
            mc.getSoundHandler().playSound(
                    PositionedSoundRecord.func_147673_a(
                            new ResourceLocation(soundName)
                    )
            );
        }
    }

    private ItemStack findFirstRadioStack(EntityPlayer player) {
        if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemWalkieTalkie) {
            return player.getHeldItem();
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                return stack;
            }
        }
        return null;
    }
}