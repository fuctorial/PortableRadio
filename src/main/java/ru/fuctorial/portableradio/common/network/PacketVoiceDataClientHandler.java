package ru.fuctorial.portableradio.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.fuctorial.portableradio.client.ClientConfig;
import ru.fuctorial.portableradio.client.audio.AudioDeviceManager;
import ru.fuctorial.portableradio.client.audio.AudioPlaybackManager;
import ru.fuctorial.portableradio.common.item.ItemWalkieTalkie;

@SideOnly(Side.CLIENT)
public class PacketVoiceDataClientHandler implements IMessageHandler<PacketVoiceData, IMessage> {

    private static AudioPlaybackManager playbackManager = null;

    @Override
    public IMessage onMessage(PacketVoiceData message, MessageContext ctx) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return null;

        ItemStack radioStack = findActiveRadio(player);
        if (radioStack == null) return null;

        if (playbackManager == null) {
            playbackManager = new AudioPlaybackManager();
        }

        if (!playbackManager.isPlaying()) {
            ItemWalkieTalkie radioItem = (ItemWalkieTalkie) radioStack.getItem();

            int speakerIndex = ClientConfig.getSelectedSpeakerIndex();
            float speakerVolume = radioItem.getSpeakerVolume(radioStack);
            String speakerName = AudioDeviceManager.INSTANCE.getRealSpeakerName(speakerIndex);
            playbackManager.startPlayback(speakerName, speakerVolume);
        }

        playbackManager.queueAudio(message.getData());
        return null;
    }

    private ItemStack findActiveRadio(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemWalkieTalkie) {
                if (((ItemWalkieTalkie) stack.getItem()).isRadioOn(stack)) {
                    return stack;
                }
            }
        }
        return null;
    }
}